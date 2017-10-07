package org.delta.distributed;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;

import java.util.ArrayList;
import java.util.List;

public class MainVerticle extends io.vertx.rxjava.core.AbstractVerticle {

  private List<Device> devices;

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    Router router = Router.router(vertx);
    BridgeOptions opts = new BridgeOptions()
      .addInboundPermitted(new PermittedOptions().setAddress("result.to.server"))
      .addOutboundPermitted(new PermittedOptions().setAddress("publish.to.client"));

    SockJSHandler sockHandler = SockJSHandler.create(vertx).bridge(opts);

    router.route().handler(BodyHandler.create());
    router.route("/bus/*").handler(sockHandler);
    router.route("/register/").handler(this::registerHandler);
    router.route().handler(StaticHandler.create().setCachingEnabled(false));

    devices = new ArrayList<>();

    vertx.createHttpServer().requestHandler(router::accept)
      .listen(9090, res -> {
        if (res.succeeded()) {
          startFuture.complete();
        } else
          startFuture.fail("Error occurred");
      });

    vertx.eventBus().consumer("result.to.server", message -> {
      String body = (String) message.body();
      System.out.println(body);
    });
  }

  public void registerHandler(RoutingContext routingContext) {

    String body = routingContext.getBodyAsString();

    final Device device = body != null ? Json.decodeValue(routingContext.getBodyAsString(),
        Device.class) : null;
    if(device != null && !devices.contains(device)) {
      devices.add(device);
      routingContext.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(new DeviceRegisterResponse("success")));

      vertx.setTimer(5000, l -> {
        List<ProgramPublish> publishes = new ArrayList<>();
        publishes.add(new ProgramPublish(1, tempProgram()));
        vertx.eventBus()
            .publish("publish.to.client", new JsonArray(publishes).toString());
      });

    } else {
      routingContext.response()
          .setStatusCode(200)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(new DeviceRegisterResponse("failure")));
    }
  }

  public void dummy() {
    long sum = 0;
    for(long i=0; i<99999999; i++) {
      sum += i;
    }
  }

  public String tempProgram() {
    return "package org.delta.distributed;\n"
        + "\n"
        + "import java.io.*;\n"
        + "import java.util.*;\n"
        + "\n"
        + "public class Main {\n"
        + "\t public static void main(String args[]) {\n"
        + "    long sum = 0;\n"
        + "    for(long i=0; i<99999999; i++) {\n"
        + "      sum += i;\n"
        + "    }"
        + "    System.out.println(sum);\n"
        + "\t}\n"
        + "}";
  }
}

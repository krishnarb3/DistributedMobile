package org.delta.distributed;

import com.squareup.javapoet.JavaFile;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.sockjs.SockJSHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainVerticle extends io.vertx.rxjava.core.AbstractVerticle {

  Integer temp = 0;
  Integer currWeight = 1;
  double res = 1;
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
    router.route("/weight/").handler(this::weightHandler);
    router.route().handler(StaticHandler.create().setCachingEnabled(true));

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
      Double num = Double.valueOf(body);
      System.out.println(body);
      res = res * num;
      temp++;
      if(temp == 4) {
        System.out.println(res);
      }
    });
  }

  public void weightHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response.end(currWeight + "");
    System.out.println("CurrWeight" + currWeight);
    currWeight++;
    if(currWeight > 4) currWeight = 1;
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

      vertx.setTimer(2000, l -> {
        List<ProgramPublish> publishes = new ArrayList<>();
        publishes.add(new ProgramPublish(1, generateProgram(1)));
        publishes.add(new ProgramPublish(2, generateProgram(2)));
        publishes.add(new ProgramPublish(3, generateProgram(3)));
        publishes.add(new ProgramPublish(4, generateProgram(4)));
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

  @DistributedMethod
  public static double multiply(long l, long u) {
    double product = l, i;
    for(i = l+1; i <= u; i++) {
      product *= i;
    }
    return product;
  }

  public String generateProgram(int i) {
    Path currentRelativePath = Paths.get("");
    String s = currentRelativePath.toAbsolutePath().toString();
    try {
      String res = Files.lines(Paths.get(s + "/" + i + "/" + "org/delta/distributed/Main.java"))
          .reduce("", (s1, s2) -> s1 + " " + s2);
      //System.out.println(res);
      return res;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
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

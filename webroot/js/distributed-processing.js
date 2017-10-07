var eventBus;

function init() {
    eventBus = new EventBus('/bus/');
    eventBus.onopen = function () {
        console.log("Inside onopen");

        eventBus.registerHandler("publish.to.client", function (error, response) {
            var messages = JSON.parse(response.body);
            for(var i = 0; i < messages.length; i++) {
                var program = messages[i].program;
                document.getElementById("program").innerHTML = program;
                console.log(program);
            }
        });
    };
}

function sendResult(result) {
    eventBus.send("result.to.server", result);
}
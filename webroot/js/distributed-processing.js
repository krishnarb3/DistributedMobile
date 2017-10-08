var eventBus;
var weight = 0;

function init() {

    $.get( "http://10.1.24.43:9090/weight/", function(data) {
        weight = data;
        console.log(weight);
    });

    eventBus = new EventBus('/bus/');
    eventBus.onopen = function () {
        console.log("Inside onopen");

        eventBus.registerHandler("publish.to.client", function (error, response) {
            var messages = JSON.parse(response.body);
            console.log(messages);
            for(var i = 0; i < messages.length; i++) {
                var temp = messages[i].weight;
                var program = messages[i].program;
                if(weight == temp) {
                    console.log(program);
                    document.getElementById("program").innerHTML = program;
                    Android.compileAndExecuteCode(program);
                }
            }
        });
    };
}

function stripExtraLines(text) {
    while (text.indexOf('\n') > -1) {
        text = text.replace('\n', ' ');
    }
    return text;
}

function sendResult(result) {
    console.log(result);
    eventBus.send("result.to.server", result);
}
$(document).ready(function(){
    var $txtMsg = $("#chatInput");
    var htmlChatTemplate = $("#chatbox-message").html();

    $("#frmChat").submit(function(e){
        e.preventDefault();

        var msg = { message: $txtMsg.val() };
        $.post("/chat/send", msg);
        $txtMsg.val("");
    });

    var $result = $("#messages-box");
    var source = new EventSource("/chat/receive");
    source.onmessage = function(event) {
        console.log(event.data);
        var obj = JSON.parse(event.data);
        var rendered = Mustache.render(htmlChatTemplate, obj);
        $result.prepend(rendered);
    };

    function loadMessages(){
        $.get("/chat/history", function(res){
            $.each(res, function(idx, item){
                var rendered = Mustache.render(htmlChatTemplate, item);
                $result.append(rendered);
            });
        });
    }

    loadMessages();
});
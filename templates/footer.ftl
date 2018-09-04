
</div><!-- /.container -->

<form id="frmChat" action="/chat/send" method="POST" class='chat-container'>

	<div class="wrapper-chat-container" style="background-image: url('/static/img/chat-pattern-background.jpg')">

		<div class="messages-container">

			<div id="messages-box" class="wrapper-messages-container">



			</div>

		</div>

		<div class="send-message-container">
		         <#if context.session().get("user") ??>
                    <div class="input-group">
                        <input
                            id="chatInput"
                            class="form-control"
                            placeholder="¿Chateas?"
                            maxlength="140"
                            type='text' />

                    </div>
		         <#else>
                    <div>
                        <input type='text'
                            disabled class='form-control'
                            placeholder="Inicie sesión para Chatear">
                    </div>
		         </#if>

		</div>
	</div>
</form>


<script type="text/ng-template" id="chatbox-message">
    <div class='one-message-container'>
        <p class="username">
            <a>{{ user }}</a>
            <span>{{ time }}</span>
        </p>
        <p class='message'>{{ message }}</p>
        <hr>
    </div>
</script>
<script type="text/ng-template" id="modalMessage.html">
    <div class="message-box-container" ng-click="ctrl.close()">
        <div class='alert alert-{{ctrl.type}}'>
            <p>{{ctrl.message}}</p>
            <p class='text-right'>
                <button class='btn btn-default'>Aceptar</button>
            </p>
        </div>
    </div>
</script>

</body>
</html>

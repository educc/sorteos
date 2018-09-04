<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <title>${ context.appName }</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link rel="stylesheet" href="/static/css/bootstrap.min.css">
    <link rel="stylesheet" href="/static/css/app.css">

    <script type="text/javascript" src="/static/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript" src="/static/js/mustache.min.js"></script>
    <script type="text/javascript" src="/static/js/chat.js"></script>
</head>
<body>
<!--[if IE ]>
<div class="browser-message">
    <div class="content-browser-message">
        <h1>${ context.appName }</h1>
        <p>Este aplicativo se ha creado con tecnologías modernas y este navegador no es una de ellas</p>
        <p>Por favor use Firefox o Chrome</p>
    </div>
</div>
<![endif]-->

<nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top">
    <a class="navbar-brand" href="/">Inicio</a>

    <div class="collapse navbar-collapse" id="navbarNav">
        <#if context.session().get("user") ??>
        <div class="navbar-nav mr-auto">
            <#if context.request().path()?starts_with("/app/sorteos") >
                <a class="nav-item nav-link active" href="/app/sorteos">Sorteos</a>
            <#else>
                <a class="nav-item nav-link" href="/app/sorteos">Sorteos</a>
            </#if>

            <#if context.request().path()?starts_with("/app/ganadores") >
                <a class="nav-item nav-link active" href="/app/ganadores">Ganadores</a>
            <#else>
                <a class="nav-item nav-link" href="/app/ganadores">Ganadores</a>
            </#if>
        </div>
        <form class="form-inline">
            <span class="mr-sm-2" style="color:white;">¡Hola ${ context.session().get("user") }!</span>
            <a class="btn btn-outline-info my-2 my-sm-0" href="/cerrar-sesion">Cerrar Sesión</a>
        </form>
        </#if>
    </div>
</nav>

<div class='main-container'>
<#include "header.ftl">
<div class="home-container">
    <div class="home-container-child center-background" style="background-image: url('/static/img/home.jpg?v=${ context.random }')">
        &nbsp;
    </div>

<div  class="login-container">

    <h4 class='text-center'>Bienvenidos a <strong><br>${ context.appName }</br></strong></h4>

    <div class='wrapper-login-container'>
        <form action="/iniciar-sesion" method="POST" class="form-horizontal">
            <div class="form-group">
                <div class="col-sm-12">
                    <input autocomplete="off" name="userId"
                           type="text" class="form-control" id="inputUser" placeholder="Matrícula">
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-12">

                    <div class="input-group">
                        <input
                                type="password"
                                class="form-control"
                                id="inputPassword"
                                name="password"
                                placeholder="Contraseña">

                        <span class="input-group-btn">
                <button
                        type='submit' class="btn btn-primary" type="button">Entrar</button>
              </span>
                    </div><!-- /input-group -->
                </div>
            </div>
            <#if context.appMessage ??>
            <div class="form-group" style='margin-bottom:0'>
                <div class="col-sm-12 text-left">
                    <div id='errorMessage' class="alert alert-danger">
                        ${ context.appMessage }
                    </div>
                </div>
            </div>
            </#if>
        </form>
    </div>
</div>

<#include "footer.ftl">


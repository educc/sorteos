<#include "../header.ftl">

<div class="page-header">
    <h1>Ganadores</h1>
</div>
<div class="fluid-container">
<div class='wrapper-fluid-container'>

    <#list context.list?keys as key>

        <div style="display:flex; justify-content:center;flex-wrap: wrap;">

         <#list context.list[key] as item>

                <div class="card sorteo-box " style="width: 18rem; margin: 10px 15px">
                    <img class="card-img-top" src='${ item.photoWinnerPath!"/static/img/ganador.jpg" }?v=${ context.random }' alt="Card image cap">
                    <div class="card-body">
                        <h5 class="card-title">${ item.name }</h5>
                        <p class="card-text">${ item.description }</p>
                        <p class="card-text">
                            <#if item.winner ??>
                            ${ item.winner }
                            <#else>
                            No hubo ganador
                        </#if>
                    </p>
                </div>
            </div>

        </#list>
        </div>

        <hr>
    </#list>
</div>
</div>



<#include "../footer.ftl">
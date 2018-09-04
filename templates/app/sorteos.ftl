<#include "../header.ftl">

<script type="text/javascript">
    var APP = {};
    APP.data = ${ context.list }
</script>
<div class="page-header">
    <#if context.session().get("isUserCanWin") >
        <#if context.session().get("userOptions") ??>

            <p>Tienes ${ context.session().get("userOptions") } opción(es) para los sorteos del año. Por estar aquí tienes una opción adicional.</p>
            <p>En total ${ context.session().get("userOptions")+1 } opciones</p>
        <#else>
            <p>No fue posible conseguir las opciones que usted tiene para el sorteo</p>
        </#if>
    <#else>
        <p>Usted ya ha ganado un sorteo y no puede ser elegido para los sorteos vigentes</p>
    </#if>
</div>
<div class="fluid-container">
    <div class='wrapper-fluid-container'>

        <div id="raffle-box" style="display:flex; justify-content:center;flex-wrap: wrap;">

        </div>
    </div>
</div>

<script type="text/ng-template" id="raffle-tmpl">
{{#list}}
    <div class="sorteo-box card"
         data-date="{{ dateString }}"
         style="width: 18rem; margin: 10px 15px">
        <img class="card-img-top" src='{{ photoPath }}?v=${ context.random }' alt="Card image cap">
        <div class="card-body">
            <h5 class="card-title">{{ name }}</h5>
            <p class="card-text">{{ description }}</p>
            <p class="card-text">{{ humanDate }}</p>
        </div>
    </div>
{{/list}}
</script>
<script type="text/javascript" src="/static/js/utils.js"></script>
<script type="text/javascript">
    $(document).ready(function(){

        function transform(){
            $.each(APP.data.body, function(idx,val){
                val.dateString = Utils.toDate(val.date).toString();
                val.humanDate = function(){
                    return Utils.humanDate(Utils.toDate(val.date));
                }
            });
        }

        function renderRaffles(callback){
            var $box = $("#raffle-box");
            var html = $("#raffle-tmpl").html();
            var rendered = Mustache.render(html, { list: APP.data.body });
            $box.append(rendered);

            callback($box);
        }

        function setTimerEvents($box){
            var urlImgLoading = Utils.addRnd("/static/img/loading.gif");
            var urlImgSeeWinner = Utils.addRnd("/static/img/quien_es_ganador.gif");
            var redirectPath = "/app/ganadores";

            $box.find("[data-date]").each(function(idx, val){
                var $boxRaffle = $(val);
                var futureDate = new Date($boxRaffle.attr("data-date"));
                if(Utils.isValidDate(futureDate)){
                    var diff = futureDate - (new Date());
                    console.log(diff);
                    setTimeout(function(){
                        console.log("raffle now");

                        var result = $boxRaffle.find("img");
                        if(result.length > 0){
                            result[0].src = urlImgLoading;
                            var $item = $(result[0])
                                    .toggleClass("winner-progress", true);

                            setTimeout(function(){
                                result[0].src = urlImgSeeWinner;

                                $item
                                    .toggleClass("winner-progress", false)
                                    .toggleClass("winner-select", true)
                                    .click(function(){
                                        window.location.href = redirectPath;
                                    });
                            }, 10*1000);
                        }
                    }, diff);
                }
            });
        }

        transform();
        renderRaffles(setTimerEvents);
    });
</script>

<#include "../footer.ftl">

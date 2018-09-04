package com.ecacho.sorteos.web;

import com.ecacho.sorteos.Events;
import com.ecacho.sorteos.repository.RepositoryVerticle;
import com.ecacho.sorteos.repository.model.raffle.Raffle;
import com.ecacho.sorteos.utils.codec.ObjectCodec;
import com.ecacho.sorteos.web.chat.ChatRepository;
import com.ecacho.sorteos.web.model.UserMessage;
import com.ecacho.sorteos.web.model.UsersOnlineRepository;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.JsonParser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;


@Slf4j
public class WebVerticle extends AbstractVerticle {

  private UsersOnlineRepository usersOnlineRepo;
  private ChatRepository chatRepo;
  private JsonObject appConfig;

  private static final String EB_CHAT = "com.ecacho.sorteos.chat";

  private static final String DEFAULT_PATH = "DEFAULT_PATH";
  private static final String SERVER_PORT = "SERVER_PORT";

  private static final String CTX_APP_NAME = "appName";
  private static final String CTX_RANDOMNUMBER = "random";
  private static final String CTX_USER_OPTION_EXTRA = "hasUserOptionExtra";

  public static final String SESSION_MESSAGE = "appMessage";

  @Override
  public JsonObject config() {
    return appConfig;
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Future configFut = configureConfig();

    vertx.eventBus().registerDefaultCodec(UserMessage.class, new ObjectCodec<UserMessage>(UserMessage.class));
    registerEventsAtBus();

    configFut.compose(v -> {
      startHttpServer();
    }, startFuture);
  }

  private void registerEventsAtBus(){

  }

  private boolean prepareChatRepository(){
    this.chatRepo = new ChatRepository();
    return this.chatRepo.prepareRepository();
  }

  public Future<JsonObject> configureConfig(){
    ConfigStoreOptions store = new ConfigStoreOptions()
            .setType("file")
            .setFormat("properties")
            .setConfig(new JsonObject().put("path", "config.properties"));

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
            new ConfigRetrieverOptions().addStore(store));

    Future<JsonObject> f = Future.future();
    retriever.getConfig(result -> {
      if( result.failed()){
        f.fail(result.cause().getCause());
        return;
      }

      appConfig = result.result();
      f.complete(result.result());

    });

    return f;
  }

  public Future<Void> startHttpServer() {
    Future<Void> future = Future.future();

    HttpServer server = vertx.createHttpServer();

    int port = config().getInteger(SERVER_PORT, 9000);

    usersOnlineRepo = new UsersOnlineRepository();
    if(!prepareChatRepository()){
      future.fail("Cannot connect to Chat Messages database");
    }

    Router router = Router.router(vertx);

    setSessionHandlers(router);
    setRoutesApp(router);

    server
      .requestHandler(router::accept)
      .listen(port, ar -> {
        if (ar.succeeded()) {
          log.info("HTTP server running on port " + port);
          future.complete();
        } else {
          log.error("Could not start a HTTP server", ar.cause());
          future.fail(ar.cause());
        }
      });
    return future;
  }

  private void setSessionHandlers(Router router){
    // We need a cookie handler first
    router.route().handler(CookieHandler.create());
    SessionStore store = LocalSessionStore.create(vertx);
    SessionHandler sessionHandler = SessionHandler.create(store);
    router.route().handler(sessionHandler);
  }

  private void setRoutesApp(Router router){
    TemplateEngine engine = FreeMarkerTemplateEngine.create();
    TemplateHandler handlerTemplateMaker = TemplateHandler.create(
            engine,"templates", "text/html;charset=utf-8");

    router.route().handler(this::setContextNamesHandler);
    router.route("/static/*").handler(StaticHandler.create());

    router.get("/").handler(this::home);
    router.post("/iniciar-sesion")
            .handler(BodyHandler.create())
            .handler(it -> {
              String programAuth = config().getString(
                  "authApp",
                  "auth.exe");
              String loginFailMessage = config().getString(
                      "mensajeInicioSesion",
                      "Usted no pertenece al club de eventos");

              SessionHandlers.login(vertx, it, programAuth, loginFailMessage);
            });
    router.get("/cerrar-sesion").handler(SessionHandlers::logout);
    router.get("/online").handler(ctx -> {
      ctx.response()
              .putHeader("Content-Type", "application/json")
              .end(this.usersOnlineRepo.toString());
    });
    router.get("/app/*")
            .handler(SessionHandlers::verifyUserLoggedIn);
    router.get("/app/sorteos").handler(this::showRaffles);
    router.get("/app/ganadores").handler(this::showWinners);
    router.post("/chat/send")
            .handler(BodyHandler.create())
            .handler(SessionHandlers::verifyUserLoggedIn)
            .handler(this::chatSend);
    router.get("/chat/receive")
            .handler(this::chatReceive);
    router.get("/chat/history")
            .handler(this::chatHistory);

    router.get("/*")
            .handler(this::putAndClearAppMessageToContext)
            .handler(handlerTemplateMaker);
  }

  private void putAndClearAppMessageToContext(RoutingContext ctx){
    Session session = ctx.session();
    String text = session.get(SESSION_MESSAGE);
    if(text != null){
      ctx.put(SESSION_MESSAGE, text);
    }
    session.remove(SESSION_MESSAGE);
    ctx.next();
  }

  private void chatHistory(RoutingContext ctx){
    vertx.executeBlocking(future -> {
      List result = chatRepo.getLastMessages(50);
      future.complete(result);
    }, false, res -> {
      ctx.response()
              .putHeader("content-type","application/json")
              .end(Json.encode(res.result()));
    });
  }

  /**
   * Send messages to EventBus, this messages arrive to any
   * client connected
   * @param ctx
   */

  private void chatSend(RoutingContext ctx){
    MultiMap attributes = ctx.request().formAttributes();

    String content = attributes.get("message");
    if(content != null && !content.isEmpty()){
      String username = ctx.session().get(SessionHandlers.SESSION_USER);
      UserMessage umsg = new UserMessage(username, content);
      vertx.eventBus().publish(EB_CHAT, umsg);


      vertx.executeBlocking(future -> {
        chatRepo.addMessage(umsg);
        future.complete();
      },false, res -> {});
    }
    ctx.response().end();
  }


  private void chatReceive(RoutingContext ctx){
    ctx.response()
            .putHeader("cache-control","no-cache")
            .putHeader("content-type", "text/event-stream")
            .putHeader("connection", "keep-alive")
            .setChunked(true)
            .writeContinue();

    MessageConsumer<UserMessage> consumer = vertx.eventBus().consumer(EB_CHAT);


    String userId = ctx.session().get(SessionHandlers.SESSION_USER_ID);
    if(userId != null){
      this.usersOnlineRepo.add(userId);
    }

    consumer.handler(it -> {
      if (it.body() == null) return;
      if(ctx.response().closed()){
        consumer.unregister();
        return;
      }

      String msg = "data: " + Json.encode(it.body()) + "\n\n";
      ctx.response().write(msg);
    });

    ctx.response().endHandler(ti -> {
      if(userId != null ){
        this.usersOnlineRepo.remove(userId);
      }
      consumer.unregister();
    });
  }

  private void showWinners(RoutingContext ctx){
    vertx.eventBus().send(
      Events.GET_ALL_WINNERS, "", reply -> {
        if(reply.failed()){
          ctx.response().end(reply.cause().getMessage());
          return;
        }

        List<Raffle> result = (List<Raffle>) reply.result().body();
        result.sort(new Comparator<Raffle>() {
          @Override
          public int compare(Raffle o1, Raffle o2) {
            LocalDateTime d1 = o1.getDate();
            LocalDateTime d2 = o2.getDate();

            if(d1 == null && d2 == null){  return 0; }
            if(d1 == null){ return 1; }
            if(d2 == null){ return -1; }

            return d2.compareTo(d1);
          }
        });

        LinkedHashMap<String, List> groupResult = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("YYYYMM");

        result.forEach(it -> {
          String key = "null";
          if(it.getDate() != null){
            key = it.getDate().format(fmt);
          }

          if(groupResult.containsKey(key)){
            groupResult.get(key).add(it);
          }else{
            LinkedList<Raffle> obj = new LinkedList();
            obj.add(it);
            groupResult.put(key, obj);
          }

        });

        ctx.put("list", groupResult).next();
      });
  }

  private void showRaffles(RoutingContext ctx){

    vertx.eventBus().send(
      Events.GET_RAFFLE, "", reply -> {
        if(reply.failed()){
          ctx.response().end(reply.cause().getMessage());
          return;
        }

        JsonObject jsonToEncode = new JsonObject();
        jsonToEncode.put("body", reply.result().body());

        ctx.put("list", jsonToEncode.encode())
           .next();
      });
  }


  private void home(RoutingContext ctx){
    if(SessionHandlers.isUserLoggedIn(ctx)){
      RouterUtils.redirect(ctx,"/app/");
      return;
    }
    Session session = ctx.session();
    String appMessage = session.get(SESSION_MESSAGE);
    if (appMessage != null){
      ctx.put(SESSION_MESSAGE, appMessage);
    }
    ctx.next();
  }

  private void setContextNamesHandler(RoutingContext ctx){
    ctx.put(CTX_RANDOMNUMBER, new Double(Math.random()*1000).intValue());
    ctx.put(CTX_APP_NAME, config().getString(CTX_APP_NAME, "Sorteos App"));
    ctx.next();
  }
}

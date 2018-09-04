package com.ecacho.sorteos.web;

import com.ecacho.sorteos.Events;
import com.ecacho.sorteos.repository.RepositoryVerticle;
import com.ecacho.sorteos.repository.model.users.User;
import com.sun.javafx.binding.StringFormatter;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SessionHandlers {

  public static final String SESSION_USER = "user";
  public static final String SESSION_USER_ID = "userId";
  public static final String SESSION_USER_OPTIONS= "userOptions";
  public static final String SESSION_USER_CANWIN= "isUserCanWin";

  private static final String CTX_AUTH_APP = "authApp";

  public static boolean isUserLoggedIn(RoutingContext ctx){
    Session session = ctx.session();
    if(session.get(SESSION_USER) == null) {
      return  false;
    }
    return true;
  }

  public static void verifyUserLoggedIn(RoutingContext ctx){
    if(!isUserLoggedIn(ctx)){
        Session session = ctx.session();
        session.put(WebVerticle.SESSION_MESSAGE, "Debe autenticarse primero");
        RouterUtils.redirect(ctx, "/");
        return;
    }
    ctx.next();
  }

  public static void logout(RoutingContext ctx){
    ctx.session().destroy();
    RouterUtils.redirect(ctx, "/");
  }

  public static boolean loginWithAD(String programAuth, String username, String password){
      Process process = null;
      boolean result = false;
      try {
          String cmd = String.format("%s %s %s", programAuth, username.toUpperCase(), password);
          process = Runtime.getRuntime().exec(cmd);
          process.waitFor(5, TimeUnit.SECONDS);
          result = process.exitValue() == 1;
      } catch (Exception e) {
          log.error("Error at login with ActiveDirectory", e);
      } finally {
          if(process != null){
              process.destroy();
          }
      }
      return result;
  }


  public static void login(Vertx vertx, RoutingContext ctx, String programAuth, String loginFailMessage){
    MultiMap attributes = ctx.request().formAttributes();

    vertx.eventBus().send(Events.FIND_USER,
            attributes.get("userId"),
            reply -> {
              if(reply.failed()){
                ctx.response().putHeader("Content-Type","text/plain")
                        .end(reply.cause().getMessage());
                return;
              }

              Optional<User> user = (Optional<User>) reply.result().body();
              Session session = ctx.session();

              if(user.isPresent()){


                if(shouldVerifyPass()){
                  vertx.executeBlocking(future -> {

                    future.complete(loginWithAD(programAuth,
                            attributes.get("userId"),
                            attributes.get("password")));
                  }, false, res -> {
                    if(res.failed()){
                      session.put(WebVerticle.SESSION_MESSAGE, "Hubo un problema en el logueo, consulte a soporte.");
                      RouterUtils.redirect(ctx, "/");
                      return;
                    }

                    handleLoginAction(res.result(), user, ctx);
                  });
                }else{
                  handleLoginAction(new Boolean(true), user, ctx);
                }

              }else{
                session.put(WebVerticle.SESSION_MESSAGE, loginFailMessage);
                RouterUtils.redirect(ctx, "/");
              }
            });
  }

  private static void handleLoginAction(Object result, Optional<User> user, RoutingContext ctx){
    Session session = ctx.session();

    if( Boolean.TRUE.equals(result)){
      String fullname = user.get().getName() + " " + user.get().getLastName();

      session.put(SESSION_USER, fullname);
      session.put(SESSION_USER_ID, user.get().getId());
      session.put(SESSION_USER_OPTIONS, user.get().getOptions());
      session.put(SESSION_USER_CANWIN, user.get().isCanWin());

      RouterUtils.redirect(ctx, "/app/");
    }else{
      session.put(WebVerticle.SESSION_MESSAGE, "Credenciales incorrectas.");
      RouterUtils.redirect(ctx, "/");
    }
  }

  private static boolean shouldVerifyPass(){
    String ignore = System.getenv("IGNORE_PASS");
    if(ignore != null){
      try{
        return Boolean.parseBoolean(ignore);
      }catch (NumberFormatException ex){}
    }
    return false;
  }
}


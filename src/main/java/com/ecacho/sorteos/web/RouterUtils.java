package com.ecacho.sorteos.web;


import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class RouterUtils {

  public static void redirect(RoutingContext ctx, String uri){

    ctx.response()
      .putHeader(HttpHeaders.LOCATION, uri)
      .setStatusCode(302)
      .end("Redirecting to " + uri + ".");
  }
}

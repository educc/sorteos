package com.ecacho.sorteos;


import com.ecacho.sorteos.repository.RepositoryVerticle;
import com.ecacho.sorteos.reward.RewardVerticle;
import com.ecacho.sorteos.web.WebVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Future<Void> startFuture) throws Exception {
    Future<String> dbVerticleDeployment = Future.future();

    vertx.deployVerticle(new RepositoryVerticle(), dbVerticleDeployment.completer());
    vertx.deployVerticle(new RewardVerticle(), dbVerticleDeployment.completer());

    dbVerticleDeployment.compose(id -> {
      Future<String> httpVerticleDeployment = Future.future();

      vertx.deployVerticle(new WebVerticle(), new DeploymentOptions().setInstances(1),
      httpVerticleDeployment.completer());
      return httpVerticleDeployment;
    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }
}
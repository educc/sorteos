package com.ecacho.sorteos.reward;

import com.ecacho.sorteos.Events;
import com.ecacho.sorteos.repository.RepositoryVerticle;
import com.ecacho.sorteos.repository.model.raffle.Raffle;
import com.ecacho.sorteos.repository.model.users.User;
import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.RxHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class RewardVerticle extends AbstractVerticle {

  private static final int DELAY = 6000; //1 second


  @Override
  public void start(Future<Void> startFuture) throws Exception {
    vertx.setTimer(DELAY, this::startRewardProcess);
    startFuture.complete();
  }

  private void startRewardProcess(long id){
    //GET RAFFLEs
    vertx.eventBus().send(Events.GET_RAFFLE, "", reply -> {
      if(reply.failed()){
        log.error("reward process failed at getRaffle", reply.cause().getMessage());
        return;
      }

      List<Raffle> raffles = (List<Raffle>) reply.result().body();
      LocalDateTime now = LocalDateTime.now();

      Observable.fromIterable(raffles)
              .subscribeOn(RxHelper.scheduler(vertx))
              .filter(it -> it.getDate() != null)
              .filter(it ->  now.isAfter(it.getDate()))
              .toList()
              .subscribe(list -> {
                if (list.isEmpty()) {
                  vertx.setTimer(DELAY, this::startRewardProcess);
                  return;
                }
                log.info("raffles found: " + list.size());

                //GET USERS
                vertx.eventBus().send(Events.GET_ALL_USER, "", replyUsers -> {
                  if (replyUsers.failed()) {
                    log.error("reward process failed at getRaffle", reply.cause().getMessage());
                    return;
                  }

                  List<User> users = (List<User>) replyUsers.result().body();

                  Observable.fromIterable(list)
                          .subscribeOn(RxHelper.scheduler(vertx))
                          .doOnComplete(() -> vertx.setTimer(DELAY, this::startRewardProcess))
                          .subscribe(raffle -> {
                            User user = selectWinner(users);
                            if(user != null){
                              users.removeIf(it -> it.getId().equals(user.getId()));
                              raffle.setWinner(user.completeName());
                              raffle.setPhotoWinnerPath(raffle.getPhotoPath());
                              raffle.setPhotoPath(null);

                              vertx.eventBus().publish(Events.SAVE_WINNER, raffle);
                            }else{
                              log.error("Nadie ganó: " + raffle.getName());
                            }
                          });
                });
              });
    });


  }

  private User selectWinner(List<User> users){
    Map<User, HoldTwoInteger> usersFilter = new LinkedHashMap<>();

    int sum = 0;
    for(User u : users){
      if(!u.isCanWin()){ continue; }

      usersFilter.put(u,new HoldTwoInteger(sum, sum+u.getOptions()));
      sum += u.getOptions();
    }

    int winnerNumber = (int) (Math.random() * (sum-1));

    User winner = null;
    for(Map.Entry<User, HoldTwoInteger> item: usersFilter.entrySet()){
      if (winnerNumber >= item.getValue().min && winnerNumber < item.getValue().max){
        winner = item.getKey();
        break;
      }
    }
    return winner;
  }

  @AllArgsConstructor
  private static class HoldTwoInteger {
    int min;
    int max;
  }
}

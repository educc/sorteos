package com.ecacho.sorteos.repository;

import com.ecacho.sorteos.Events;
import com.ecacho.sorteos.utils.codec.ObjectCodec;
import com.ecacho.sorteos.repository.model.raffle.Raffle;
import com.ecacho.sorteos.repository.model.raffle.RaffleRepository;
import com.ecacho.sorteos.repository.model.users.User;
import com.ecacho.sorteos.repository.model.users.UsersRepository;
import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

import java.io.File;
import java.util.LinkedList;
import java.util.Optional;

@Slf4j
public class RepositoryVerticle extends AbstractVerticle implements FileListener {

  RaffleRepository raffleRepo;
  RaffleRepository raffleWinnerRepo;
  UsersRepository usersRepo;

  private static final String PATH_DATA = "data";
  private static final String FILE_RAFFLE = "sorteos.txt";
  private static final String FILE_RAFFLE_WINNER = "ganadores.txt";
  private static final String FILE_USERS = "usuarios.txt";



  @Override
  public void start() {
    vertx.eventBus().registerDefaultCodec(LinkedList.class, new ObjectCodec<LinkedList>(LinkedList.class));
    vertx.eventBus().registerDefaultCodec(Optional.class, new ObjectCodec<Optional>(Optional.class));
    vertx.eventBus().registerDefaultCodec(Raffle.class, new ObjectCodec<Raffle>(Raffle.class));

    raffleRepo = new RaffleRepository(
            getAbspath(PATH_DATA + File.separator + FILE_RAFFLE));
    raffleWinnerRepo = new RaffleRepository(
            getAbspath(PATH_DATA + File.separator + FILE_RAFFLE_WINNER));
    usersRepo = new UsersRepository(
            getAbspath(PATH_DATA + File.separator + FILE_USERS));

    vertx.eventBus().<Raffle>consumer(Events.SAVE_WINNER, message -> {
      if (this.raffleWinnerRepo.save(message.body())) {
        this.raffleRepo.remove(message.body());
        this.raffleWinnerRepo.getData().add(message.body());
      }
    });

    vertx.eventBus().consumer(Events.GET_RAFFLE, message -> {
      message.reply(raffleRepo.getData());
    });

    vertx.eventBus().consumer(Events.GET_ALL_WINNERS, message -> {
      message.reply(raffleWinnerRepo.getData());
    });

    vertx.eventBus().consumer(Events.GET_ALL_USER, message -> {
      message.reply(usersRepo.getData());
    });

    vertx.eventBus().consumer(Events.FIND_USER, message -> {
      if(message.isSend()){
        Optional<User> user = usersRepo.findById(message.body().toString());
        message.reply(user);
      }
    });

    setMonitor();
    loadRepositories();
  }

  private String getAbspath(String relativepath){
    //File jarDir = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
    //String path = jarDir.getAbsolutePath();
    String path = "";
    if(System.getenv("DATA_PATH") != null){
      return System.getenv("DATA_PATH") + File.separator + relativepath;
    }
    return path + File.separator + relativepath;
  }

  private void setMonitor(){
    try {
      FileSystemManager manager = VFS.getManager();
      FileObject dir = manager.resolveFile(getAbspath(PATH_DATA));

      DefaultFileMonitor fm = new DefaultFileMonitor(this);
      fm.setDelay(5000);
      fm.addFile(dir);
      fm.start();
    } catch (FileSystemException e) {
      log.error("at start FileSystemManager",e);
    }
  }

  private void loadRepositories(){
    this.raffleWinnerRepo.load();
    this.raffleRepo.load();
    this.usersRepo.load();
  }

  @Override
  public void fileCreated(FileChangeEvent event) throws Exception { }

  @Override
  public void fileDeleted(FileChangeEvent event) throws Exception { }

  @Override
  public void fileChanged(FileChangeEvent e) throws Exception {
    if(FILE_RAFFLE.equalsIgnoreCase(e.getFile().getName().getBaseName())){
      raffleRepo.load();
    }

    if(FILE_USERS.equalsIgnoreCase(e.getFile().getName().getBaseName())){
      usersRepo.load();
    }

    if(FILE_RAFFLE_WINNER.equalsIgnoreCase(e.getFile().getName().getBaseName())){
      raffleWinnerRepo.load();
    }
  }
}

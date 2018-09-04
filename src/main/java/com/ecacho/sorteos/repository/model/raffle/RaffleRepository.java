package com.ecacho.sorteos.repository.model.raffle;

import com.ecacho.sorteos.repository.model.PlainTextRepository;
import lombok.extern.slf4j.Slf4j;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class RaffleRepository implements PlainTextRepository<List<Raffle>> {
  LinkedList<Raffle> data;
  ReadWriteLock lock;
  Lock writeLock;
  Lock readLock;
  String filename;

  private static RaffleRepository repo;

  private static final String K_DESCRIPTION = "descripcion";
  private static final String K_PHOTO = "foto";
  private static final String K_PHOTO_WINNER = "foto_ganador";
  private static final String K_DATE = "fecha";
  private static final String K_GANADOR = "ganador";

  public RaffleRepository(String file){
    filename = file;
    data = new LinkedList<>();
    lock = new ReentrantReadWriteLock();
    writeLock = lock.writeLock();
    readLock = lock.readLock();
  }

  public boolean save(Raffle raffle){
    if(createIfNotExists(filename)){
      try {
        Ini ini = new Ini(new File(filename));
        Profile.Section sec = ini.add(raffle.getName());

        sec.add(K_DESCRIPTION, raffle.getDescription());
        sec.add(K_PHOTO, raffle.getPhotoPath());
        sec.add(K_PHOTO_WINNER, raffle.getPhotoWinnerPath());
        sec.add(K_DATE, raffle.toDateString());
        sec.add(K_GANADOR, raffle.getWinner());

        ini.store();
        return true;
      } catch (IOException e) {
        log.error("at save winner: " + filename, e);
      }
    }
    return  false;
  }

  public boolean remove(Raffle item){
    if( removeItemPersistent(item) ){
      this.data.removeIf(it -> it.getName().equals(item.getName()));
      return true;
    }
    return false;
  }

  private boolean removeItemPersistent(Raffle raffle){
    try {
      Ini ini = new Ini(new File(filename));

      if( ini.containsKey(raffle.getName()) ){
        ini.remove(ini.get(raffle.getName()));
        ini.store();
        return true;
      }
    } catch (IOException e) {
      log.error("at remove ganador: " + filename, e);
    }
    return false;
  }

  private boolean createIfNotExists(String filename){
    Path path = Paths.get(filename);
    if(!Files.exists(path)){
      try {
        Files.createFile(path);
      } catch (IOException e) {
        log.error("at create file: " + filename);
        return false;
      }
    }
    return true;
  }

  @Override
  public void load(){
    Ini ini = new Ini();
    try {
      ini.load(new FileReader(new File(filename)));
    } catch (IOException e) {
      log.error("at load: " + filename, e);
    }

    try {
      writeLock.lock();
      data.clear();
      for (String key : ini.keySet()) {
        Profile.Section sec = ini.get(key);

        Raffle item = new Raffle();
        item.setName(sec.getName());
        item.setDescription(sec.get(K_DESCRIPTION));
        item.setWinner(sec.get(K_GANADOR));
        item.setPhotoPath(sec.get(K_PHOTO));
        item.setPhotoWinnerPath(sec.get(K_PHOTO_WINNER));
        item.setAndParseDate(sec.get(K_DATE));



        data.add(item);
      }
    }catch(Exception ex){
      log.error("at load " + filename, ex);
    }finally {
      writeLock.unlock();
    }
  }
  public List<Raffle> getData(){
    try{
      readLock.lock();
      return data;
    }finally {
      readLock.unlock();
    }
  }
}

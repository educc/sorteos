package com.ecacho.sorteos.repository.model.users;

import com.ecacho.sorteos.repository.model.PlainTextRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class UsersRepository implements PlainTextRepository<List<User>> {

  LinkedList<User> data;
  ReadWriteLock lock;
  Lock writeLock;
  Lock readLock;
  String filename;

  private static UsersRepository repo;

  private static final int IDX_ID = 0;
  private static final int IDX_NAME = 1;
  private static final int IDX_LASTNAME = 2;
  private static final int IDX_OPTIONS = 3;
  private static final int IDX_CANWIN = 4;


  public UsersRepository(String file){
    filename = file;
    data = new LinkedList<>();
    lock = new ReentrantReadWriteLock();
    writeLock = lock.writeLock();
    readLock = lock.readLock();
  }

  @Override
  public void load() {
    this.writeLock.lock();
    this.data.clear();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line = reader.readLine(); //avoid first line
      while( (line = reader.readLine()) != null){
        line = line.trim();
        if(!line.isEmpty()){
          String[] parts = line.split(",");
          if(parts.length == 5){
            User u = new User();
            u.setId(parts[IDX_ID]);
            u.setName(parts[IDX_NAME]);
            u.setLastName(parts[IDX_LASTNAME]);
            u.setAndParseOptions(parts[IDX_OPTIONS]);
            u.setAndParseCanWin(parts[IDX_CANWIN]);
            this.data.add(u);
          }else{
            log.warn("No contiene 5 columnas: " + line);
          }
        }
      }
    } catch (IOException e) {
      log.error("Error at load: " + filename, e);
    } finally {
      this.writeLock.unlock();
    }
  }

  public Optional<User> findById(String id){
    return this.getData().stream().filter(it -> it.getId().equalsIgnoreCase(id)).findFirst();
  }

  @Override
  public List<User> getData() {
    try{
      readLock.lock();
      return data;
    }finally {
      readLock.unlock();
    }
  }
}


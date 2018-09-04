package com.ecacho.sorteos.web.model;

import io.vertx.core.json.Json;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * Save in memory all users connected to the "sorteos" page
 */
public class UsersOnlineRepository {

  private LinkedList<String> data;
  private ReentrantReadWriteLock lock;
  private Lock writeLock;
  private Lock readLock;

  public UsersOnlineRepository(){
    data = new LinkedList<>();
    lock = new ReentrantReadWriteLock();
    writeLock = lock.writeLock();
    readLock = lock.readLock();
  }

  public void remove(String userId){
    writeLock.lock();
    data.removeIf(it -> it.equalsIgnoreCase(userId));
    writeLock.unlock();
  }

  public void add(String userId){
    writeLock.lock();
    data.add(userId);
    writeLock.unlock();
  }

  public boolean exists (String userId){
    readLock.lock();
    boolean result = this.data.contains(userId);
    readLock.unlock();
    return result;
  }

  @Override
  public String toString() {
    readLock.lock();
    try{
      return Json.encode(data);
    }finally {
      readLock.unlock();
    }

  }
}

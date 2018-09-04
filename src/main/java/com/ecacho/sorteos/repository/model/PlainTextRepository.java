package com.ecacho.sorteos.repository.model;

public interface PlainTextRepository<T> {

  void load();
  T getData();
}

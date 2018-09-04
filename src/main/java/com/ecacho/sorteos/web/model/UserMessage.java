package com.ecacho.sorteos.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UserMessage {
  private Integer id;
  private String user;
  private String message;

  public  UserMessage(String user, String message){
    this.user = user;
    this.message = message;
  }
}

package com.ecacho.sorteos.repository.model.users;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class User {
  String id;
  String name;
  String lastName;
  int options;
  boolean canWin;

  private static final String YES = "si";

  public String completeName(){
    return String.format("%s: %s %s", id, name, lastName);
  }

  /**
   * if canWinStr = yes then canWin = true any other case canWin = false
   * @param canWinStr
   */
  public void setAndParseCanWin(String canWinStr){
    this.canWin = false;
    if( canWinStr != null && YES.equalsIgnoreCase(canWinStr.trim()) ){
      this.canWin = true;
    }
  }

  /**
   * set a number to the options attribute
   * if the param is not a number, options = 0
   * @param number
   */
  public void setAndParseOptions(String number){
    this.options = 0;
    if(number == null){ return; }

    try{
      this.options = Integer.parseInt(number.trim());
    }catch (Exception ex){}
  }
}

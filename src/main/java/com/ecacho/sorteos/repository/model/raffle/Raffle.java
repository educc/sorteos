package com.ecacho.sorteos.repository.model.raffle;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@ToString
@Slf4j
public class Raffle {
  String name;
  String description;
  String photoPath;
  String photoWinnerPath;
  String winner;
  LocalDateTime date;

  public Raffle() {
    this.name = "";
    this.description = "";
    this.photoPath = "";
    this.winner = "";
    this.date = LocalDateTime.of(1970,1,1,0,0);
  }

  public static final String FORMAT_DATE = "dd/MM/yyyy hh:mm a";

  public String toDateString(){
    if(date == null){
      return "";
    }

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern(FORMAT_DATE);
    return date.format(fmt);
  }

  public void setAndParseDate(String date){
    if(date != null){
      String[] parts = date.split(" ");
      if(parts.length == 3){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(FORMAT_DATE);
        String dateWithoutSpace = String.join(" ", parts[0].trim(), parts[1].trim(), parts[2].trim())
                .toUpperCase();

        try{
          this.date = LocalDateTime.parse(dateWithoutSpace, fmt);
        }catch (Exception ex){
          log.error("at convert date: " + date);
        }
      }else{
        log.error("invalid date format (use " + FORMAT_DATE + " ): " + date);
      }
    }

  }
}

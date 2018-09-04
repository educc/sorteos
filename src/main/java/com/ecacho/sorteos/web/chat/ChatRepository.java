package com.ecacho.sorteos.web.chat;

import com.ecacho.sorteos.web.model.UserMessage;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class ChatRepository {

  private LocalDatabase db;

  private static final String DB_FILENAME = "messages.db";

  public ChatRepository(){
    db = new LocalDatabase();
  }

  public boolean prepareRepository(){
    if(db.createNewDatabase(DB_FILENAME)){
      String sql = "CREATE TABLE IF NOT EXISTS message ("
              + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
              + "	user text,"
              + "	content text"
              + ");";

      if( db.execute(sql) ){
        return true;
      }
    }
    return false;
  }


  public void addMessage(UserMessage msg){
    String sql = "insert into message(user, content) values(?,?);";
    this.db.execute(sql, msg.getUser(), msg.getMessage());
  }

  public List<UserMessage> getLastMessages(int size){

    String sql = String.format("select user, content from message order by id desc limit %d;", size);
    
    return this.db.query(sql, null, rs ->{
      try {
        return new UserMessage(
               rs.getString("user"),
               rs.getString("content")
        );
      } catch (SQLException e) {
        log.error("errot at transform UserMessage", e);
        return null;
      }
    });
  }
}

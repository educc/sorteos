package com.ecacho.sorteos.web.chat;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class LocalDatabase {

  private Connection connection;

  /**
   * Connect a database with filename, if not exists it's create one.
   * @param fileName
   * @return true if database is created and connected
   */
  public boolean createNewDatabase(String fileName) {

    String url = "jdbc:sqlite:" + fileName;

    try {
        this.connection = DriverManager.getConnection(url);
        DatabaseMetaData meta = this.connection.getMetaData();
        log.info("The driver name is " + meta.getDriverName());
        log.info("A new database has been created.");
        return true;
    } catch (SQLException e) {
      log.error("Error at create new chat database.", e.getMessage());
    }
    return false;
  }


  /**
   * Execute a sql command
   * @param sql
   * @return true if no error found during execute
   */
  public boolean execute(String sql) {

    try {
      Statement stmt = this.connection.createStatement();
      stmt.execute(sql);
      return true;
    } catch (SQLException e) {
      log.error("Error at execute sql command = " + sql,e.getMessage());
    }
    return false;
  }

  /**
   * Execute a sql command with params
   * @param sql
   * @param params
   * @return true if no error found during execute
   */

  public boolean execute(String sql, String... params){
    try {
      PreparedStatement stmt  = this.connection.prepareStatement(sql);
      int i = 1;
      for(String item: params){
        stmt.setString(i++, item);
      }
      return stmt.execute();
    } catch (SQLException e) {
      log.error("Error at execute sql command: " + sql,e.getMessage());
    }
    return false;
  }

  public List query(String sql, List params, Function<ResultSet, Object> fun){
    List result = new LinkedList();
    try {
         PreparedStatement stmt  = this.connection.prepareStatement(sql);

         if(params != null){
           int i = 1;
           for(Object item: params){
             stmt.setObject(i++, item);
           }
         }
         ResultSet rs = stmt.executeQuery();
      // loop through the result set
      while (rs.next()) {
        Object newItem = fun.apply(rs);
        if(newItem != null){
          result.add(newItem);
        }
      }
    } catch (SQLException e) {
      log.error("Error at query: " + sql,e.getMessage());
    }
    return result;
  }
}

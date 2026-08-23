package com.unicauca.lisw2.access;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


// abrir /  reutilizar la conexion a la db


public final class DatabaseConnection{



  private static final String DB_URL = "jdbc:sqlite:taller2.db";
  private static Connection connection = null;


  private DatabaseConnection(){}


  public static Connection getConnection(){

    try {
      if(connection == null || connection.isClosed()){
        connection = DriverManager.getConnection(DB_URL);
        initializeSchema(connection);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error al conectar a la base de datos", e);
    }

    return connection;
  }


  private static void initializeSchema(Connection connection) throws SQLException{


    String sql = "CREATE TABLE IF NOT EXISTS users(" +
    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
    "login TEXT NOT NULL UNIQUE," +
    "full_name TEXT NOT NULL," +
    "role TEXT NOT NULL," +
    "status TEXT NOT NULL," +
    "hashed_password TEXT NOT NULL" +
    ");";

    try (var stmt = connection.createStatement()) {
      stmt.execute(sql);
    }


  }


}

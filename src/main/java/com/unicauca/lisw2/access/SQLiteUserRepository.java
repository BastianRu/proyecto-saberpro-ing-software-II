package com.unicauca.lisw2.access;
 
import com.unicauca.lisw2.domain.Role;
import com.unicauca.lisw2.domain.User;
import com.unicauca.lisw2.domain.UserStatus;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// implementaion de IUserRepository usando JDB para SQLite

// Es la unica clase que sabe escirbir sql para usuarios



public class SQLiteUserRepository implements IUserRepository {


  private final Connection connection;

  public SQLiteUserRepository(Connection connection) {
    this.connection = connection;
  }



  @Override
  public User save(User user){
    String sql = "INSERT INTO users (login, full_name, role, status, hashed_password)" +
            " VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, user.getLogin());
      ps.setString(2, user.getFullName());
      ps.setString(3, user.getRole().name());
      ps.setString(4, user.getStatus().name());
      ps.setString(5, user.getHashedPassword());
      ps.executeUpdate();

      try(ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          user.setId(generatedKeys.getLong(1));
        }
      }
      return user;
    }catch (SQLException e) {
      throw new RuntimeException("Error al guardar el usuario: " + user.getLogin(), e);
    }        
  }



  @Override
  public Optional<User> findByLogin(String login){
    String sql = "SELECT * FROM users WHERE login = ?";
    try(PreparedStatement ps = connection.prepareStatement(sql)){
      ps.setString(1, sql);

      try(ResultSet rs = ps.executeQuery()){
        if(rs.next()){
          return Optional.of(mapRow(rs))
        }
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Error al buscar el usuario por login: " + login, e);
    }
  }




  @Override
  public List<User> findAll() {

    String sql  = "SELECT * FROM users";
    List<User> users = new ArrayList<>();
    try(Statement stmt = connection.createStatement();
    ResultSet rs = stmt.executeQuery(sql)){

      while(rs.next()){
        users.add(mapRow(rs));
      }

    } catch (SQLException e) {
      throw new RuntimeException("Error al obtener todos los usuarios", e);
    }
    return users;

  }



  @Override 
  public boolean existByLogin(String login){
    return findByLogin(login).isPresent();
  }


  @Override
  public void update(User user){
    String sql = "UPDATE users SET full_name = ?, role = ?, status = ?, hashed_password = ?" +
            " WHERE login = ?";

    try (PreparedStatement ps = connection.prepareStatement(sql)){
      ps.setString(1, user.getFullName());
      ps.setString(2, user.getRole().name());
      ps.setString(3, user.getStatus().name());
      ps.setString(4, user.getHashedPassword());
      ps.setString(5, user.getLogin());
      ps.executeUpdate();
    }    catch (SQLException e) {
      throw new RuntimeException("Error al actualizar el usuario: " + user.getLogin(), e);
    }    

  }


  //Convierte una fila del ResultSet en un objeto User


  private User mapRow(ResultSet rs) throws SQLException {
     User user = new User(
                rs.getString("login"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                UserStatus.valueOf(rs.getString("status")),
                rs.getString("hashed_password")
        );
        user.setId(rs.getLong("id"));
        return user;
  }

}

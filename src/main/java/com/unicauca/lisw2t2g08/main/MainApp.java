package com.unicauca.lisw2t2g08.main;

import java.sql.Connection;

import com.unicauca.lisw2t2g08.access.Argon2PasswordHasher;
import com.unicauca.lisw2t2g08.access.DatabaseConnection;
import com.unicauca.lisw2t2g08.access.IUserRepository;
import com.unicauca.lisw2t2g08.access.SQLiteUserRepository;
import com.unicauca.lisw2t2g08.domain.IPasswordHasher;
import com.unicauca.lisw2t2g08.service.IUserService;
import com.unicauca.lisw2t2g08.service.UserService;
import com.unicauca.lisw2t2g08.ui.LoginView;

import javafx.application.Application;
import javafx.stage.Stage;


//Punto de partida de la app

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Connection connection = DatabaseConnection.getConnection();
        IUserRepository userRepository = new SQLiteUserRepository(connection);
        IPasswordHasher passwordHasher = new Argon2PasswordHasher();
        IUserService userService = new UserService(userRepository, passwordHasher);

        primaryStage.setTitle("Taller 2 - Gestión de Usuarios (SOLID)");
        LoginView loginView = new LoginView(primaryStage, userService);
        primaryStage.setScene(loginView.getScene());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.unicauca.lisw2t2g08.main;

import java.sql.Connection;

import com.unicauca.lisw2t2g08.access.Argon2PasswordHasher;
import com.unicauca.lisw2t2g08.access.DatabaseConnection;
import com.unicauca.lisw2t2g08.access.IQuestionRepository;
import com.unicauca.lisw2t2g08.access.IUserRepository;
import com.unicauca.lisw2t2g08.access.QuestionSampleData;
import com.unicauca.lisw2t2g08.access.SQLiteQuestionRepository;
import com.unicauca.lisw2t2g08.access.SQLiteUserRepository;
import com.unicauca.lisw2t2g08.domain.IPasswordHasher;
import com.unicauca.lisw2t2g08.service.IUserService;
import com.unicauca.lisw2t2g08.service.QuestionService;
import com.unicauca.lisw2t2g08.service.UserService;
import com.unicauca.lisw2t2g08.ui.LoginView;
import com.unicauca.lisw2t2g08.ui.QuestionBankWindows;
import com.unicauca.lisw2t2g08.ui.QuestionController;

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

        // Banco de preguntas: QuestionService es el modelo del MVC y el sujeto
        // observable; se usa el tipo concreto solo aqui para registrar observadores.
        IQuestionRepository questionRepository = new SQLiteQuestionRepository(connection);
        QuestionSampleData.loadIfEmpty(questionRepository);
        QuestionService questionService = new QuestionService(questionRepository);
        QuestionController questionController = new QuestionController(questionService);
        QuestionBankWindows questionBank = new QuestionBankWindows(questionController, questionService);

        primaryStage.setTitle("Aplicación de Banco de Preguntas");
        LoginView loginView = new LoginView(primaryStage, userService, questionBank);
        primaryStage.setScene(loginView.getScene());
        // Al cerrar la ventana principal se cierran tambien las del banco de preguntas
        primaryStage.setOnHidden(e -> questionBank.close());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

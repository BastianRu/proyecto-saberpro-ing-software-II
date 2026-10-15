package com.unicauca.lisw2t2g08.ui;
 
import java.util.List;

import com.unicauca.lisw2t2g08.domain.Role;
import com.unicauca.lisw2t2g08.domain.User;
import com.unicauca.lisw2t2g08.service.IUserService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Menu que se muestra tras un login exitoso

public class MainMenuView {

  private final Stage stage;
  private final User user;
  private final IUserService userService;
  private final QuestionBankWindows questionBank;
  private final Scene scene;


  public MainMenuView(Stage stage, User user, IUserService userService, QuestionBankWindows questionBank){
    this.stage = stage;
    this.user = user;
    this.userService = userService;
    this.questionBank = questionBank;
    this.scene = buildScene();
  }

  public Scene getScene() {
    return scene;
  }


  private Scene buildScene(){

    Label welcome = new Label("Bienvenido, " + user.getFullName() + " (" + user.getRole() + ")");
    welcome.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");


    VBox layout  = new VBox(10, welcome);
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(20));


    for(String option : optionsForRole(user.getRole())){
      Button optionButton  = new Button(option);
      optionButton.setDisable(true);
      layout.getChildren().add(optionButton);
    }


    if (canManageQuestions(user.getRole())) {
      Button questionBankButton = new Button("Banco de preguntas");
      questionBankButton.setOnAction(e -> {
        questionBank.setCurrentUser(user);
        questionBank.show();
      });
      layout.getChildren().add(questionBankButton);
    }


    Button logoutButton = new Button("Cerrar sesión");
    logoutButton.setOnAction(e -> {
      questionBank.close();
      LoginView loginView = new LoginView(stage, userService, questionBank);
      stage.setScene(loginView.getScene());
    });
    layout.getChildren().add(logoutButton);

    return new Scene(layout, 400, 300);
  }

  // Roles que gestionan el estado de las preguntas del banco
  private boolean canManageQuestions(Role role) {
    return role == Role.ADMINISTRADOR || role == Role.AUTOR_PREGUNTAS || role == Role.REVISOR;
  }

  private List<String> optionsForRole(Role role) {
        switch (role) {
            case ADMINISTRADOR:
                return List.of("Gestionar usuarios", "Ver reportes", "Configuración del sistema");
            case AUTOR_PREGUNTAS:
                return List.of("Crear pregunta", "Mis preguntas");
            case REVISOR:
                return List.of("Preguntas pendientes de revisión");
            case DOCENTE:
                return List.of("Mis cursos", "Generar examen");
            case ESTUDIANTE:
                return List.of("Mis exámenes disponibles", "Mi historial");
            default:
                return List.of();
        }
    }
}
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
  private final Scene scene;


  public MainMenuView(Stage stage, User user, IUserService userService){
    this.stage = stage;
    this.user = user;
    this.userService = userService;
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


    Button logoutButton = new Button("Cerrar sesión");
    logoutButton.setOnAction(e -> {
      LoginView loginView = new LoginView(stage, userService);
      stage.setScene(loginView.getScene());
    });
    layout.getChildren().add(logoutButton);

    return new Scene(layout, 400, 300);
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
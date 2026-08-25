package com.unicauca.lisw2.ui;
 
import com.unicauca.lisw2.domain.User;
import com.unicauca.lisw2.service.AuthenticationException;
import com.unicauca.lisw2.service.IUserService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


// Pantalla de inicio de sesion


public class LoginView {
  

  private final Stage stage;
  private final IUserService userService;
  private final Scene scene;



  public LoginView(Stage stage, IUserService userService){
    this.stage = stage;
    this.userService = userService;
    this.scene = buildScene();
  }

  public Scene getScene() {
    return scene;
  }




  private Scene buildScene(){

    Label title  = new Label("Iniciar Sesion");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

    TextField loginField = new TextField();
    loginField.setPromptText("Usuario login");

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Contraseña");

    Label errorLabel = new Label();
    errorLabel.setStyle("-fx-text-fill: red;");
    errorLabel.setWrapText(true);

    Button loginButton = new Button("Iniciar Sesion");
    loginButton.setOnAction(event -> {
      try{
        User user  = userService.login(loginField.getText(), passwordField.getText());
        MainMenuView menuView = new MainMenuView(stage, user, userService);
        stage.setScene(menuView.getScene());
      } catch (AuthenticationException e) {
        errorLabel.setText(e.getMessage());
      }
    });

    Button goToRegisterButton = new Button("¿No tienes cuenta? Regístrate");
        goToRegisterButton.setOnAction(e -> {
         RegisterView registerView = new RegisterView(stage, userService);
         stage.setScene(registerView.getScene());
        });

     VBox layout = new VBox(12, title, loginField, passwordField, loginButton, errorLabel, goToRegisterButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
 
        return new Scene(layout, 400, 350);
  }



}

package com.unicauca.lisw2t2g08.ui;
 
import com.unicauca.lisw2t2g08.domain.Session;
import com.unicauca.lisw2t2g08.service.AuthServiceUnavailableException;
import com.unicauca.lisw2t2g08.service.AuthenticationException;
import com.unicauca.lisw2t2g08.service.IAuthClient;

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
  private final IAuthClient authClient;
  private final QuestionBankWindows questionBank;
  private final Scene scene;



  public LoginView(Stage stage, IAuthClient authClient, QuestionBankWindows questionBank){
    this.stage = stage;
    this.authClient = authClient;
    this.questionBank = questionBank;
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
        Session session = authClient.login(loginField.getText(), passwordField.getText());
        MainMenuView menuView = new MainMenuView(stage, session, authClient, questionBank);
        stage.setScene(menuView.getScene());
      } catch (AuthenticationException | AuthServiceUnavailableException e) {
        errorLabel.setText(e.getMessage());
      }
    });

    Button goToRegisterButton = new Button("¿No tienes cuenta? Regístrate");
        goToRegisterButton.setOnAction(e -> {
         RegisterView registerView = new RegisterView(stage, authClient, questionBank);
         stage.setScene(registerView.getScene());
        });

     VBox layout = new VBox(12, title, loginField, passwordField, loginButton, errorLabel, goToRegisterButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
 
        return new Scene(layout, 400, 350);
  }



}

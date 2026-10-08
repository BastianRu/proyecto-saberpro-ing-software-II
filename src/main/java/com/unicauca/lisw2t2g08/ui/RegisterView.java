package com.unicauca.lisw2t2g08.ui;
 
import com.unicauca.lisw2t2g08.domain.Role;
import com.unicauca.lisw2t2g08.service.IUserService;
import com.unicauca.lisw2t2g08.service.InvalidPasswordException;
import com.unicauca.lisw2t2g08.service.UserAlreadyExistsException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


// Pantalla de registro de usuario. 


public class RegisterView {
  

  private final Stage stage;
  private final IUserService userService;
  private final QuestionBankWindows questionBank;
  private final Scene scene;



  public RegisterView(Stage stage, IUserService userService, QuestionBankWindows questionBank){
    this.stage = stage;
    this.userService = userService;
    this.questionBank = questionBank;
    this.scene = buildScene();
  }


  public Scene getScene() {
    return scene;
  }


  private Scene buildScene() {
        Label title = new Label("Registro de usuario");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
 
        TextField loginField = new TextField();
        loginField.setPromptText("Usuario (login)");
 
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Nombre completo");
 
        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(Role.values());
        roleCombo.setPromptText("Rol");
 
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña (min. 6, 1 dígito, 1 mayúscula, 1 especial)");
 
        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(340);
 
        Button registerButton = new Button("Registrar");
        registerButton.setOnAction(e -> handleRegister(
                loginField, fullNameField, roleCombo, passwordField, messageLabel));
 
        Button backButton = new Button("Volver a inicio de sesión");
        backButton.setOnAction(e -> {
            LoginView loginView = new LoginView(stage, userService, questionBank);
            stage.setScene(loginView.getScene());
        });
 
        VBox layout = new VBox(12, title, loginField, fullNameField, roleCombo, passwordField,
                registerButton, messageLabel, backButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
 
        return new Scene(layout, 420, 460);
     }



     private void handleRegister(TextField loginField, TextField fullNameField, ComboBox<Role> roleCombo, PasswordField passwordField, Label messageLabel){

      Role selectedRole = roleCombo.getValue();
      if(selectedRole == null){
        messageLabel.setText("Por favor seleccione un rol.");
        return;
      }

     try {
            userService.register(loginField.getText(), fullNameField.getText(),
                    selectedRole, passwordField.getText());
            showSuccess(messageLabel, "Usuario registrado con éxito. Ya puedes iniciar sesión.");
        } catch (UserAlreadyExistsException ex) {
            showError(messageLabel, ex.getMessage());
        } catch (InvalidPasswordException ex) {
            showError(messageLabel, String.join("\n", ex.getErrors()));
        } catch (IllegalArgumentException ex) {
            // Capturamos los invariantes de la entidad User (login/nombre vacíos, etc.)
            showError(messageLabel, ex.getMessage());
        }

     }



     private void showError(Label messageLabel, String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);

     }


      private void showSuccess(Label messageLabel, String message) {
          messageLabel.setStyle("-fx-text-fill: green;");
          messageLabel.setText(message);
      }


  }

package Controllers.user;

import Models.User;
import Services.UserService;
import Controllers.BasefrontController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button forgotPasswordButton;

    @FXML
    private Label errorLabel;
    
    @FXML
    private Label emailErrorLabel;
    
    @FXML
    private Label passwordErrorLabel;

    private final UserService userService = new UserService();
    
    @FXML
    void initialize() {
        setupValidationListeners();
    }
    
    private void setupValidationListeners() {
        // Validation de l'email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                emailErrorLabel.setText("L'email est requis");
                emailErrorLabel.setTextFill(Color.RED);
                emailField.setStyle("-fx-border-color: red;");
            } else if (!newValue.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailErrorLabel.setText("Format d'email invalide");
                emailErrorLabel.setTextFill(Color.RED);
                emailField.setStyle("-fx-border-color: red;");
            } else {
                emailErrorLabel.setText("✓");
                emailErrorLabel.setTextFill(Color.GREEN);
                emailField.setStyle("-fx-border-color: green;");
            }
        });
        
        // Validation du mot de passe
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                passwordErrorLabel.setText("Le mot de passe est requis");
                passwordErrorLabel.setTextFill(Color.RED);
                passwordField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 6) {
                passwordErrorLabel.setText("Le mot de passe doit contenir au moins 6 caractères");
                passwordErrorLabel.setTextFill(Color.RED);
                passwordField.setStyle("-fx-border-color: red;");
            } else {
                passwordErrorLabel.setText("✓");
                passwordErrorLabel.setTextFill(Color.GREEN);
                passwordField.setStyle("-fx-border-color: green;");
            }
        });
    }

    @FXML
    void handleLogin(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        try {
            User user = userService.authenticate(emailField.getText(), passwordField.getText());
            if (user != null) {
                boolean rememberMe = rememberMeCheckBox != null && rememberMeCheckBox.isSelected();
                
                if (rememberMe) {
                    // TODO: Implement remember me functionality
                    System.out.println("Remember me selected");
                }

                redirectToHomeWithUser(user);
            } else {
                errorLabel.setText("Email ou mot de passe incorrect");
                emailField.setStyle("-fx-border-color: red;");
                passwordField.setStyle("-fx-border-color: red;");
            }
        } catch (Exception e) {
            errorLabel.setText("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean validateInput() {
        boolean isValid = true;
        
        // Validation de l'email
        if (emailField.getText().trim().isEmpty()) {
            emailErrorLabel.setText("L'email est requis");
            emailErrorLabel.setTextFill(Color.RED);
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailErrorLabel.setText("Format d'email invalide");
            emailErrorLabel.setTextFill(Color.RED);
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validation du mot de passe
        if (passwordField.getText().trim().isEmpty()) {
            passwordErrorLabel.setText("Le mot de passe est requis");
            passwordErrorLabel.setTextFill(Color.RED);
            passwordField.setStyle("-fx-border-color: red;");
            isValid = false;
        } else if (passwordField.getText().length() < 6) {
            passwordErrorLabel.setText("Le mot de passe doit contenir au moins 6 caractères");
            passwordErrorLabel.setTextFill(Color.RED);
            passwordField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        return isValid;
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/user/registration.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la redirection vers la page d'inscription");
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/user/forgetpassword.fxml"));
            Stage stage = (Stage) forgotPasswordButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la redirection vers la page de récupération de mot de passe");
        }
    }

    private void redirectToHomeWithUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/basefront.fxml"));
            Parent root = loader.load();

            BasefrontController controller = loader.getController();
            controller.setCurrentUser(user);
            
            controller.navigateTo("/Views/home.fxml");

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
            
            System.out.println("Connexion réussie et redirection vers la page d'accueil");
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la redirection vers la page d'accueil: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
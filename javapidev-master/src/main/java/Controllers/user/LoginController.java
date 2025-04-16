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

    private final UserService userService = new UserService();

    @FXML
    void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        try {
            User user = userService.authenticate(email, password);
            if (user != null) {
                // Store remember me preference if needed
                boolean rememberMe = false;
                if (rememberMeCheckBox != null) {
                    rememberMe = rememberMeCheckBox.isSelected();
                }
                
                if (rememberMe) {
                    // TODO: Implement remember me functionality
                    System.out.println("Remember me selected");
                }

                // Redirect to home page with user
                redirectToHomeWithUser(user);
            } else {
                errorLabel.setText("Invalid email or password");
            }
        } catch (Exception e) {
            errorLabel.setText("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/user/registration.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Error redirecting to registration page");
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/user/forgetpassword.fxml"));
            Stage stage = (Stage) forgotPasswordButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Error redirecting to forgot password page");
        }
    }

    private void redirectToHomeWithUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/basefront.fxml"));
            Parent root = loader.load();

            // Get the controller and set the user
            BasefrontController controller = loader.getController();
            controller.setCurrentUser(user);
            
            // Redirect to home view
            controller.navigateTo("/Views/home.fxml");

            // Set the scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
            
            System.out.println("Successfully logged in and redirected to home page");
        } catch (IOException e) {
            errorLabel.setText("Error redirecting to home page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package Controllers.user;

import Models.User;
import Services.UserService;
import Controllers.BasefrontController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class RegistrationController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField numtlfField;

    @FXML
    private TextField ageField;

    @FXML
    private TextField avatarUrlField;

    @FXML
    private ComboBox<String> rolesComboBox;

    @FXML
    private CheckBox isVerifiedCheckBox;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();
    private File selectedAvatarFile;

    @FXML
    void initialize() {
        rolesComboBox.getItems().setAll(
                "ROLE_USER",
                "ROLE_ADMIN",
                "ROLE_ARTIST"
        );
        rolesComboBox.setValue("ROLE_USER");
    }

    @FXML
    void handleBrowseAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Avatar Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedAvatarFile = fileChooser.showOpenDialog(avatarUrlField.getScene().getWindow());
        if (selectedAvatarFile != null) {
            avatarUrlField.setText(selectedAvatarFile.getName());
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        try {
            if (validateInput()) {
                String avatarUrl = null;

                if (selectedAvatarFile != null) {
                    Path uploadsPath = Paths.get("src/main/resources/uploads");
                    if (!Files.exists(uploadsPath)) {
                        Files.createDirectories(uploadsPath);
                    }

                    String fileName = System.currentTimeMillis() + "_" + selectedAvatarFile.getName();
                    Path targetPath = uploadsPath.resolve(fileName);
                    Files.copy(selectedAvatarFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    avatarUrl = "uploads/" + fileName;
                }

                String selectedRole = rolesComboBox.getValue();
                String rolesJson = "[\"" + selectedRole + "\"]";
                if (selectedRole.equals("ROLE_ADMIN")) {
                    rolesJson = "[\"ROLE_USER\",\"ROLE_ADMIN\"]";
                }

                User user = new User(
                        nomField.getText(),
                        prenomField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        numtlfField.getText(),
                        Integer.parseInt(ageField.getText()),
                        avatarUrl,
                        rolesJson,
                        isVerifiedCheckBox.isSelected()
                );

                userService.insert(user);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText(null);
                alert.setContentText("Your account has been created successfully!");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        redirectToHomeWithUser(user);
                    }
                });
            }
        } catch (Exception e) {
            errorLabel.setText("Error during registration: " + e.getMessage());
        }
    }

    private void redirectToHomeWithUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/basefront.fxml"));
            Parent root = loader.load();

            BasefrontController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Error redirecting to home page: " + e.getMessage());
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/user/login.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Error redirecting to login page");
        }
    }

    private boolean validateInput() {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || passwordField.getText().isEmpty() ||
                numtlfField.getText().isEmpty() || ageField.getText().isEmpty() ||
                rolesComboBox.getValue() == null) {
            errorLabel.setText("Please fill in all required fields");
            return false;
        }

        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 0 || age > 120) {
                errorLabel.setText("Please enter a valid age");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Age must be a number");
            return false;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Please enter a valid email address");
            return false;
        }

        return true;
    }
}
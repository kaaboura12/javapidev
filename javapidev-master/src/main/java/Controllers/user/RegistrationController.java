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
import javafx.scene.paint.Color;

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
    
    @FXML
    private Label nomErrorLabel;
    
    @FXML
    private Label prenomErrorLabel;
    
    @FXML
    private Label emailErrorLabel;
    
    @FXML
    private Label passwordErrorLabel;
    
    @FXML
    private Label numtlfErrorLabel;
    
    @FXML
    private Label ageErrorLabel;

    private final UserService userService = new UserService();
    private File selectedAvatarFile;

    @FXML
    void initialize() {
        rolesComboBox.getItems().setAll(
                "ROLE_USER",
                "ROLE_ARTIST"
        );
        rolesComboBox.setValue("ROLE_USER");
        
        setupValidationListeners();
    }
    
    private void setupValidationListeners() {
        // Validation du nom
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                nomErrorLabel.setText("Le nom est requis");
                nomErrorLabel.setTextFill(Color.RED);
                nomField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 2) {
                nomErrorLabel.setText("Le nom doit contenir au moins 2 caractères");
                nomErrorLabel.setTextFill(Color.RED);
                nomField.setStyle("-fx-border-color: red;");
            } else {
                nomErrorLabel.setText("✓");
                nomErrorLabel.setTextFill(Color.GREEN);
                nomField.setStyle("-fx-border-color: green;");
            }
        });
        
        // Validation du prénom
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                prenomErrorLabel.setText("Le prénom est requis");
                prenomErrorLabel.setTextFill(Color.RED);
                prenomField.setStyle("-fx-border-color: red;");
            } else if (newValue.length() < 2) {
                prenomErrorLabel.setText("Le prénom doit contenir au moins 2 caractères");
                prenomErrorLabel.setTextFill(Color.RED);
                prenomField.setStyle("-fx-border-color: red;");
            } else {
                prenomErrorLabel.setText("✓");
                prenomErrorLabel.setTextFill(Color.GREEN);
                prenomField.setStyle("-fx-border-color: green;");
            }
        });
        
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
            } else if (!newValue.matches(".*[A-Z].*") || !newValue.matches(".*[a-z].*") || !newValue.matches(".*\\d.*")) {
                passwordErrorLabel.setText("Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre");
                passwordErrorLabel.setTextFill(Color.RED);
                passwordField.setStyle("-fx-border-color: red;");
            } else {
                passwordErrorLabel.setText("✓");
                passwordErrorLabel.setTextFill(Color.GREEN);
                passwordField.setStyle("-fx-border-color: green;");
            }
        });
        
        // Validation du numéro de téléphone
        numtlfField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                numtlfErrorLabel.setText("Le numéro de téléphone est requis");
                numtlfErrorLabel.setTextFill(Color.RED);
                numtlfField.setStyle("-fx-border-color: red;");
            } else if (!newValue.matches("\\d+") || newValue.length() != 8) {
                numtlfErrorLabel.setText("Le numéro doit contenir 8 chiffres");
                numtlfErrorLabel.setTextFill(Color.RED);
                numtlfField.setStyle("-fx-border-color: red;");
            } else {
                numtlfErrorLabel.setText("✓");
                numtlfErrorLabel.setTextFill(Color.GREEN);
                numtlfField.setStyle("-fx-border-color: green;");
            }
        });
        
        // Validation de l'âge
        ageField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().isEmpty()) {
                    ageErrorLabel.setText("L'âge est requis");
                    ageErrorLabel.setTextFill(Color.RED);
                    ageField.setStyle("-fx-border-color: red;");
                } else {
                    int age = Integer.parseInt(newValue);
                    if (age < 13 || age > 120) {
                        ageErrorLabel.setText("L'âge doit être entre 13 et 120 ans");
                        ageErrorLabel.setTextFill(Color.RED);
                        ageField.setStyle("-fx-border-color: red;");
                    } else {
                        ageErrorLabel.setText("✓");
                        ageErrorLabel.setTextFill(Color.GREEN);
                        ageField.setStyle("-fx-border-color: green;");
                    }
                }
            } catch (NumberFormatException e) {
                ageErrorLabel.setText("L'âge doit être un nombre");
                ageErrorLabel.setTextFill(Color.RED);
                ageField.setStyle("-fx-border-color: red;");
            }
        });
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
                alert.setTitle("Inscription réussie");
                alert.setHeaderText(null);
                alert.setContentText("Votre compte a été créé avec succès!");
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        redirectToHomeWithUser(user);
                    }
                });
            }
        } catch (Exception e) {
            errorLabel.setText("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean isValid = true;
        
        // Validation du nom
        if (nomField.getText().trim().isEmpty() || nomField.getText().length() < 2) {
            isValid = false;
        }
        
        // Validation du prénom
        if (prenomField.getText().trim().isEmpty() || prenomField.getText().length() < 2) {
            isValid = false;
        }
        
        // Validation de l'email
        if (emailField.getText().trim().isEmpty() || !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            isValid = false;
        }
        
        // Validation du mot de passe
        String password = passwordField.getText();
        if (password.trim().isEmpty() || password.length() < 6 || 
            !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            isValid = false;
        }
        
        // Validation du numéro de téléphone
        if (numtlfField.getText().trim().isEmpty() || !numtlfField.getText().matches("\\d{8}")) {
            isValid = false;
        }
        
        // Validation de l'âge
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 13 || age > 120) {
                isValid = false;
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }
        
        if (!isValid) {
            errorLabel.setText("Veuillez remplir le formulaire");
        }
        
        return isValid;
    }

    @FXML
    void handleLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Views/user/login.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la redirection vers la page de connexion");
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
            errorLabel.setText("Erreur lors de la redirection vers la page d'accueil: " + e.getMessage());
        }
    }
}
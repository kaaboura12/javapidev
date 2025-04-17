package Controllers.backuser;

import Models.User;
import Services.UserService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.scene.paint.Color;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListUserController implements Initializable {

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label adminUsersLabel;

    @FXML
    private Label artistUsersLabel;

    @FXML
    private Label verifiedUsersLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addUserButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nomColumn;

    @FXML
    private TableColumn<User, String> prenomColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, Integer> ageColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Boolean> verifiedColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private AnchorPane popupOverlay;

    @FXML
    private VBox userFormContainer;

    @FXML
    private Label formTitleLabel;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

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
    private Label formErrorLabel;

    @FXML
    private Button saveButton;

    @FXML
    private AnchorPane deleteConfirmationOverlay;

    @FXML
    private Label deleteConfirmationText;

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

    private UserService userService;
    private ObservableList<User> usersList;
    private FilteredList<User> filteredUsers;
    private User currentUser; // For editing
    private User userToDelete; // For deletion confirmation
    private File selectedAvatarFile;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        
        // Initialize roles combo box
        rolesComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN", "ROLE_ARTIST");
        rolesComboBox.setValue("ROLE_USER");
        
        // Setup table columns
        setupTableColumns();
        
        // Load users
        loadUsers();
        
        // Setup search functionality
        setupSearch();
        
        // Update statistics
        updateStatistics();
        
        // Setup form validation
        setupValidationListeners();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prenomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrenom()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        ageColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNumtlf()));
        roleColumn.setCellValueFactory(cellData -> {
            String roles = cellData.getValue().getRoles();
            if (roles.contains("ROLE_ADMIN")) {
                return new SimpleStringProperty("Admin");
            } else if (roles.contains("ROLE_ARTIST")) {
                return new SimpleStringProperty("Artist");
            } else {
                return new SimpleStringProperty("User");
            }
        });
        verifiedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isVerified()));
        
        // Format the verified column to show checkmarks
        verifiedColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item ? "✓" : "✗");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red;");
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        // Setup actions column with edit and delete buttons
        actionsColumn.setCellFactory(createActionsColumnCallback());
    }
    
    private Callback<TableColumn<User, Void>, TableCell<User, Void>> createActionsColumnCallback() {
        return new Callback<>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox pane = new HBox(5, editButton, deleteButton);
                    
                    {
                        editButton.getStyleClass().add("edit-button");
                        deleteButton.getStyleClass().add("delete-button");
                        pane.setAlignment(Pos.CENTER);
                        
                        // Edit button action
                        editButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            openEditForm(user);
                        });
                        
                        // Delete button action
                        deleteButton.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            showDeleteConfirmation(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
    }
    
    private void loadUsers() {
        try {
            List<User> users = userService.findAll();
            usersList = FXCollections.observableArrayList(users);
            filteredUsers = new FilteredList<>(usersList, p -> true);
            usersTable.setItems(filteredUsers);
        } catch (Exception e) {
            showAlert("Error loading users", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
        
        searchButton.setOnAction(event -> filterUsers(searchField.getText()));
    }
    
    private void filterUsers(String searchText) {
        filteredUsers.setPredicate(user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            String lowerCaseSearch = searchText.toLowerCase();
            
            // Check if any field contains the search text
            return user.getNom().toLowerCase().contains(lowerCaseSearch) ||
                   user.getPrenom().toLowerCase().contains(lowerCaseSearch) ||
                   user.getEmail().toLowerCase().contains(lowerCaseSearch) ||
                   user.getNumtlf().toLowerCase().contains(lowerCaseSearch) ||
                   user.getRoles().toLowerCase().contains(lowerCaseSearch);
        });
    }
    
    private void updateStatistics() {
        int totalUsers = usersList.size();
        int adminUsers = 0;
        int artistUsers = 0;
        int verifiedUsers = 0;
        
        for (User user : usersList) {
            if (user.getRoles().contains("ROLE_ADMIN")) {
                adminUsers++;
            }
            if (user.getRoles().contains("ROLE_ARTIST")) {
                artistUsers++;
            }
            if (user.isVerified()) {
                verifiedUsers++;
            }
        }
        
        totalUsersLabel.setText(String.valueOf(totalUsers));
        adminUsersLabel.setText(String.valueOf(adminUsers));
        artistUsersLabel.setText(String.valueOf(artistUsers));
        verifiedUsersLabel.setText(String.valueOf(verifiedUsers));
    }
    
    @FXML
    void handleSearch(ActionEvent event) {
        filterUsers(searchField.getText());
    }
    
    @FXML
    void showAddUserDialog(ActionEvent event) {
        isEditMode = false;
        formTitleLabel.setText("Add New User");
        clearForm();
        popupOverlay.setVisible(true);
    }
    
    private void openEditForm(User user) {
        isEditMode = true;
        currentUser = user;
        formTitleLabel.setText("Edit User");
        
        // Populate form fields
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        passwordField.setText(""); // For security, don't display the password
        numtlfField.setText(user.getNumtlf());
        ageField.setText(String.valueOf(user.getAge()));
        avatarUrlField.setText(user.getAvatarUrl());
        
        // Set role
        if (user.getRoles().contains("ROLE_ADMIN")) {
            rolesComboBox.setValue("ROLE_ADMIN");
        } else if (user.getRoles().contains("ROLE_ARTIST")) {
            rolesComboBox.setValue("ROLE_ARTIST");
        } else {
            rolesComboBox.setValue("ROLE_USER");
        }
        
        isVerifiedCheckBox.setSelected(user.isVerified());
        
        popupOverlay.setVisible(true);
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
    void closeForm(ActionEvent event) {
        popupOverlay.setVisible(false);
        clearForm();
    }
    
    @FXML
    void saveUser(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        try {
            String avatarUrl = processAvatarUpload();
            
            String selectedRole = rolesComboBox.getValue();
            String rolesJson;
            
            if (selectedRole.equals("ROLE_ADMIN")) {
                rolesJson = "[\"ROLE_USER\",\"ROLE_ADMIN\"]";
            } else if (selectedRole.equals("ROLE_ARTIST")) {
                rolesJson = "[\"ROLE_USER\",\"ROLE_ARTIST\"]";
            } else {
                rolesJson = "[\"ROLE_USER\"]";
            }
            
            if (isEditMode) {
                // Update existing user
                currentUser.setNom(nomField.getText());
                currentUser.setPrenom(prenomField.getText());
                currentUser.setEmail(emailField.getText());
                
                // Only update password if provided
                if (!passwordField.getText().isEmpty()) {
                    currentUser.setPassword(passwordField.getText());
                }
                
                currentUser.setNumtlf(numtlfField.getText());
                currentUser.setAge(Integer.parseInt(ageField.getText()));
                
                // Only update avatar if new one was selected
                if (avatarUrl != null) {
                    currentUser.setAvatarUrl(avatarUrl);
                }
                
                currentUser.setRoles(rolesJson);
                currentUser.setVerified(isVerifiedCheckBox.isSelected());
                
                userService.update(currentUser);
                showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
            } else {
                // Create new user
                User newUser = new User(
                        nomField.getText(),
                        prenomField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        numtlfField.getText(),
                        Integer.parseInt(ageField.getText()),
                        avatarUrl != null ? avatarUrl : "default-avatar.png",
                        rolesJson,
                        isVerifiedCheckBox.isSelected()
                );
                
                userService.insert(newUser);
                showAlert("Success", "User created successfully", Alert.AlertType.INFORMATION);
            }
            
            // Refresh data
            loadUsers();
            updateStatistics();
            
            // Close form
            popupOverlay.setVisible(false);
            clearForm();
            
        } catch (Exception e) {
            showAlert("Error", "Error saving user: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    private String processAvatarUpload() {
        if (selectedAvatarFile == null) {
            return null;
        }
        
        try {
            Path uploadsPath = Paths.get("src/main/resources/uploads");
            if (!Files.exists(uploadsPath)) {
                Files.createDirectories(uploadsPath);
            }
            
            String fileName = System.currentTimeMillis() + "_" + selectedAvatarFile.getName();
            Path targetPath = uploadsPath.resolve(fileName);
            Files.copy(selectedAvatarFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return "uploads/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            if (!isEditMode && newValue.trim().isEmpty()) {
                passwordErrorLabel.setText("Le mot de passe est requis");
                passwordErrorLabel.setTextFill(Color.RED);
                passwordField.setStyle("-fx-border-color: red;");
            } else if (!newValue.trim().isEmpty()) {
                if (newValue.length() < 6) {
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
            } else {
                passwordErrorLabel.setText("");
                passwordField.setStyle("");
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

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validation du nom
        if (nomField.getText().trim().isEmpty() || nomField.getText().length() < 2) {
            nomErrorLabel.setText("Le nom doit contenir au moins 2 caractères");
            nomErrorLabel.setTextFill(Color.RED);
            nomField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validation du prénom
        if (prenomField.getText().trim().isEmpty() || prenomField.getText().length() < 2) {
            prenomErrorLabel.setText("Le prénom doit contenir au moins 2 caractères");
            prenomErrorLabel.setTextFill(Color.RED);
            prenomField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validation de l'email
        if (emailField.getText().trim().isEmpty() || !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailErrorLabel.setText("Format d'email invalide");
            emailErrorLabel.setTextFill(Color.RED);
            emailField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validation du mot de passe (seulement pour les nouveaux utilisateurs)
        if (!isEditMode || !passwordField.getText().trim().isEmpty()) {
            String password = passwordField.getText();
            if (password.trim().isEmpty()) {
                passwordErrorLabel.setText("Le mot de passe est requis");
                passwordErrorLabel.setTextFill(Color.RED);
                passwordField.setStyle("-fx-border-color: red;");
                isValid = false;
            } else if (password.length() < 6 || !password.matches(".*[A-Z].*") || 
                      !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
                passwordErrorLabel.setText("Le mot de passe doit contenir au moins 6 caractères, une majuscule, une minuscule et un chiffre");
                passwordErrorLabel.setTextFill(Color.RED);
                passwordField.setStyle("-fx-border-color: red;");
                isValid = false;
            }
        }
        
        // Validation du numéro de téléphone
        if (numtlfField.getText().trim().isEmpty() || !numtlfField.getText().matches("\\d{8}")) {
            numtlfErrorLabel.setText("Le numéro doit contenir 8 chiffres");
            numtlfErrorLabel.setTextFill(Color.RED);
            numtlfField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        // Validation de l'âge
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 13 || age > 120) {
                ageErrorLabel.setText("L'âge doit être entre 13 et 120 ans");
                ageErrorLabel.setTextFill(Color.RED);
                ageField.setStyle("-fx-border-color: red;");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            ageErrorLabel.setText("L'âge doit être un nombre");
            ageErrorLabel.setTextFill(Color.RED);
            ageField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        
        if (!isValid) {
            formErrorLabel.setText("Veuillez corriger les erreurs dans le formulaire");
            formErrorLabel.setVisible(true);
        } else {
            formErrorLabel.setVisible(false);
        }
        
        return isValid;
    }
    
    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        numtlfField.clear();
        ageField.clear();
        avatarUrlField.clear();
        rolesComboBox.setValue("ROLE_USER");
        isVerifiedCheckBox.setSelected(false);
        formErrorLabel.setVisible(false);
        formErrorLabel.setManaged(false);
        selectedAvatarFile = null;
        currentUser = null;
    }
    
    @FXML
    void showDeleteConfirmation(User user) {
        userToDelete = user;
        deleteConfirmationText.setText("Are you sure you want to delete user: " + user.getPrenom() + " " + user.getNom() + "?");
        deleteConfirmationOverlay.setVisible(true);
    }
    
    @FXML
    void cancelDelete(ActionEvent event) {
        deleteConfirmationOverlay.setVisible(false);
        userToDelete = null;
    }
    
    @FXML
    void confirmDelete(ActionEvent event) {
        if (userToDelete != null) {
            try {
                // Pass the entire User object to the delete method instead of just the ID
                userService.delete(userToDelete);
                
                // Refresh data
                loadUsers();
                updateStatistics();
                
                showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Error deleting user: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
            
            deleteConfirmationOverlay.setVisible(false);
            userToDelete = null;
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
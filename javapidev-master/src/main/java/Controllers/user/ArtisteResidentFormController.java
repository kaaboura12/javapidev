package Controllers;

import Models.ArtisteResident;
import Models.User;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Utils.DatabaseConnection;

public class ArtisteResidentFormController implements Initializable {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label userInfoLabel;
    
    @FXML
    private TextArea descriptionTextArea;
    
    @FXML
    private Button chooseFileButton;
    
    @FXML
    private Label fileNameLabel;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private VBox formContainer;
    
    private User currentUser;
    private File selectedFile;
    private final String UPLOAD_DIR = "uploads/portfolios/";
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Make sure upload directory exists
        createUploadDirectoryIfNotExists();
        
        // Initialize button actions
        chooseFileButton.setOnAction(this::handleChooseFile);
        submitButton.setOnAction(this::handleSubmit);
        cancelButton.setOnAction(this::handleCancel);
    }
    
    /**
     * Set the current user to populate form fields with user info
     * 
     * @param user The currently logged in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Update UI with user information
        if (user != null && userInfoLabel != null) {
            userInfoLabel.setText("Candidat: " + user.getPrenom() + " " + user.getNom());
        }
    }
    
    /**
     * Handle the file selection button click
     * 
     * @param event The action event
     */
    private void handleChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez votre portfolio");
        
        // Set filters for PDF files
        FileChooser.ExtensionFilter pdfFilter = 
                new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter imageFilter = 
                new FileChooser.ExtensionFilter("Image Files (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().addAll(pdfFilter, imageFilter);
        
        // Show open file dialog
        File file = fileChooser.showOpenDialog(chooseFileButton.getScene().getWindow());
        
        if (file != null) {
            // Check file size (limit to 10MB for example)
            if (file.length() > 10 * 1024 * 1024) {
                showAlert(Alert.AlertType.ERROR, "Erreur de fichier", 
                        "Le fichier est trop grand", 
                        "Veuillez sélectionner un fichier inférieur à 10MB.");
                return;
            }
            
            selectedFile = file;
            fileNameLabel.setText(file.getName());
        }
    }
    
    /**
     * Handle the submit button click to save the application
     * 
     * @param event The action event
     */
    private void handleSubmit(ActionEvent event) {
        if (validateForm()) {
            try {
                // Save the portfolio file
                String savedFilePath = saveFile();
                
                // Create ArtisteResident object
                ArtisteResident artisteResident = new ArtisteResident(
                        currentUser.getId(),
                        descriptionTextArea.getText(),
                        savedFilePath
                );
                
                // Save to database
                saveToDatabase(artisteResident);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Candidature soumise", 
                        "Votre candidature a été soumise avec succès", 
                        "Nous examinerons votre candidature et vous contacterons bientôt.");
                
                // Return to main page
                navigateBack();
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Erreur lors de la soumission de la candidature", 
                        e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handle the cancel button click to return to previous screen
     * 
     * @param event The action event
     */
    private void handleCancel(ActionEvent event) {
        navigateBack();
    }
    
    /**
     * Validate the form inputs
     * 
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm() {
        if (descriptionTextArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Formulaire incomplet", 
                    "Description manquante", 
                    "Veuillez décrire votre projet pour devenir un artiste résident.");
            return false;
        }
        
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "Formulaire incomplet", 
                    "Portfolio manquant", 
                    "Veuillez charger votre portfolio pour compléter votre candidature.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Save the uploaded file to the server
     * 
     * @return The path to the saved file
     * @throws IOException If there's an error saving the file
     */
    private String saveFile() throws IOException {
        if (selectedFile == null) {
            return null;
        }
        
        // Generate unique filename using UUID
        String fileExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        String relativePath = UPLOAD_DIR + newFileName;
        
        // Create absolute path
        Path targetPath = Paths.get(System.getProperty("user.dir"), relativePath);
        
        // Copy file to target location
        Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return relativePath;
    }
    
    /**
     * Save the artist resident application to the database
     * 
     * @param artisteResident The artist resident application object
     * @throws SQLException If there's an error executing the SQL
     */
    private void saveToDatabase(ArtisteResident artisteResident) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "INSERT INTO artiste_resident (user_id, description, portfolio_path, status) VALUES (?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, artisteResident.getUserId());
            pstmt.setString(2, artisteResident.getDescription());
            pstmt.setString(3, artisteResident.getPortfolioPath());
            pstmt.setString(4, artisteResident.getStatus());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to save artist resident application.");
            }
            
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Create the upload directory if it doesn't exist
     */
    private void createUploadDirectoryIfNotExists() {
        try {
            Path dirPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            System.err.println("Error creating upload directory: " + e.getMessage());
        }
    }
    
    /**
     * Navigate back to the previous screen
     */
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/basefront.fxml"));
            Parent root = loader.load();
            
            // Get controller and set current user
            BasefrontController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Set the scene
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
        } catch (IOException e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show an alert dialog with the specified parameters
     * 
     * @param type The alert type
     * @param title The alert window title
     * @param header The alert header text
     * @param content The alert content text
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
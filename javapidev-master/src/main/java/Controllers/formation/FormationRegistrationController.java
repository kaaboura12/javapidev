package Controllers.formation;

import Models.Formation;
import Models.Inscription;
import Models.User;
import Services.InscriptionService;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.io.File;

public class FormationRegistrationController {
    @FXML private Label formationTitleLabel;
    @FXML private Label formationDateLabel;
    @FXML private Label formationPriceLabel;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button confirmButton;
    
    private Formation formation;
    private InscriptionService inscriptionService;
    private UserService userService;
    private User currentUser;
    
    // Static user ID for testing
    private static final int TEST_USER_ID = 2; // Change this to match an existing user_id in your database

    @FXML
    public void initialize() {
        inscriptionService = new InscriptionService();
        userService = new UserService();
        
        try {
            // Get test user for payment details
            currentUser = userService.findById(TEST_USER_ID);
        } catch (SQLException e) {
            System.err.println("Error loading test user: " + e.getMessage());
        }
        
        confirmButton.disableProperty().bind(termsCheckBox.selectedProperty().not());
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        updateUI();
    }

    private void updateUI() {
        formationTitleLabel.setText(formation.getTitre());
        formationDateLabel.setText(String.format("From: %s To: %s", 
            formation.getDateDebut().toLocalDate(), 
            formation.getDateFin().toLocalDate()));
        formationPriceLabel.setText(String.format("Price: $%.2f", formation.getPrix()));
    }

    @FXML
    private void handleRegistration() {
        try {
            // Using static test user ID
            int testUserId = TEST_USER_ID;
            
            if (inscriptionService.checkIfAlreadyRegistered(testUserId, formation.getId())) {
                showAlert("Already Registered", "You are already registered for this formation.", Alert.AlertType.WARNING);
                return;
            }

            // Check if spots are available
            if (formation.getNbrpart() <= 0) {
                showAlert("Registration Failed", "No more spots available for this formation.", Alert.AlertType.ERROR);
                return;
            }

            // Proceed to payment if spots are available
            proceedToPayment();
            
        } catch (SQLException e) {
            if (e.getMessage().contains("No more spots available")) {
                showAlert("Registration Failed", "No more spots available for this formation.", Alert.AlertType.ERROR);
            } else {
                showAlert("Error", "Registration failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
            e.printStackTrace();
        }
    }
    
    /**
     * Open the payment form
     */
    private void proceedToPayment() {
        try {
            // Try multiple methods to locate the stripe.fxml file
            URL stripeResource = null;
            
            // Method 1: Using class resource (standard way)
            stripeResource = getClass().getResource("/Views/event/stripe.fxml");
            System.out.println("Method 1 - Standard resource path: " + stripeResource);
            
            // Method 2: Try with ClassLoader (absolute path from classpath root)
            if (stripeResource == null) {
                stripeResource = getClass().getClassLoader().getResource("Views/event/stripe.fxml");
                System.out.println("Method 2 - ClassLoader: " + stripeResource);
            }
            
            // Method 3: Try direct file access as a last resort
            if (stripeResource == null) {
                // Check multiple possible locations
                String[] possiblePaths = {
                    "c:/Users/alari/Downloads/pidev-java/javapidev-master/src/main/resources/Views/event/stripe.fxml",
                    "c:/Users/alari/Downloads/pidev-java/javapidev-master/javapidev-master/src/main/resources/Views/event/stripe.fxml",
                    "c:/Users/alari/Downloads/pidev-java/javapidev-master/target/classes/Views/event/stripe.fxml"
                };
                
                for (String path : possiblePaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        stripeResource = file.toURI().toURL();
                        System.out.println("Method 3 - Direct file access: " + stripeResource);
                        break;
                    }
                }
            }
            
            // If still not found, show error message with detailed information
            if (stripeResource == null) {
                String errorDetail = "Could not find stripe.fxml in any standard location. " +
                    "Please ensure the file exists in the correct path: /Views/event/stripe.fxml";
                System.err.println(errorDetail);
                showAlert("Error", errorDetail, Alert.AlertType.ERROR);
                return;
            }
            
            System.out.println("Successfully found stripe.fxml at: " + stripeResource);
            
            // Load the Stripe FXML 
            FXMLLoader loader = new FXMLLoader(stripeResource);
            Parent stripePane = loader.load();
            
            // Get the controller and set transaction details
            Controllers.event.StripeController stripeController = loader.getController();
            
            // Adapt event parameters to work with formation registration
            // We're reusing the Stripe controller by passing formation details as an "event"
            stripeController.setPaymentCompletionHandler(success -> {
                if (success) {
                    // Complete the registration process after successful payment
                    completeRegistration();
                }
            });
            
            // Set transaction details for the payment
            stripeController.setTransactionDetailsForFormation(
                    formation.getTitre(),
                    formation.getPrix(),
                    1, // Only registering 1 slot
                    currentUser,
                    "Formation Registration"
            );
            
            // Create new scene with the payment form
            Scene scene = new Scene(stripePane);
            
            // Get the current stage and set the new scene
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Payment - " + formation.getTitre());
            
        } catch (IOException e) {
            showAlert("Error", "Error loading payment form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    /**
     * Complete the registration process after successful payment
     */
    private void completeRegistration() {
        try {
            Inscription inscription = new Inscription(
                formation.getId(),
                TEST_USER_ID,
                LocalDate.now(),
                "paid", // Status is now "paid" instead of "pending"
                LocalDate.now()
            );

            inscriptionService.insert(inscription);
            
            showAlert("Success", "Registration successful! Payment has been processed.", Alert.AlertType.INFORMATION);
            closeWindow();
            
        } catch (SQLException e) {
            showAlert("Error", "Payment was successful, but there was an error completing your registration: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        ((Stage) formationTitleLabel.getScene().getWindow()).close();
    }
}

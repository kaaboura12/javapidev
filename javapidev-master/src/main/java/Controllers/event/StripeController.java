package Controllers.event;

import Controllers.BasefrontController;
import Models.Event;
import Models.Reservation;
import Models.User;
import Services.ReservationService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class StripeController extends BasefrontController {
    
    // FXML Components
    @FXML private Label pageTitle;
    @FXML private Label amountLabel;
    @FXML private Label eventNameLabel;
    @FXML private Label eventDetailsLabel;
    @FXML private Label seatsLabel;
    
    @FXML private TextField cardNumberField;
    @FXML private TextField cardholderNameField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvcField;
    
    @FXML private Button payButton;
    @FXML private Button cancelButton;
    @FXML private Label cardBrandLabel;
    @FXML private ImageView cardBrandLogo;
    @FXML private ProgressBar paymentProgress;
    @FXML private Label errorMessageLabel;
    
    // Data fields
    private Event event;
    private double amount;
    private int seatsReserved;
    private User currentUser;
    private String stripePublicKey;
    private ReservationService reservationService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        // Initialize service
        reservationService = new ReservationService();
        
        // Initialize Stripe public key from the image
        stripePublicKey = "pk_test_51Qu0kjJAoyWxedP4xWII1KzrY5WHtEOjRN1khc2REr3CXVdXYUjdUAaQ4RjF5GYo17fdYXWbJ32aaBSLEhABg0xZ00qzVueOSi";
        
        // Setup input field formatters
        setupInputFormatting();
        
        // Set up card brand detection
        setupCardBrandDetection();
        
        // Configure buttons
        setupButtons();
        
        // Setup responsive layout
        setupResponsiveLayout();
        
        // Hide progress bar and error message initially
        if (paymentProgress != null) {
            paymentProgress.setVisible(false);
        }
        if (errorMessageLabel != null) {
            errorMessageLabel.setText("");
            errorMessageLabel.setVisible(false);
        }
    }
    
    /**
     * Set the transaction details
     * 
     * @param event The event being reserved
     * @param amount The payment amount
     * @param seatsReserved The number of seats reserved
     * @param user The current user
     */
    public void setTransactionDetails(Event event, double amount, int seatsReserved, User user) {
        this.event = event;
        this.amount = amount;
        this.seatsReserved = seatsReserved;
        this.currentUser = user;
        
        // Update UI with transaction details
        updateTransactionDetails();
    }
    
    /**
     * Update transaction details in the UI
     */
    private void updateTransactionDetails() {
        if (event != null) {
            // Calculate subtotal (amount before service fee)
            double subtotal = amount - 2.50; // Assuming a fixed $2.50 service fee
            String formattedAmount = String.format("%.2f", amount);
            String formattedSubtotal = String.format("%.2f", subtotal);
            
            if (amountLabel != null) {
                amountLabel.setText("/$" + formattedAmount);
            }
            
            // Update subtotal if the label exists
            Label subtotalLabel = (Label) lookup("#subtotalLabel");
            if (subtotalLabel != null) {
                subtotalLabel.setText("/$" + formattedSubtotal);
            }
            
            if (eventNameLabel != null) {
                eventNameLabel.setText(event.getTitre());
            }
            
            if (eventDetailsLabel != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
                String eventDate = event.getDateEvenement().format(formatter);
                eventDetailsLabel.setText(eventDate + " at " + event.getLieu());
            }
            
            if (seatsLabel != null) {
                seatsLabel.setText(seatsReserved + (seatsReserved > 1 ? " seats" : " seat"));
            }
        }
    }
    
    /**
     * Helper method to find a node by ID
     */
    private Node lookup(String id) {
        if (payButton == null || payButton.getScene() == null) {
            return null;
        }
        return payButton.getScene().lookup(id);
    }
    
    /**
     * Set up input field formatting
     */
    private void setupInputFormatting() {
        // Format credit card input (xxxx xxxx xxxx xxxx)
        if (cardNumberField != null) {
            cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 19) {
                    cardNumberField.setText(oldValue);
                    return;
                }
                
                // Remove all non-digit characters
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                
                // Insert spaces after every 4 digits
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digitsOnly.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(digitsOnly.charAt(i));
                }
                
                // Only update if the formatting changed something
                if (!newValue.equals(formatted.toString())) {
                    cardNumberField.setText(formatted.toString());
                }
            });
        }
        
        // Format expiry date input (MM/YY)
        if (expiryDateField != null) {
            expiryDateField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 5) {
                    expiryDateField.setText(oldValue);
                    return;
                }
                
                // Remove all non-digit characters
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                
                // Format as MM/YY
                if (digitsOnly.length() > 0) {
                    if (digitsOnly.length() <= 2) {
                        expiryDateField.setText(digitsOnly);
                    } else {
                        expiryDateField.setText(digitsOnly.substring(0, 2) + "/" + digitsOnly.substring(2));
                    }
                }
            });
        }
        
        // Format CVC to only allow 3-4 digits
        if (cvcField != null) {
            cvcField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d{0,4}")) {
                    cvcField.setText(oldValue);
                }
            });
        }
    }
    
    /**
     * Set up card brand detection based on card number
     */
    private void setupCardBrandDetection() {
        if (cardNumberField != null) {
            cardNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
                String cardNumber = newValue.replaceAll("\\s", "");
                String cardType = detectCardType(cardNumber);
                
                // Update card brand label
                updateCardBrand(cardType);
            });
        }
    }
    
    /**
     * Detect card type from card number prefix
     */
    private String detectCardType(String cardNumber) {
        if (cardNumber.isEmpty()) {
            return "unknown";
        }
        
        // Simple prefix detection
        if (cardNumber.startsWith("4")) {
            return "visa";
        } else if (cardNumber.startsWith("5")) {
            return "mastercard";
        } else if (cardNumber.startsWith("3")) {
            return "amex";
        } else if (cardNumber.startsWith("6")) {
            return "discover";
        }
        
        return "unknown";
    }
    
    /**
     * Update card brand label based on detected card type
     */
    private void updateCardBrand(String cardType) {
        if (cardBrandLabel != null) {
            // Update the label
            switch (cardType) {
                case "visa":
                    cardBrandLabel.setText("VISA");
                    cardBrandLabel.setStyle("-fx-background-color: #1a1a95; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 3;");
                    break;
                case "mastercard":
                    cardBrandLabel.setText("MASTERCARD");
                    cardBrandLabel.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 3;");
                    break;
                case "amex":
                    cardBrandLabel.setText("AMEX");
                    cardBrandLabel.setStyle("-fx-background-color: #006fcf; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 3;");
                    break;
                case "discover":
                    cardBrandLabel.setText("DISCOVER");
                    cardBrandLabel.setStyle("-fx-background-color: #ff6600; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 3;");
                    break;
                default:
                    cardBrandLabel.setText("CARD");
                    cardBrandLabel.setStyle("-fx-background-color: #444455; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 6; -fx-background-radius: 3;");
                    break;
            }
        }
        
        // If the ImageView still exists in the FXML, update it too
        if (cardBrandLogo != null) {
            try {
                String imagePath = "/Images/card-" + cardType + ".png";
                cardBrandLogo.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } catch (Exception e) {
                // Ignore if image isn't available
            }
        }
    }
    
    /**
     * Set up button event handlers
     */
    private void setupButtons() {
        if (payButton != null) {
            payButton.setOnAction(event -> handlePayment());
        }
        
        if (cancelButton != null) {
            cancelButton.setOnAction(event -> handleCancel());
        }
    }
    
    /**
     * Handle the payment button click
     */
    @FXML
    private void handlePayment() {
        // Validate form
        if (!validatePaymentForm()) {
            return;
        }
        
        // Show progress indicator
        if (paymentProgress != null) {
            paymentProgress.setVisible(true);
            paymentProgress.setProgress(0.25);
        }
        
        // Disable buttons during processing
        setButtonsEnabled(false);
        
        // Simulate payment processing (in a real app, this would call the Stripe API)
        new Thread(() -> {
            try {
                // Simulate network delay with progress updates
                updateProgress(0.25);
                Thread.sleep(500);
                
                updateProgress(0.5);
                Thread.sleep(500);
                
                updateProgress(0.75);
                Thread.sleep(500);
                
                updateProgress(1.0);
                Thread.sleep(500);
                
                // Process the payment (in a real implementation, this would call Stripe API)
                boolean paymentSuccessful = processStripePayment();
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    if (paymentSuccessful) {
                        // Create the reservation in the database
                        createReservation();
                        
                        // Show success message
                        showPaymentSuccess();
                    } else {
                        // Show error message
                        showError("Payment failed. Please try again.");
                    }
                    
                    // Hide progress and re-enable buttons
                    if (paymentProgress != null) {
                        paymentProgress.setVisible(false);
                    }
                    setButtonsEnabled(true);
                });
                
            } catch (InterruptedException e) {
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    showError("Payment processing was interrupted.");
                    
                    // Hide progress and re-enable buttons
                    if (paymentProgress != null) {
                        paymentProgress.setVisible(false);
                    }
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }
    
    /**
     * Update progress bar from background thread
     */
    private void updateProgress(double value) {
        javafx.application.Platform.runLater(() -> {
            if (paymentProgress != null) {
                paymentProgress.setProgress(value);
            }
        });
    }
    
    /**
     * Validate payment form inputs
     */
    private boolean validatePaymentForm() {
        if (cardNumberField.getText().replaceAll("\\s", "").length() < 16) {
            showError("Please enter a valid card number.");
            return false;
        }
        
        if (cardholderNameField.getText().trim().isEmpty()) {
            showError("Please enter the cardholder's name.");
            return false;
        }
        
        if (!expiryDateField.getText().matches("\\d{2}/\\d{2}")) {
            showError("Please enter a valid expiration date (MM/YY).");
            return false;
        }
        
        if (cvcField.getText().length() < 3) {
            showError("Please enter a valid CVC code.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Process payment through Stripe (simulated)
     */
    private boolean processStripePayment() {
        // In a real implementation, this would use Stripe SDK to create a payment
        // For this demonstration, we're simulating a successful payment
        return true;
    }
    
    /**
     * Create the reservation in the database
     */
    private void createReservation() {
        try {
            // Create reservation
            Reservation reservation = new Reservation(
                    currentUser, 
                    event, 
                    LocalDate.now(), 
                    seatsReserved, 
                    amount
            );
            
            // Save to database
            reservationService.insert(reservation);
            
        } catch (SQLException e) {
            System.err.println("Error saving reservation: " + e.getMessage());
            showError("Payment was successful, but there was an error saving your reservation. Please contact support.");
        }
    }
    
    /**
     * Show payment success dialog and return to event list
     */
    private void showPaymentSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Successful");
        alert.setHeaderText("Thank you for your payment!");
        alert.setContentText("Your reservation for " + event.getTitre() + " has been confirmed. A confirmation email will be sent to you shortly.");
        
        // Style the alert
        styleAlert(alert);
        
        alert.showAndWait();
        
        // Navigate back to event list
        navigateToEventList();
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        // Ask for confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Payment");
        alert.setHeaderText("Are you sure you want to cancel?");
        alert.setContentText("Your reservation will not be processed if you cancel.");
        
        // Style the alert
        styleAlert(alert);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Navigate back to event list
                navigateToEventList();
            }
        });
    }
    
    /**
     * Style an alert dialog to match the application theme
     */
    private void styleAlert(Alert alert) {
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e30; -fx-text-fill: white;");
        alert.getDialogPane().getScene().getRoot().setStyle("-fx-background-color: #1e1e30;");
        
        for (ButtonType buttonType : alert.getButtonTypes()) {
            Button button = (Button) alert.getDialogPane().lookupButton(buttonType);
            if (buttonType == ButtonType.OK || buttonType == ButtonType.APPLY) {
                button.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white;");
            } else {
                button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white; -fx-border-color: rgba(255, 255, 255, 0.2);");
            }
        }
    }
    
    /**
     * Display an error message
     */
    private void showError(String message) {
        if (errorMessageLabel != null) {
            errorMessageLabel.setText(message);
            errorMessageLabel.setVisible(true);
        }
    }
    
    /**
     * Navigate back to event list
     */
    private void navigateToEventList() {
        navigateTo("/Views/event/listevent.fxml");
    }
    
    /**
     * Enable or disable all buttons
     */
    private void setButtonsEnabled(boolean enabled) {
        if (payButton != null) {
            payButton.setDisable(!enabled);
        }
        if (cancelButton != null) {
            cancelButton.setDisable(!enabled);
        }
    }
    
    /**
     * Setup responsive layout handlers
     */
    private void setupResponsiveLayout() {
        // Initial layout will be handled by CSS
        // This method is here for future dynamic adjustments if needed
        
        // For now, we'll rely on CSS media queries for responsive layout
        // This is a more reliable approach for JavaFX
    }
    
    /**
     * Adjust layout based on available width - simplified version
     * This method is unused but kept for future reference
     */
    private void adjustLayoutForWidth(double width) {
        // Find the order summary component
        Node orderSummary = lookup("#orderSummary");
        if (orderSummary == null) {
            return;
        }
        
        // Adjust max width based on screen size
        if (width < 700) {
            // For narrow screens
            orderSummary.setStyle("-fx-max-width: infinity;");
        } else {
            // For wider screens
            orderSummary.setStyle("-fx-max-width: 300px;");
        }
    }
    
    /**
     * Find the parent HBox containing the payment components
     * This method is unused but kept for future reference
     */
    private HBox findHBoxInParents(Node node) {
        Node parent = node.getParent();
        while (parent != null) {
            if (parent instanceof HBox) {
                return (HBox) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}

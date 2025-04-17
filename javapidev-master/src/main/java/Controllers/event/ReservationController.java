package Controllers.event;

import Controllers.BasefrontController;
import Models.Event;
import Models.Reservation;
import Models.User;
import Services.EventService;
import Services.ReservationService;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReservationController extends BasefrontController {
    
    @FXML
    private Label eventTitleLabel;
    
    @FXML
    private Label eventDateLabel;
    
    @FXML
    private Label eventLocationLabel;
    
    @FXML
    private Label seatPriceLabel;
    
    @FXML
    private Label availableSeatsLabel;
    
    @FXML
    private Spinner<Integer> seatsSpinner;
    
    @FXML
    private Label totalPriceLabel;
    
    @FXML
    private Label grandTotalLabel;
    
    @FXML
    private CheckBox termsCheckBox;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button confirmButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label statusMessageLabel;
    
    // Services
    private EventService eventService;
    private ReservationService reservationService;
    private UserService userService;
    
    // Current event and user
    private Event event;
    private User currentUser;
    
    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
    
    // Constants
    private final double SERVICE_FEE = 2.50;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        // Initialize services
        eventService = new EventService();
        reservationService = new ReservationService();
        userService = new UserService();
        
        // Setup spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        seatsSpinner.setValueFactory(valueFactory);
        
        // Apply modern styling to spinner
        styleSpinner();
        
        // Add listener to update total price when seats change
        seatsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateTotalPrice();
        });
        
        // Add listener for terms checkbox to enable/disable confirm button
        termsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateConfirmButtonState();
        });
        
        // Add hover effects for buttons
        setupButtonEffects();
        
        // Initially disable confirm button until terms are accepted
        updateConfirmButtonState();
        
        // Clear status message
        statusMessageLabel.setText("");
        
        // For development: load a test user
        // In a real system, you would get the current logged-in user
        try {
            currentUser = userService.findById(1); // Get user with ID 1 for testing
        } catch (SQLException e) {
            System.err.println("Error loading test user: " + e.getMessage());
        }
    }
    
    /**
     * Apply modern styling to the spinner
     */
    private void styleSpinner() {
        // Style spinner when focused
        seatsSpinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                seatsSpinner.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-border-color: rgba(255,126,95,0.7); -fx-text-fill: white;");
            } else {
                seatsSpinner.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white;");
            }
        });
        
        // Set initial style
        seatsSpinner.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white;");
    }
    
    /**
     * Setup hover effects for buttons
     */
    private void setupButtonEffects() {
        // Hover effect for confirm button
        confirmButton.setOnMouseEntered(e -> {
            confirmButton.setStyle("-fx-background-color: linear-gradient(to right, #feb47b, #ff7e5f); -fx-text-fill: white; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(255,126,95,0.5), 8, 0, 0, 0);");
            confirmButton.setScaleX(1.05);
            confirmButton.setScaleY(1.05);
        });
        
        confirmButton.setOnMouseExited(e -> {
            confirmButton.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
            confirmButton.setScaleX(1.0);
            confirmButton.setScaleY(1.0);
        });
        
        // Hover effect for cancel button
        cancelButton.setOnMouseEntered(e -> {
            cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white; -fx-border-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 20px; -fx-border-radius: 20px;");
        });
        
        cancelButton.setOnMouseExited(e -> {
            cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: #c0c0c0; -fx-border-color: rgba(255, 255, 255, 0.2); -fx-background-radius: 20px; -fx-border-radius: 20px;");
        });
        
        // Hover effect for back button
        if (backButton != null) {
            backButton.setOnMouseEntered(e -> {
                backButton.getStyleClass().add("back-button-hover");
            });
            
            backButton.setOnMouseExited(e -> {
                backButton.getStyleClass().remove("back-button-hover");
            });
        }
    }
    
    /**
     * Set the event for reservation
     * 
     * @param event The event to make reservation for
     */
    public void setEvent(Event event) {
        this.event = event;
        
        if (event != null) {
            // Update UI with event details
            eventTitleLabel.setText(event.getTitre());
            eventDateLabel.setText(event.getDateEvenement().format(dateFormatter));
            eventLocationLabel.setText(event.getLieu());
            seatPriceLabel.setText(String.format("$%.2f", event.getSeatprice()));
            
            try {
                // Get available seats
                int totalReserved = reservationService.getTotalReservedSeatsForEvent(event.getIdevent());
                int available = event.getNombreBillets() - totalReserved;
                availableSeatsLabel.setText(String.valueOf(available));
                
                // Update spinner max value
                SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                        (SpinnerValueFactory.IntegerSpinnerValueFactory) seatsSpinner.getValueFactory();
                valueFactory.setMax(available);
                
                // Enable/disable confirm button based on availability and terms
                updateConfirmButtonState();
                
                if (available <= 0) {
                    statusMessageLabel.setText("Sorry, this event is sold out.");
                    termsCheckBox.setDisable(true);
                    confirmButton.setDisable(true);
                }
            } catch (SQLException e) {
                System.err.println("Error getting reservation data: " + e.getMessage());
                availableSeatsLabel.setText("Error");
            }
            
            // Update initial total price
            updateTotalPrice();
        }
    }
    
    /**
     * Update the total price based on seat count
     */
    private void updateTotalPrice() {
        if (event != null) {
            int seats = seatsSpinner.getValue();
            double subtotal = seats * event.getSeatprice();
            double grandTotal = subtotal + SERVICE_FEE;
            
            totalPriceLabel.setText(String.format("$%.2f", subtotal));
            grandTotalLabel.setText(String.format("$%.2f", grandTotal));
            
            // Add a subtle animation for the total price change
            DropShadow glow = new DropShadow();
            glow.setColor(Color.valueOf("#ff7e5f"));
            glow.setRadius(10);
            grandTotalLabel.setEffect(glow);
            
            // Fade out the glow effect after a short delay
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(700));
            pause.setOnFinished(event -> {
                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), grandTotalLabel);
                fade.setFromValue(1.0);
                fade.setToValue(1.0); // Keep text opacity full
                fade.setOnFinished(e -> grandTotalLabel.setEffect(null));
                fade.play();
            });
            pause.play();
        }
    }
    
    /**
     * Update confirm button state based on terms checkbox
     */
    private void updateConfirmButtonState() {
        boolean termsAccepted = termsCheckBox.isSelected();
        boolean hasSeatAvailability = true;
        
        try {
            // Check if seats are available
            if (event != null) {
                int totalReserved = reservationService.getTotalReservedSeatsForEvent(event.getIdevent());
                int available = event.getNombreBillets() - totalReserved;
                hasSeatAvailability = available > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking availability: " + e.getMessage());
        }
        
        // Enable button only if terms accepted and seats available
        confirmButton.setDisable(!termsAccepted || !hasSeatAvailability);
        
        // Update button appearance based on state
        if (!termsAccepted || !hasSeatAvailability) {
            confirmButton.setOpacity(0.7);
        } else {
            confirmButton.setOpacity(1.0);
        }
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        // Navigate back to events list
        navigateTo("/Views/event/listevent.fxml");
    }
    
    /**
     * Handle back button click to return to events list
     * @param event The action event
     */
    @FXML
    private void handleBackToEvents() {
        try {
            // Try to find the parent content area
            StackPane contentArea = findContentArea();
            
            if (contentArea != null) {
                // Load the events list view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/listevent.fxml"));
                Parent eventsView = loader.load();
                
                // Replace the content
                contentArea.getChildren().clear();
                
                // Add a fade-in transition
                FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), eventsView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                
                contentArea.getChildren().add(eventsView);
                fadeIn.play();
            } else {
                // Fallback: Navigate using the existing method
                navigateTo("/Views/event/listevent.fxml");
            }
        } catch (IOException e) {
            showErrorMessage("Error navigating back to events: " + e.getMessage());
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find the main content area in parent hierarchy
     * @return The content area StackPane or null if not found
     */
    private StackPane findContentArea() {
        try {
            // Get the current scene
            Scene currentScene = backButton.getScene();
            if (currentScene == null) {
                return null;
            }
            
            // Start from the root of the scene
            Parent parent = currentScene.getRoot();
            
            // Traverse the scene graph to find a StackPane with id "contentArea"
            return findStackPaneById(parent, "contentArea");
        } catch (Exception e) {
            // If any error occurs, return null
            return null;
        }
    }
    
    /**
     * Recursively search for a StackPane with specific ID
     * @param parent The parent node to search in
     * @param id The id to look for
     * @return The found StackPane or null
     */
    private StackPane findStackPaneById(Parent parent, String id) {
        // Check if this node is a StackPane with matching id
        if (parent instanceof StackPane && id.equals(parent.getId())) {
            return (StackPane) parent;
        }
        
        // If not, search in its children
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                StackPane found = findStackPaneById((Parent) child, id);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Handle confirm reservation button click
     */
    @FXML
    private void handleConfirmReservation() {
        if (!termsCheckBox.isSelected()) {
            showErrorMessage("Please agree to the terms and conditions.");
            return;
        }
        
        if (event == null) {
            showErrorMessage("Error: No event selected");
            return;
        }
        
        if (currentUser == null) {
            showErrorMessage("Error: You must be logged in to make a reservation");
            return;
        }
        
        int seatsToReserve = seatsSpinner.getValue();
        double subtotal = seatsToReserve * event.getSeatprice();
        double totalAmount = subtotal + SERVICE_FEE; // Include service fee
        
        try {
            // Check available seats again (in case they changed)
            int totalReserved = reservationService.getTotalReservedSeatsForEvent(event.getIdevent());
            int available = event.getNombreBillets() - totalReserved;
            
            if (seatsToReserve > available) {
                showErrorMessage("Error: Not enough seats available");
                return;
            }
            
            // Navigate to Stripe payment page
            try {
                // Load the Stripe FXML 
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/stripe.fxml"));
                Parent stripePane = loader.load();
                
                // Get the controller and set transaction details
                Controllers.event.StripeController stripeController = loader.getController();
                stripeController.setTransactionDetails(event, totalAmount, seatsToReserve, currentUser);
                
                // Find the content area to place the Stripe payment form
                StackPane contentArea = findContentArea();
                if (contentArea != null) {
                    // Clear current content and add payment form
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(stripePane);
                    
                    // Apply a fade-in transition
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), stripePane);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                } else {
                    // Fallback to creating a new scene
                    Stage stage = (Stage) confirmButton.getScene().getWindow();
                    Scene scene = new Scene(stripePane);
                    stage.setScene(scene);
                }
                
            } catch (IOException e) {
                showErrorMessage("Error loading payment form");
                System.err.println("Error loading Stripe payment form: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            showErrorMessage("Error: Failed to check seat availability");
            System.err.println("Error checking seat availability: " + e.getMessage());
        }
    }
    
    /**
     * Show an error message in a styled alert dialog
     * 
     * @param message The error message to display
     */
    private void showErrorMessage(String message) {
        statusMessageLabel.setText(message);
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Reservation Error");
        alert.setHeaderText("Unable to Complete Reservation");
        alert.setContentText(message);
        
        // Style the alert to match theme
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e30; -fx-text-fill: white;");
        alert.getDialogPane().getScene().getRoot().setStyle("-fx-background-color: #1e1e30;");
        
        ((Button) alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0))).setStyle(
            "-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white;"
        );
        
        alert.showAndWait();
    }
    
    /**
     * Show a success message in a styled alert dialog
     * 
     * @param message The success message to display
     */
    private void showSuccessMessage(String message) {
        statusMessageLabel.setText(message);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reservation Successful");
        alert.setHeaderText("Booking Confirmed!");
        alert.setContentText(message + "\n\nA confirmation email will be sent to you shortly.");
        
        // Style the alert to match theme
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e30; -fx-text-fill: white;");
        alert.getDialogPane().getScene().getRoot().setStyle("-fx-background-color: #1e1e30;");
        
        ((Button) alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0))).setStyle(
            "-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white;"
        );
        
        alert.showAndWait();
    }
} 
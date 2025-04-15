package Controllers.event;

import Models.Donation;
import Models.Event;
import Services.DonationService;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class DonationController implements Initializable {

    @FXML
    private Label eventNameLabel;
    
    @FXML
    private Label eventDayLabel;
    
    @FXML
    private Label eventMonthLabel;
    
    @FXML
    private Label eventTimeLabel;
    
    @FXML
    private Label eventLocationLabel;
    
    @FXML
    private Button amount10Btn;
    
    @FXML
    private Button amount25Btn;
    
    @FXML
    private Button amount50Btn;
    
    @FXML
    private Button amount100Btn;
    
    @FXML
    private Button amount200Btn;
    
    @FXML
    private Button amountCustomBtn;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private Spinner<Double> amountSpinner;
    
    @FXML
    private ComboBox<String> paymentMethodCombo;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label statusMessageLabel;
    
    private Event currentEvent;
    private Button lastSelectedAmountButton;
    private DonationService donationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize donation service
        donationService = new DonationService();
        
        // Initialize payment methods
        paymentMethodCombo.getItems().addAll("Credit Card", "PayPal", "Bank Transfer", "Crypto");
        paymentMethodCombo.setValue("Credit Card");
        
        // Apply CSS to combobox
        paymentMethodCombo.setStyle("-fx-text-fill: white;");
        
        // Setup amount spinner
        SpinnerValueFactory<Double> valueFactory = 
                new SpinnerValueFactory.DoubleSpinnerValueFactory(5.0, 5000.0, 50.0, 5.0);
        amountSpinner.setValueFactory(valueFactory);
        
        // Set default selected amount
        selectAmountButton(amount50Btn, 50.0);
        
        // Add focus listeners to style fields when focused
        setupFocusListeners();
        
        // Set up animations and effects for the back button
        setupBackButton();
        
        // Clear status message
        if (statusMessageLabel != null) {
            statusMessageLabel.setText("");
        }
        
        // Apply modern styling to submit and cancel buttons
        setupButtonEffects();
    }
    
    /**
     * Setup hover effects for buttons
     */
    private void setupButtonEffects() {
        // Hover effect for submit button
        submitButton.setOnMouseEntered(e -> {
            submitButton.setStyle("-fx-background-color: linear-gradient(to right, #feb47b, #ff7e5f); -fx-text-fill: white; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(255,126,95,0.5), 8, 0, 0, 0);");
            submitButton.setScaleX(1.05);
            submitButton.setScaleY(1.05);
        });
        
        submitButton.setOnMouseExited(e -> {
            submitButton.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
            submitButton.setScaleX(1.0);
            submitButton.setScaleY(1.0);
        });
        
        // Hover effect for cancel button
        cancelButton.setOnMouseEntered(e -> {
            cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white; -fx-border-color: rgba(255, 255, 255, 0.3); -fx-background-radius: 20px; -fx-border-radius: 20px;");
        });
        
        cancelButton.setOnMouseExited(e -> {
            cancelButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-text-fill: #c0c0c0; -fx-border-color: rgba(255, 255, 255, 0.2); -fx-background-radius: 20px; -fx-border-radius: 20px;");
        });
    }
    
    /**
     * Set up focus listeners for form fields to enhance their appearance
     */
    private void setupFocusListeners() {
        // Add focus styling for text fields
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                nameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-border-color: rgba(255,126,95,0.7); -fx-text-fill: white; -fx-prompt-text-fill: #909090;");
            } else {
                nameField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-prompt-text-fill: #909090;");
            }
        });
        
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                emailField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-border-color: rgba(255,126,95,0.7); -fx-text-fill: white; -fx-prompt-text-fill: #909090;");
            } else {
                emailField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-prompt-text-fill: #909090;");
            }
        });
        
        phoneField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                phoneField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.15); -fx-border-color: rgba(255,126,95,0.7); -fx-text-fill: white; -fx-prompt-text-fill: #909090;");
            } else {
                phoneField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; -fx-prompt-text-fill: #909090;");
            }
        });
    }
    
    /**
     * Set the event for which the donation is being made
     * @param event The event to set
     */
    public void setEvent(Event event) {
        this.currentEvent = event;
        updateEventInfo();
    }
    
    private void updateEventInfo() {
        if (currentEvent != null) {
            // Set event name
            eventNameLabel.setText(currentEvent.getTitre());
            
            // Get event date string and parse components for display
            try {
                String dateString = currentEvent.getTimestart().toString();
                
                // Extract and set day (assuming format contains day information)
                if (dateString.length() >= 2) {
                    // Try to extract day - this is a simplified approach
                    // In a real application, properly parse the date according to its actual format
                    String day = dateString.substring(0, 2).replaceAll("[^0-9]", "");
                    if (!day.isEmpty()) {
                        eventDayLabel.setText(day);
                    } else {
                        eventDayLabel.setText("15"); // Default day
                    }
                }
                
                // Set month (default to current month if not available)
                String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
                int monthIndex = LocalDate.now().getMonthValue() - 1; // Default to current month
                try {
                    if (dateString.length() >= 5) {
                        String monthPart = dateString.substring(3, 5).replaceAll("[^0-9]", "");
                        if (!monthPart.isEmpty()) {
                            int parsedMonth = Integer.parseInt(monthPart) - 1;
                            if (parsedMonth >= 0 && parsedMonth < 12) {
                                monthIndex = parsedMonth;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Use default month if parsing fails
                }
                eventMonthLabel.setText(months[monthIndex]);
                
                // Set default time
                eventTimeLabel.setText("7:00 PM - 10:00 PM");
            } catch (Exception e) {
                // Set defaults if parsing fails
                eventDayLabel.setText("15");
                eventMonthLabel.setText("JUN");
                eventTimeLabel.setText("7:00 PM - 10:00 PM");
            }
            
            // Set location (if available in the event object)
            try {
                if (currentEvent.getLieu() != null && !currentEvent.getLieu().isEmpty()) {
                    eventLocationLabel.setText(currentEvent.getLieu());
                } else {
                    eventLocationLabel.setText("Grand Hall");
                }
            } catch (Exception e) {
                eventLocationLabel.setText("Grand Hall");
            }
        }
    }
    
    /**
     * Handle clicking on a preset donation amount button
     * @param event The action event
     */
    @FXML
    private void handleAmountSelection(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Button clickedButton = (Button) event.getSource();
            double amount = parseAmountFromButton(clickedButton.getText());
            
            // Update spinner with the selected amount
            amountSpinner.getValueFactory().setValue(amount);
            
            // Update button styling
            selectAmountButton(clickedButton, amount);
        }
    }
    
    /**
     * Handle custom amount button click
     * @param event The action event
     */
    @FXML
    private void handleCustomAmount(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Custom Donation Amount");
        dialog.setHeaderText("Enter Custom Amount");
        dialog.setContentText("Amount ($):");
        
        // Style the dialog to match theme
        dialog.getDialogPane().setStyle("-fx-background-color: #1e1e30;");
        dialog.getDialogPane().getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialog.getDialogPane().lookupButton(buttonType);
            if (button != null) {
                if (buttonType.getButtonData().isDefaultButton()) {
                    button.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white;");
                } else {
                    button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
                }
            }
        });
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                double customAmount = Double.parseDouble(result);
                if (customAmount < 5.0) {
                    showErrorMessage("Minimum donation amount is $5.00");
                    return;
                }
                if (customAmount > 5000.0) {
                    showErrorMessage("Maximum donation amount is $5,000.00");
                    return;
                }
                
                // Update spinner with custom amount
                amountSpinner.getValueFactory().setValue(customAmount);
                
                // Update button styling
                selectAmountButton(amountCustomBtn, customAmount);
            } catch (NumberFormatException e) {
                showErrorMessage("Please enter a valid amount");
            }
        });
    }
    
    /**
     * Helper method to parse the amount from a button's text
     * @param buttonText The button text (e.g. "/$10")
     * @return The parsed amount as a double
     */
    private double parseAmountFromButton(String buttonText) {
        try {
            return Double.parseDouble(buttonText.replace("/$", ""));
        } catch (NumberFormatException e) {
            return 50.0; // Default value if parsing fails
        }
    }
    
    /**
     * Helper method to update the styling of the amount buttons
     * @param selectedButton The button that was selected
     * @param amount The amount selected
     */
    private void selectAmountButton(Button selectedButton, double amount) {
        // Reset styling on previous selection
        if (lastSelectedAmountButton != null) {
            lastSelectedAmountButton.getStyleClass().remove("donation-amount-selected");
            lastSelectedAmountButton.getStyleClass().add("donation-amount");
            lastSelectedAmountButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-border-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white;");
        }
        
        // Apply selected styling to current button
        selectedButton.getStyleClass().remove("donation-amount");
        selectedButton.getStyleClass().add("donation-amount-selected");
        selectedButton.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white; -fx-border-color: transparent; -fx-effect: dropshadow(gaussian, rgba(255,126,95,0.5), 5, 0, 0, 0);");
        
        // Update spinner value
        amountSpinner.getValueFactory().setValue(amount);
        
        // Remember the last selected button
        lastSelectedAmountButton = selectedButton;
    }
    
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (validateForm()) {
            try {
                Donation donation = createDonation();
                saveDonation(donation);
                showSuccessMessage("Donation successful!");
                clearForm();
            } catch (Exception ex) {
                showErrorMessage("Error processing donation: " + ex.getMessage());
            }
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        // Return to previous screen or close dialog
        clearForm();
        handleBackToEvents(event);
    }
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (nameField.getText().trim().isEmpty()) {
            errors.append("Please enter your name.\n");
        }
        
        if (emailField.getText().trim().isEmpty() || !isValidEmail(emailField.getText())) {
            errors.append("Please enter a valid email address.\n");
        }
        
        if (phoneField.getText().trim().isEmpty() || !isValidPhone(phoneField.getText())) {
            errors.append("Please enter a valid phone number.\n");
        }
        
        if (currentEvent == null) {
            errors.append("No event selected for donation.\n");
        }
        
        if (errors.length() > 0) {
            showErrorMessage(errors.toString());
            return false;
        }
        
        return true;
    }
    
    private Donation createDonation() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        double amount = amountSpinner.getValue();
        String paymentMethod = paymentMethodCombo.getValue();
        
        return new Donation(
            currentEvent.getIdevent(),
            null, // userid is null since we're not using a session
            name,
            email,
            amount,
            LocalDate.now(),
            paymentMethod,
            phone
        );
    }
    
    private void saveDonation(Donation donation) {
        try {
            donationService.insert(donation);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save donation: " + ex.getMessage(), ex);
        }
    }
    
    private void showSuccessMessage(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Donation Successful");
        alert.setHeaderText("Thank You For Your Support!");
        alert.setContentText("Your donation of $" + amountSpinner.getValue() + " has been successfully processed. A confirmation email will be sent to " + emailField.getText() + " shortly.");
        
        // Style the alert to match theme
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e30; -fx-text-fill: white;");
        alert.getDialogPane().getScene().getRoot().setStyle("-fx-background-color: #1e1e30;");
        
        ((Button) alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0))).setStyle(
            "-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white;"
        );
        
        alert.showAndWait();
    }
    
    private void showErrorMessage(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
        }
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Unable to Process Donation");
        alert.setContentText(message);
        
        // Style the alert to match theme
        alert.getDialogPane().setStyle("-fx-background-color: #1e1e30; -fx-text-fill: white;");
        alert.getDialogPane().getScene().getRoot().setStyle("-fx-background-color: #1e1e30;");
        
        ((Button) alert.getDialogPane().lookupButton(alert.getButtonTypes().get(0))).setStyle(
            "-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white;"
        );
        
        alert.showAndWait();
    }
    
    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        selectAmountButton(amount50Btn, 50.0);
        paymentMethodCombo.setValue("Credit Card");
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
    
    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9\\-\\+\\s\\(\\)]{8,15}$");
    }
    
    /**
     * Set up the back button with hover animations
     */
    private void setupBackButton() {
        // Add hover effect
        backButton.setOnMouseEntered(e -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), backButton);
            fadeIn.setFromValue(1.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        backButton.setOnMouseExited(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), backButton);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(1.0);
            fadeOut.play();
        });
    }
    
    /**
     * Handle back button click to return to events list
     * @param event The action event
     */
    @FXML
    private void handleBackToEvents(ActionEvent event) {
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
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), eventsView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                
                contentArea.getChildren().add(eventsView);
                fadeIn.play();
            } else {
                // Fallback: If we can't find the content area, navigate back using the stage
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/listevent.fxml"));
                Parent eventsView = loader.load();
                
                Scene scene = new Scene(eventsView);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                // Set the scene and show
                stage.setScene(scene);
                stage.show();
            }
        } catch (IOException e) {
            showErrorMessage("Error navigating back to events: " + e.getMessage());
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
} 
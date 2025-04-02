package Controllers.event;

import Models.Donation;
import Models.Event;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

public class DonationController implements Initializable {

    @FXML
    private Label eventNameLabel;
    
    @FXML
    private Label eventDateLabel;
    
    @FXML
    private Button amount10Btn;
    
    @FXML
    private Button amount25Btn;
    
    @FXML
    private Button amount50Btn;
    
    @FXML
    private Button amount100Btn;
    
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
    
    private Event currentEvent;
    private Button lastSelectedAmountButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize payment methods
        paymentMethodCombo.getItems().addAll("Credit Card", "PayPal", "Bank Transfer");
        paymentMethodCombo.setValue("Credit Card");
        
        // Setup amount spinner
        SpinnerValueFactory<Double> valueFactory = 
                new SpinnerValueFactory.DoubleSpinnerValueFactory(5.0, 5000.0, 50.0, 5.0);
        amountSpinner.setValueFactory(valueFactory);
        
        // Set default selected amount
        selectAmountButton(amount50Btn, 50.0);
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
            eventNameLabel.setText(currentEvent.getTitre());
            eventDateLabel.setText(currentEvent.getTimestart().toString());
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
     * @param buttonText The button text (e.g. "$10")
     * @return The parsed amount as a double
     */
    private double parseAmountFromButton(String buttonText) {
        try {
            return Double.parseDouble(buttonText.replace("$", ""));
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
        }
        
        // Apply selected styling to current button
        selectedButton.getStyleClass().remove("donation-amount");
        selectedButton.getStyleClass().add("donation-amount-selected");
        
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
                showSuccessMessage();
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
        // Here you would add the code to save the donation to your database
        // This is a placeholder for the actual implementation
        System.out.println("Saving donation: " + donation);
        
        // Example of how you might call a service
        // DonationService.getInstance().saveDonation(donation);
    }
    
    private void showSuccessMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Donation Successful");
        alert.setHeaderText("Thank You for Your Donation");
        alert.setContentText("Your donation has been processed successfully.");
        alert.showAndWait();
    }
    
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Unable to Process Donation");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        
        // Reset amount to default
        amountSpinner.getValueFactory().setValue(50.0);
        selectAmountButton(amount50Btn, 50.0);
        
        paymentMethodCombo.setValue("Credit Card");
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
    
    private boolean isValidPhone(String phone) {
        // Simple phone validation
        return phone.matches("^[0-9]{8,12}$");
    }
} 
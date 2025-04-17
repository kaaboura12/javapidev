package Controllers.event;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class TestController implements Initializable {

    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField eventNameField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private DatePicker eventDatePicker;
    
    @FXML
    private TextField eventTimeField;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Label statusLabel;
    
    // The webhook URL from the screenshot
    private final String WEBHOOK_URL = "https://kaaboura12.app.n8n.cloud/webhook/316c3d40-110a-4f50-931c-53ff1a103cab";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set some default values for testing
        usernameField.setText("sayari amin");
        emailField.setText("mohamedamin.sayari@esprit.tn");
        eventNameField.setText("streetart");
        priceField.setText("122");
        eventDatePicker.setValue(LocalDate.now());
        eventTimeField.setText("23:30:00");
        
        // Style the date picker
        eventDatePicker.setStyle("-fx-control-inner-background: rgba(255, 255, 255, 0.1); -fx-text-fill: white;");
    }
    
    @FXML
    private void handleSendToWebhook() {
        // Validate the form
        if (!validateForm()) {
            return;
        }
        
        try {
            // Get values from form
            String username = usernameField.getText();
            String email = emailField.getText();
            String eventName = eventNameField.getText();
            double price = Double.parseDouble(priceField.getText());
            
            // Format the date and time
            LocalDate date = eventDatePicker.getValue();
            LocalTime time = LocalTime.parse(eventTimeField.getText());
            LocalDateTime eventDateTime = LocalDateTime.of(date, time);
            String formattedDateTime = eventDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Send data to webhook
            boolean success = sendDataToWebhook(username, email, eventName, price, formattedDateTime);
            
            if (success) {
                showStatus("Success! Data sent to webhook", true);
            } else {
                showStatus("Failed to send data to webhook", false);
            }
            
        } catch (NumberFormatException e) {
            showStatus("Error: Invalid price format", false);
        } catch (DateTimeParseException e) {
            showStatus("Error: Invalid time format. Use HH:MM:SS", false);
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    /**
     * Validate form fields
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        if (usernameField.getText().trim().isEmpty()) {
            showStatus("Username is required", false);
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showStatus("Email is required", false);
            return false;
        }
        
        if (eventNameField.getText().trim().isEmpty()) {
            showStatus("Event name is required", false);
            return false;
        }
        
        if (priceField.getText().trim().isEmpty()) {
            showStatus("Price is required", false);
            return false;
        }
        
        if (eventDatePicker.getValue() == null) {
            showStatus("Event date is required", false);
            return false;
        }
        
        if (eventTimeField.getText().trim().isEmpty()) {
            showStatus("Event time is required", false);
            return false;
        }
        
        return true;
    }
    
    /**
     * Send data to webhook URL
     * 
     * @param username The username
     * @param email The email
     * @param eventName The event name
     * @param price The price
     * @param eventDate The formatted event date
     * @return true if successful, false otherwise
     */
    private boolean sendDataToWebhook(String username, String email, String eventName, double price, String eventDate) {
        try {
            // Create URL with query parameters
            String queryParams = String.format(
                "username=%s&email=%s&eventname=%s&price=%.2f&eventdate=%s",
                java.net.URLEncoder.encode(username, "UTF-8"),
                java.net.URLEncoder.encode(email, "UTF-8"),
                java.net.URLEncoder.encode(eventName, "UTF-8"),
                price,
                java.net.URLEncoder.encode(eventDate, "UTF-8")
            );

            URL url = new URL(WEBHOOK_URL + "?" + queryParams);
            System.out.println("Sending GET request to: " + url);
            
            // Setup connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // Send request and get response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            connection.disconnect();
            
            // Check if successful
            return responseCode >= 200 && responseCode < 300;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show status message
     * 
     * @param message The message to display
     * @param success Whether it was successful
     */
    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setTextFill(success ? Color.LIGHTGREEN : Color.TOMATO);
    }
}

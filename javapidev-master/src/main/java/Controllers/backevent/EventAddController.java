package Controllers.backevent;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EventAddController {
    @FXML private TextField eventNameField;
    @FXML private DatePicker eventDatePicker;
    @FXML private TextField eventTimeField;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField capacityField;
    @FXML private TextField priceField;
    @FXML private TextField missionField;
    @FXML private TextField donationObjectiveField;
    @FXML private TextField imageField;

    private EventService eventService;

    @FXML
    public void initialize() {
        eventService = new EventService();
        
        // Set default date to today
        eventDatePicker.setValue(LocalDate.now());
        
        // Add input validation listeners
        capacityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                capacityField.setText(oldValue);
            }
        });
        
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
            }
        });

        donationObjectiveField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*\\.?\\d*")) {
                donationObjectiveField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleAddEvent(ActionEvent event) {
        if (validateInputs()) {
            try {
                // Create event object with the form data
                String eventName = eventNameField.getText();
                LocalDate eventDate = eventDatePicker.getValue();
                LocalTime eventTime = LocalTime.parse(eventTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                String location = locationField.getText();
                String description = descriptionArea.getText();
                int capacity = Integer.parseInt(capacityField.getText());
                double price = Double.parseDouble(priceField.getText());
                String mission = missionField.getText();
                double donationObjective = Double.parseDouble(donationObjectiveField.getText());
                String image = imageField.getText();

                // Create and save the event
                Event newEvent = new Event();
                newEvent.setTitre(eventName);
                newEvent.setDescription(description);
                newEvent.setLieu(location);
                newEvent.setNombreBillets(capacity);
                newEvent.setImage(image);
                newEvent.setTimestart(eventTime);
                newEvent.setEvent_mission(mission);
                newEvent.setDonation_objective(donationObjective);
                newEvent.setSeatprice(price);
                newEvent.setDateEvenement(LocalDateTime.of(eventDate, eventTime));

                eventService.insert(newEvent);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Event added successfully!");
                closeWindow(event);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add event: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow(event);
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (eventNameField.getText().trim().isEmpty()) {
            errors.append("Event name is required\n");
        }

        if (eventDatePicker.getValue() == null) {
            errors.append("Event date is required\n");
        }

        if (eventTimeField.getText().trim().isEmpty()) {
            errors.append("Event time is required\n");
        } else {
            try {
                LocalTime.parse(eventTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                errors.append("Invalid time format. Please use HH:mm\n");
            }
        }

        if (locationField.getText().trim().isEmpty()) {
            errors.append("Location is required\n");
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            errors.append("Description is required\n");
        }

        if (capacityField.getText().trim().isEmpty()) {
            errors.append("Capacity is required\n");
        } else {
            try {
                int capacity = Integer.parseInt(capacityField.getText());
                if (capacity <= 0) {
                    errors.append("Capacity must be greater than 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Invalid capacity value\n");
            }
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("Price is required\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    errors.append("Price cannot be negative\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Invalid price value\n");
            }
        }

        if (donationObjectiveField.getText().trim().isEmpty()) {
            errors.append("Donation objective is required\n");
        } else {
            try {
                double donationObjective = Double.parseDouble(donationObjectiveField.getText());
                if (donationObjective < 0) {
                    errors.append("Donation objective cannot be negative\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Invalid donation objective value\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}

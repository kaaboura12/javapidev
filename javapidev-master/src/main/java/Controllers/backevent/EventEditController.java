package Controllers.backevent;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class EventEditController implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private Spinner<Integer> ticketsSpinner;
    @FXML private TextField imageField;
    @FXML private TextField timeField;
    @FXML private TextField missionField;
    @FXML private TextField donationField;
    @FXML private TextField priceField;
    @FXML private DatePicker datePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Validation message containers
    @FXML private VBox titleValidation;
    @FXML private VBox locationValidation;
    @FXML private VBox timeValidation;
    @FXML private VBox donationValidation;
    @FXML private VBox priceValidation;
    @FXML private VBox dateValidation;

    private Event event;
    private EventService eventService;
    private boolean isSaved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        eventService = new EventService();
        
        // Initialize validation containers
        initializeValidationContainers();
        
        // Add real-time validation listeners
        setupValidationListeners();
    }

    private void initializeValidationContainers() {
        // Create validation containers if they don't exist
        if (titleValidation == null) titleValidation = new VBox(2);
        if (locationValidation == null) locationValidation = new VBox(2);
        if (timeValidation == null) timeValidation = new VBox(2);
        if (donationValidation == null) donationValidation = new VBox(2);
        if (priceValidation == null) priceValidation = new VBox(2);
        if (dateValidation == null) dateValidation = new VBox(2);
    }

    private void setupValidationListeners() {
        // Title validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(titleValidation, "Title is required");
            } else if (newValue.length() < 3) {
                showValidationError(titleValidation, "Title must be at least 3 characters");
            } else {
                clearValidationError(titleValidation);
            }
        });

        // Location validation
        locationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(locationValidation, "Location is required");
            } else {
                clearValidationError(locationValidation);
            }
        });

        // Time validation
        timeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    LocalTime.parse(newValue, DateTimeFormatter.ofPattern("HH:mm"));
                    clearValidationError(timeValidation);
                } catch (DateTimeParseException e) {
                    showValidationError(timeValidation, "Invalid time format (HH:mm)");
                }
            } else {
                clearValidationError(timeValidation);
            }
        });

        // Donation validation
        donationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(donationValidation, "Donation goal is required");
            } else if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                showValidationError(donationValidation, "Invalid amount format");
            } else {
                try {
                    double amount = Double.parseDouble(newValue);
                    if (amount < 0) {
                        showValidationError(donationValidation, "Amount cannot be negative");
                    } else {
                        clearValidationError(donationValidation);
                    }
                } catch (NumberFormatException e) {
                    showValidationError(donationValidation, "Invalid amount");
                }
            }
        });

        // Price validation
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(priceValidation, "Price is required");
            } else if (!newValue.matches("\\d*(\\.\\d{0,2})?")) {
                showValidationError(priceValidation, "Invalid price format");
            } else {
                try {
                    double price = Double.parseDouble(newValue);
                    if (price < 0) {
                        showValidationError(priceValidation, "Price cannot be negative");
                    } else {
                        clearValidationError(priceValidation);
                    }
                } catch (NumberFormatException e) {
                    showValidationError(priceValidation, "Invalid price");
                }
            }
        });

        // Date validation
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                showValidationError(dateValidation, "Date is required");
            } else if (newValue.isBefore(LocalDate.now())) {
                showValidationError(dateValidation, "Date cannot be in the past");
            } else {
                clearValidationError(dateValidation);
            }
        });
    }

    private void showValidationError(VBox container, String message) {
        container.getChildren().clear();
        Text errorText = new Text(message);
        errorText.setFill(Color.RED);
        errorText.setStyle("-fx-font-size: 12px;");
        container.getChildren().add(errorText);
    }

    private void clearValidationError(VBox container) {
        container.getChildren().clear();
    }

    private boolean isFormValid() {
        boolean isValid = true;
        
        // Check title
        if (titleField.getText().trim().isEmpty() || titleField.getText().length() < 3) {
            showValidationError(titleValidation, "Title is required and must be at least 3 characters");
            isValid = false;
        }
        
        // Check location
        if (locationField.getText().trim().isEmpty()) {
            showValidationError(locationValidation, "Location is required");
            isValid = false;
        }
        
        // Check time format if provided
        if (!timeField.getText().trim().isEmpty()) {
            try {
                LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                showValidationError(timeValidation, "Invalid time format (HH:mm)");
                isValid = false;
            }
        }
        
        // Check donation
        try {
            double donation = Double.parseDouble(donationField.getText());
            if (donation < 0) {
                showValidationError(donationValidation, "Donation amount cannot be negative");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showValidationError(donationValidation, "Invalid donation amount");
            isValid = false;
        }
        
        // Check price
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                showValidationError(priceValidation, "Price cannot be negative");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showValidationError(priceValidation, "Invalid price");
            isValid = false;
        }
        
        // Check date
        if (datePicker.getValue() == null) {
            showValidationError(dateValidation, "Date is required");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            showValidationError(dateValidation, "Date cannot be in the past");
            isValid = false;
        }
        
        return isValid;
    }

    public void setEvent(Event event) {
        this.event = event;
        populateFields();
    }

    private void populateFields() {
        titleField.setText(event.getTitre());
        descriptionField.setText(event.getDescription());
        locationField.setText(event.getLieu());
        ticketsSpinner.getValueFactory().setValue(event.getNombreBillets());
        imageField.setText(event.getImage());
        timeField.setText(event.getTimestart() != null ? event.getTimestart().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
        missionField.setText(event.getEvent_mission());
        donationField.setText(String.format("%.2f", event.getDonation_objective()));
        priceField.setText(String.format("%.2f", event.getSeatprice()));
        datePicker.setValue(event.getDateEvenement() != null ? event.getDateEvenement().toLocalDate() : null);
    }

    @FXML
    private void handleSave() {
        if (!isFormValid()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please correct the errors before saving.");
            return;
        }

        try {
            // Parse time
            LocalTime time = null;
            if (!timeField.getText().trim().isEmpty()) {
                time = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            }

            // Get date
            LocalDate date = datePicker.getValue();
            LocalDateTime dateTime = date.atTime(time != null ? time : LocalTime.MIDNIGHT);

            // Update event object
            event.setTitre(titleField.getText().trim());
            event.setDescription(descriptionField.getText().trim());
            event.setLieu(locationField.getText().trim());
            event.setNombreBillets(ticketsSpinner.getValue());
            event.setImage(imageField.getText().trim());
            event.setTimestart(time);
            event.setEvent_mission(missionField.getText().trim());
            event.setDonation_objective(Double.parseDouble(donationField.getText()));
            event.setSeatprice(Double.parseDouble(priceField.getText()));
            event.setDateEvenement(dateTime);

            // Save to database
            eventService.update(event);
            isSaved = true;

            // Close the dialog
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save event", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        showAlert(alertType, title, null, content);
    }

    public boolean isSaved() {
        return isSaved;
    }
} 
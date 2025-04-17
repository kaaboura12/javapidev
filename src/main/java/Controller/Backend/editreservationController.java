package Controller.Backend;

import Models.Event;
import Models.Reservation;
import Models.User;
import Services.EventService;
import Services.ReservationService;
import Services.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class editreservationController implements Initializable {

    // Define an interface for refresh callback
    public interface RefreshCallback {
        void onRefresh();
    }
    
    @FXML
    private Label reservationIdLabel;
    
    @FXML
    private ComboBox<User> userComboBox;
    
    @FXML
    private ComboBox<Event> eventComboBox;
    
    @FXML
    private DatePicker reservationDatePicker;
    
    @FXML
    private Spinner<Integer> seatsSpinner;
    
    @FXML
    private TextField totalAmountField;
    
    @FXML
    private ComboBox<String> statusComboBox;
    
    @FXML
    private Label userValidationLabel;
    
    @FXML
    private Label eventValidationLabel;
    
    @FXML
    private Label dateValidationLabel;
    
    @FXML
    private Label seatsValidationLabel;
    
    @FXML
    private Label amountValidationLabel;
    
    @FXML
    private Label statusValidationLabel;
    
    private ReservationService reservationService;
    private UserService userService;
    private EventService eventService;
    
    private Reservation currentReservation;
    
    // Add a callback field
    private RefreshCallback refreshCallback;
    
    // Add a setter for the callback
    public void setRefreshCallback(RefreshCallback callback) {
        this.refreshCallback = callback;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reservationService = new ReservationService();
        userService = new UserService();
        eventService = new EventService();
        
        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(
                "Pending", "Confirmed", "Cancelled", "Completed"));
        
        // Initialize spinner for seats
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        seatsSpinner.setValueFactory(valueFactory);
        
        // Set up user combo box
        setupUserComboBox();
        
        // Set up event combo box
        setupEventComboBox();
        
        // Set up event listener for seats spinner to recalculate total amount
        seatsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> 
            calculateTotalAmount());
        
        // Set up event listener for event selection to recalculate total amount
        eventComboBox.valueProperty().addListener((obs, oldValue, newValue) -> 
            calculateTotalAmount());
        
        // Clear validation labels
        clearValidationLabels();
    }
    
    public void setReservation(Reservation reservation) {
        this.currentReservation = reservation;
        
        // Populate form fields with reservation data
        reservationIdLabel.setText(String.valueOf(reservation.getId_reservation()));
        userComboBox.setValue(reservation.getUser());
        eventComboBox.setValue(reservation.getEvent());
        
        if (reservation.getReservation_date() != null) {
            reservationDatePicker.setValue(reservation.getReservation_date());
        }
        
        seatsSpinner.getValueFactory().setValue(reservation.getSeats_reserved());
        totalAmountField.setText(String.valueOf(reservation.getTotal_amount()));
        statusComboBox.setValue("Confirmed"); // Default status since it's not in the model
    }
    
    private void setupUserComboBox() {
        try {
            List<User> users = userService.findAll();
            userComboBox.setItems(FXCollections.observableArrayList(users));
            
            // Set cell factory for displaying user names
            userComboBox.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getNom() + " " + user.getPrenom());
                    }
                }
            });
            
            // Set converter for selected value display
            userComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    if (user == null) {
                        return null;
                    }
                    return user.getNom() + " " + user.getPrenom();
                }
                
                @Override
                public User fromString(String string) {
                    // Not needed for this use case
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load users: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void setupEventComboBox() {
        try {
            List<Event> events = eventService.findAll();
            eventComboBox.setItems(FXCollections.observableArrayList(events));
            
            // Set cell factory for displaying event names
            eventComboBox.setCellFactory(param -> new ListCell<Event>() {
                @Override
                protected void updateItem(Event event, boolean empty) {
                    super.updateItem(event, empty);
                    if (empty || event == null) {
                        setText(null);
                    } else {
                        setText(event.getTitre());
                    }
                }
            });
            
            // Set converter for selected value display
            eventComboBox.setConverter(new StringConverter<Event>() {
                @Override
                public String toString(Event event) {
                    if (event == null) {
                        return null;
                    }
                    return event.getTitre();
                }
                
                @Override
                public Event fromString(String string) {
                    // Not needed for this use case
                    return null;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load events: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void calculateTotalAmount() {
        Event selectedEvent = eventComboBox.getValue();
        Integer seats = seatsSpinner.getValue();
        
        if (selectedEvent != null && seats != null) {
            double pricePerSeat = selectedEvent.getSeatprice();
            double total = pricePerSeat * seats;
            totalAmountField.setText(String.format("%.2f", total));
        }
    }
    
    private void clearValidationLabels() {
        userValidationLabel.setText("");
        eventValidationLabel.setText("");
        dateValidationLabel.setText("");
        seatsValidationLabel.setText("");
        amountValidationLabel.setText("");
        statusValidationLabel.setText("");
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        clearValidationLabels();
        
        if (userComboBox.getValue() == null) {
            userValidationLabel.setText("Please select a user");
            isValid = false;
        }
        
        if (eventComboBox.getValue() == null) {
            eventValidationLabel.setText("Please select an event");
            isValid = false;
        }
        
        if (reservationDatePicker.getValue() == null) {
            dateValidationLabel.setText("Please select a date");
            isValid = false;
        } else if (reservationDatePicker.getValue().isBefore(LocalDate.now())) {
            dateValidationLabel.setText("Date cannot be in the past");
            isValid = false;
        }
        
        if (seatsSpinner.getValue() <= 0) {
            seatsValidationLabel.setText("Seats must be greater than 0");
            isValid = false;
        }
        
        try {
            double amount = Double.parseDouble(totalAmountField.getText());
            if (amount <= 0) {
                amountValidationLabel.setText("Amount must be greater than 0");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            amountValidationLabel.setText("Invalid amount format");
            isValid = false;
        }
        
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) {
            statusValidationLabel.setText("Please select a status");
            isValid = false;
        }
        
        return isValid;
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        // Update reservation object
        currentReservation.setUser(userComboBox.getValue());
        currentReservation.setEvent(eventComboBox.getValue());
        currentReservation.setReservation_date(reservationDatePicker.getValue());
        currentReservation.setSeats_reserved(seatsSpinner.getValue());
        currentReservation.setTotal_amount(Double.parseDouble(totalAmountField.getText()));
        // Status is not in the model, so we don't set it
        
        // Save the updated reservation
        try {
            reservationService.update(currentReservation);
            
            // Show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Reservation updated successfully!");
            alert.showAndWait();
            
            // Trigger the refresh callback before closing if it exists
            if (refreshCallback != null) {
                refreshCallback.onRefresh();
            }
            
            // Navigate back to reservation management
            navigateToReservationManagement(event);
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to update reservation: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        // Confirm before canceling
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Cancel");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to cancel? Any unsaved changes will be lost.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            // Trigger the refresh callback before closing if it exists
            if (refreshCallback != null) {
                refreshCallback.onRefresh();
            }
            
            navigateToReservationManagement(event);
        }
    }
    
    private void navigateToReservationManagement(ActionEvent event) {
        // Simply close this window
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
} 
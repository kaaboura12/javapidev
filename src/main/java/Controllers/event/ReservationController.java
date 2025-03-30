package Controllers.event;

import Controllers.BasefrontController;
import Models.Event;
import Models.Reservation;
import Models.User;
import Services.EventService;
import Services.ReservationService;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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
        
        // Add listener to update total price when seats change
        seatsSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, 
                              Integer oldValue, Integer newValue) {
                updateTotalPrice();
            }
        });
        
        // Add listener for terms checkbox to enable/disable confirm button
        termsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, 
                              Boolean oldValue, Boolean newValue) {
                updateConfirmButtonState();
            }
        });
        
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
     * Handle confirm reservation button click
     */
    @FXML
    private void handleConfirmReservation() {
        if (!termsCheckBox.isSelected()) {
            statusMessageLabel.setText("Please agree to the terms and conditions.");
            return;
        }
        
        if (event == null) {
            statusMessageLabel.setText("Error: No event selected");
            return;
        }
        
        if (currentUser == null) {
            statusMessageLabel.setText("Error: You must be logged in to make a reservation");
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
                statusMessageLabel.setText("Error: Not enough seats available");
                return;
            }
            
            // Create reservation
            Reservation reservation = new Reservation(
                    currentUser, 
                    event, 
                    LocalDate.now(), 
                    seatsToReserve, 
                    totalAmount
            );
            
            // Save to database
            reservationService.insert(reservation);
            
            // Show success message
            statusMessageLabel.setText("Reservation confirmed successfully! Your booking is complete.");
            
            // Disable form elements to prevent duplicate reservations
            termsCheckBox.setDisable(true);
            seatsSpinner.setDisable(true);
            confirmButton.setDisable(true);
            
            // Update available seats
            int newAvailable = available - seatsToReserve;
            availableSeatsLabel.setText(String.valueOf(newAvailable));
        } catch (SQLException e) {
            statusMessageLabel.setText("Error: Failed to save reservation");
            System.err.println("Error saving reservation: " + e.getMessage());
        }
    }
} 
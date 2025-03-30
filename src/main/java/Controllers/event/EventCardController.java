package Controllers.event;

import Models.Event;
import Utils.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EventCardController implements Initializable {
    
    @FXML
    private Label eventDay;
    
    @FXML
    private Label eventMonth;
    
    @FXML
    private Label eventTitle;
    
    @FXML
    private Label eventLocation;
    
    @FXML
    private Button getTicketButton;
    
    @FXML
    private Button donateButton;
    
    @FXML
    private ImageView eventImage;
    
    private Event event;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add event handlers
        if (getTicketButton != null) {
            getTicketButton.setOnAction(e -> handleGetTicket());
        }
        
        if (donateButton != null) {
            donateButton.setOnAction(e -> handleDonation());
        }
    }
    
    /**
     * Set the event data for this card
     * 
     * @param event The event to display
     */
    public void setEvent(Event event) {
        this.event = event;
        updateUI();
    }
    
    /**
     * Update UI with event data
     */
    private void updateUI() {
        if (event == null) return;
        
        // Format date
        if (eventDay != null && event.getDateEvenement() != null) {
            eventDay.setText(event.getDateEvenement().format(DateTimeFormatter.ofPattern("dd")));
        }
        
        if (eventMonth != null && event.getDateEvenement() != null) {
            eventMonth.setText(event.getDateEvenement().format(DateTimeFormatter.ofPattern("MMMM, yyyy")));
        }
        
        // Set title and location
        if (eventTitle != null) {
            eventTitle.setText(event.getTitre());
        }
        
        if (eventLocation != null) {
            eventLocation.setText(event.getLieu());
        }
        
        // Load image
        if (eventImage != null) {
            loadEventImage();
        }
    }
    
    /**
     * Load the event image
     */
    private void loadEventImage() {
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                File imageFile = new File(event.getImage());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    eventImage.setImage(image);
                } else {
                    // Try to load from resources
                    URL imageUrl = getClass().getResource(event.getImage());
                    if (imageUrl != null) {
                        Image image = new Image(imageUrl.toString());
                        eventImage.setImage(image);
                    } else {
                        // Load default image
                        loadDefaultImage();
                    }
                }
            } else {
                // Load default image
                loadDefaultImage();
            }
        } catch (Exception e) {
            // Load default image on error
            loadDefaultImage();
        }
    }
    
    /**
     * Load default image when event image is not available
     */
    private void loadDefaultImage() {
        try {
            URL defaultImageUrl = getClass().getResource("/Assets/event_default.jpg");
            if (defaultImageUrl != null) {
                Image defaultImage = new Image(defaultImageUrl.toString());
                eventImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            // Ignore if default image can't be loaded
        }
    }
    
    /**
     * Handle get ticket button click
     */
    private void handleGetTicket() {
        if (event == null) return;
        
        try {
            // Get the root StackPane from parent controllers
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                // Load the reservation view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/reservation.fxml"));
                Parent reservationView = loader.load();
                
                // Get the controller and set the event
                ReservationController controller = loader.getController();
                controller.setEvent(event);
                
                // Replace the content in the main content area
                contentArea.getChildren().clear();
                contentArea.getChildren().add(reservationView);
            } else {
                System.err.println("Content area not found for navigation");
                openInNewWindow();
            }
        } catch (IOException e) {
            System.err.println("Error navigating to reservation view: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to opening in a new window
            openInNewWindow();
        }
    }
    
    /**
     * Find the content area in parent containers
     * This allows navigation within the main application layout
     * 
     * @return The content area StackPane or null if not found
     */
    private StackPane findContentArea() {
        // Try to find contentArea through parent hierarchy
        Parent parent = getTicketButton.getScene().getRoot();
        
        // Traverse the scene graph to find a StackPane with id "contentArea"
        return findStackPaneById(parent, "contentArea");
    }
    
    /**
     * Recursively search for a StackPane with specific ID
     * 
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
     * Fallback method to open reservation in a new window
     */
    private void openInNewWindow() {
        try {
            // Load the reservation view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/reservation.fxml"));
            Parent reservationView = loader.load();
            
            // Get the controller and set the event
            ReservationController controller = loader.getController();
            controller.setEvent(event);
            
            // Create a new stage
            Stage stage = new Stage();
            stage.setTitle("Reserve Tickets - " + event.getTitre());
            stage.setScene(new Scene(reservationView));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error opening reservation in new window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle donation button click
     */
    private void handleDonation() {
        if (event == null) return;
        
        try {
            // Get the root StackPane from parent controllers
            StackPane contentArea = findContentArea();
            if (contentArea != null) {
                // Here you would load the donation view
                // For example, if you had a donation.fxml:
                // FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/donation.fxml"));
                // Parent donationView = loader.load();
                // 
                // Get the controller and set the event
                // DonationController controller = loader.getController();
                // controller.setEvent(event);
                // 
                // Replace the content in the main content area
                // contentArea.getChildren().clear();
                // contentArea.getChildren().add(donationView);
                
                // For now, just show a status message
                System.out.println("Donate to event: " + event.getTitre());
                showDonationDialog();
            } else {
                System.err.println("Content area not found for navigation");
                showDonationDialog();
            }
        } catch (Exception e) {
            System.err.println("Error navigating to donation view: " + e.getMessage());
            e.printStackTrace();
            showDonationDialog();
        }
    }
    
    /**
     * Show a simple donation dialog as a placeholder
     */
    private void showDonationDialog() {
        try {
            // Create a simple alert dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Donation");
            alert.setHeaderText("Donate to " + event.getTitre());
            alert.setContentText("Thank you for your interest in supporting this event! " +
                    "The donation feature will be implemented soon.\n\n" +
                    "Event mission: " + (event.getEvent_mission() != null ? 
                            event.getEvent_mission() : "Support this wonderful cause!") + "\n" +
                    "Donation goal: $" + event.getDonation_objective());
            
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing donation dialog: " + e.getMessage());
        }
    }
} 
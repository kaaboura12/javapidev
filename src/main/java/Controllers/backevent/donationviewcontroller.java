package Controllers.backevent;

import Models.Donation;
import Models.Event;
import Models.User;
import Services.DonationService;
import Services.EventService;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class donationviewcontroller implements Initializable {
    
    // Services
    private DonationService donationService;
    private EventService eventService;
    private UserService userService;
    
    // Labels for donation details
    @FXML private Label donationIdLabel;
    @FXML private Label donorNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label amountLabel;
    @FXML private Label dateLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label phoneNumberLabel;
    @FXML private Label eventNameLabel;
    @FXML private Label eventDateLabel;
    @FXML private Label eventLocationLabel;
    @FXML private Label statusLabel;
    
    // Buttons
    @FXML private Button backButton;
    @FXML private Button deleteButton;
    
    // The donation being displayed
    private Donation currentDonation;
    private int donationId;
    
    // DateFormatter for consistent date display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize services
        donationService = new DonationService();
        eventService = new EventService();
        userService = new UserService();
        
        // Set up button actions
        if (backButton != null) {
            backButton.setOnAction(event -> handleBack());
        }
        
        if (deleteButton != null) {
            deleteButton.setOnAction(event -> handleDelete());
        }
    }
    
    /**
     * Load donation details by ID
     * @param id The donation ID to load
     */
    public void loadDonationDetails(int id) {
        this.donationId = id;
        try {
            currentDonation = donationService.findById(id);
            if (currentDonation != null) {
                displayDonationDetails();
            } else {
                showError("Donation not found");
            }
        } catch (SQLException e) {
            showError("Error loading donation: " + e.getMessage());
        }
    }
    
    /**
     * Display donation details in the UI
     */
    private void displayDonationDetails() {
        if (currentDonation == null) return;
        
        // Set basic donation details
        donationIdLabel.setText(String.valueOf(currentDonation.getIddon()));
        donorNameLabel.setText(currentDonation.getDonorname());
        emailLabel.setText(currentDonation.getEmail());
        amountLabel.setText(String.format("%.2f", currentDonation.getMontant()));
        
        // Format and display date
        if (currentDonation.getDate() != null) {
            dateLabel.setText(currentDonation.getDate().format(dateFormatter));
        } else {
            dateLabel.setText("N/A");
        }
        
        paymentMethodLabel.setText(currentDonation.getPayment_method());
        phoneNumberLabel.setText(currentDonation.getNum_tlf());
        
        // Get event information if available
        Event event = currentDonation.getEvent();
        if (event == null && currentDonation.getIdevent() > 0) {
            try {
                event = eventService.findById(currentDonation.getIdevent());
                currentDonation.setEvent(event);
            } catch (SQLException e) {
                System.err.println("Failed to load event: " + e.getMessage());
            }
        }
        
        // Display event information
        if (event != null) {
            eventNameLabel.setText(event.getTitre());
            if (event.getDateEvenement() != null) {
                eventDateLabel.setText(event.getDateEvenement().format(dateFormatter));
            } else {
                eventDateLabel.setText("N/A");
            }
            eventLocationLabel.setText(event.getLieu());
        } else {
            eventNameLabel.setText("N/A");
            eventDateLabel.setText("N/A");
            eventLocationLabel.setText("N/A");
        }
    }
    
    /**
     * Handle back button action
     */
    private void handleBack() {
        try {
            // Try to find the main content area (AnchorPane with ID "contentArea")
            AnchorPane contentArea = findContentArea(backButton);
            
            if (contentArea != null) {
                // Load donation list view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backdonation/donationbacklist.fxml"));
                Parent root = loader.load();
                
                // Set anchors for the new view
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                
                // Clear content area and add donation list view
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
                
                // Update the title if possible
                updatePageTitle("Donations");
            } else {
                // Fallback: Close the current stage if in a separate window
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            showError("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find the main content area in parent hierarchy
     * @param node The starting node to search from
     * @return The content area AnchorPane or null if not found
     */
    private AnchorPane findContentArea(Node node) {
        if (node == null) return null;
        
        // Walk up the parent hierarchy
        Parent parent = node.getParent();
        while (parent != null) {
            // First, check if any children or the parent itself is the content area
            if (parent instanceof AnchorPane && "contentArea".equals(parent.getId())) {
                return (AnchorPane) parent;
            }
            
            // Look for contentArea in all scenes
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof AnchorPane && "contentArea".equals(child.getId())) {
                    return (AnchorPane) child;
                }
                
                // Check if this child has children (recursive)
                if (child instanceof Parent) {
                    AnchorPane result = searchChildren((Parent) child);
                    if (result != null) {
                        return result;
                    }
                }
            }
            
            // Try parent's parent
            if (parent.getParent() != null) {
                parent = parent.getParent();
            } else {
                // Reached the root, try one more approach
                if (parent.getScene() != null && parent.getScene().getRoot() != null) {
                    // Try searching the scene root
                    AnchorPane result = searchChildren(parent.getScene().getRoot());
                    if (result != null) {
                        return result;
                    }
                    break;
                } else {
                    break;
                }
            }
        }
        return null;
    }
    
    /**
     * Search children of a parent node for the content area
     * @param parent The parent to search within
     * @return The content area AnchorPane or null if not found
     */
    private AnchorPane searchChildren(Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof AnchorPane && "contentArea".equals(child.getId())) {
                return (AnchorPane) child;
            }
            
            if (child instanceof Parent) {
                AnchorPane result = searchChildren((Parent) child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Try to update the page title in the parent controller
     * @param title The title to set
     */
    private void updatePageTitle(String title) {
        try {
            // Find the scene
            Scene scene = backButton.getScene();
            if (scene == null) return;
            
            // Find the root
            Parent root = scene.getRoot();
            if (root == null) return;
            
            // Look for the page title label
            Label pageTitleLabel = (Label) root.lookup("#pageTitle");
            if (pageTitleLabel != null) {
                pageTitleLabel.setText(title);
            }
        } catch (Exception e) {
            // Silently ignore - title update is not critical
            System.err.println("Could not update page title: " + e.getMessage());
        }
    }
    
    /**
     * Handle delete button action
     */
    private void handleDelete() {
        if (currentDonation != null) {
            try {
                donationService.delete(currentDonation);
                System.out.println("Donation deleted successfully");
                handleBack(); // Go back after deletion
            } catch (SQLException e) {
                showError("Error deleting donation: " + e.getMessage());
            }
        }
    }
    
    /**
     * Display error message
     * @param message The error message to display
     */
    private void showError(String message) {
        System.err.println(message);
        // In a real application, show a proper error dialog to the user
    }
}

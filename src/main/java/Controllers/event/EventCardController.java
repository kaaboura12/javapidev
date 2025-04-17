package Controllers.event;

import Models.Event;
import Utils.ViewLoader;
import Views.event.Eventdetails;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.text.NumberFormat;
import java.util.Locale;

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
    
    @FXML
    private HBox eventLocationHBox;
    
    // New FXML elements
    @FXML
    private Label eventStatus;
    
    @FXML
    private Label eventTime;
    
    @FXML
    private HBox eventTimeHBox;
    
    @FXML
    private Label eventDescription;
    
    @FXML
    private Label eventPrice;
    
    @FXML
    private Label availableSeats;
    
    private Event event;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Add event handlers
        if (getTicketButton != null) {
            getTicketButton.setOnAction(e -> handleGetTicket());
            addButtonHoverEffect(getTicketButton);
        }
        
        if (donateButton != null) {
            donateButton.setOnAction(e -> handleDonation());
            addButtonHoverEffect(donateButton);
        }
        
        // Apply styles to event title
        if (eventTitle != null) {
            eventTitle.setWrapText(true);
            eventTitle.setTextAlignment(TextAlignment.LEFT);
            
            // Make title clickable with hand cursor
            eventTitle.setCursor(Cursor.HAND);
            
            // Add click event to title to open event details
            eventTitle.setOnMouseClicked(this::handleTitleClick);
            
            // Add hover effect for title
            eventTitle.setOnMouseEntered(e -> {
                eventTitle.setStyle("-fx-underline: true; -fx-text-fill: #3a5ca2;");
            });
            
            eventTitle.setOnMouseExited(e -> {
                eventTitle.setStyle("-fx-underline: false; -fx-text-fill: inherit;");
            });
        }
        
        // Initialize location icon if HBox exists
        initializeLocationIcon();
        
        // Initialize time icon if HBox exists
        initializeTimeIcon();
    }
    
    /**
     * Handle event title click to open event details page
     */
    private void handleTitleClick(MouseEvent event) {
        if (this.event != null) {
            // Open the event details page
            Eventdetails detailsPage = new Eventdetails();
            detailsPage.show(this.event.getIdevent());
        }
    }
    
    /**
     * Initialize location icon in the HBox
     */
    private void initializeLocationIcon() {
        if (eventLocation != null) {
            try {
                // Create location icon
                ImageView locationIcon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/icons/location-icon.png")));
                if (locationIcon.getImage().isError()) {
                    // Try another resource path
                    locationIcon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/location-icon.png")));
                }
                
                // Set icon size
                locationIcon.setFitWidth(16);
                locationIcon.setFitHeight(16);
                locationIcon.setPreserveRatio(true);
                
                // Find parent HBox and add icon
                HBox parent = (HBox) eventLocation.getParent();
                if (parent != null && parent.getChildren().size() == 1) {
                    parent.getChildren().add(0, locationIcon);
                    parent.setAlignment(Pos.CENTER_LEFT);
                    parent.setSpacing(5);
                }
            } catch (Exception e) {
                // Silently fail if icon cannot be loaded
                System.out.println("Could not load location icon: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize time icon in the HBox
     */
    private void initializeTimeIcon() {
        if (eventTimeHBox != null) {
            try {
                // Create time icon
                ImageView timeIcon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/icons/clock.png")));
                if (timeIcon.getImage().isError()) {
                    // Try another resource path
                    timeIcon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/clock.png")));
                }
                
                // Set icon size
                timeIcon.setFitWidth(16);
                timeIcon.setFitHeight(16);
                timeIcon.setPreserveRatio(true);
                
                // Add the icon to the HBox
                if (eventTimeHBox.getChildren().isEmpty()) {
                    // If HBox is empty, add the icon
                    eventTimeHBox.getChildren().add(0, timeIcon);
                } else if (eventTimeHBox.getChildren().get(0) instanceof javafx.scene.layout.Region) {
                    // Replace Region with the actual icon if it exists
                    eventTimeHBox.getChildren().set(0, timeIcon);
                }
                
                // Make sure eventTime label is properly added to HBox if it's not already there
                if (eventTimeHBox.getChildren().size() < 2) {
                    // If label isn't in HBox yet (or was removed), add it back
                    if (eventTime != null && !eventTimeHBox.getChildren().contains(eventTime)) {
                        eventTimeHBox.getChildren().add(eventTime);
                    }
                }
                
                eventTimeHBox.setAlignment(Pos.CENTER_LEFT);
                eventTimeHBox.setSpacing(5); // Ensure proper spacing between icon and text
            } catch (Exception e) {
                // Log error
                System.out.println("Could not load time icon: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Add hover effect to buttons
     */
    private void addButtonHoverEffect(Button button) {
        // Store original style
        String originalStyle = button.getStyle();
        
        // Add glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(Color.valueOf("#ff7e5f80"));
        glow.setWidth(20);
        glow.setHeight(20);
        glow.setRadius(10);
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;");
            button.setEffect(glow);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
            button.setEffect(null);
        });
        
        // Add press effect
        button.setOnMousePressed(e -> {
            button.setStyle(originalStyle + "-fx-scale-x: 0.95; -fx-scale-y: 0.95;");
        });
        
        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                button.setStyle(originalStyle + "-fx-scale-x: 1.1; -fx-scale-y: 1.1;");
                button.setEffect(glow);
            } else {
                button.setStyle(originalStyle);
                button.setEffect(null);
            }
        });
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
            // For modern view, use short month format
            eventMonth.setText(event.getDateEvenement().format(DateTimeFormatter.ofPattern("MMM")));
        }
        
        // Set title and location
        if (eventTitle != null) {
            eventTitle.setText(event.getTitre());
            
            // Add a subtle shadow effect to make text pop
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.color(0, 0, 0, 0.3));
            shadow.setRadius(2);
            shadow.setOffsetX(0);
            shadow.setOffsetY(1);
            eventTitle.setEffect(shadow);
        }
        
        if (eventLocation != null) {
            eventLocation.setText(event.getLieu());
        }
        
        // Set event time
        if (eventTime != null) {
            try {
                if (event.getTimestart() != null) {
                    String formattedTime = event.getTimestart().format(DateTimeFormatter.ofPattern("h:mm a"));
                    eventTime.setText(formattedTime);
                    
                    // Make the time HBox visible
                    if (eventTimeHBox != null) {
                        eventTimeHBox.setVisible(true);
                        eventTimeHBox.setManaged(true);
                    }
                } else {
                    // Hide time section if no time available
                    if (eventTimeHBox != null) {
                        eventTimeHBox.setVisible(false);
                        eventTimeHBox.setManaged(false);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error formatting event time: " + e.getMessage());
                // Set a default value in case of error
                eventTime.setText("Time TBA");
            }
        }
        
        // Set event description (not used in modern design but keep for backward compatibility)
        if (eventDescription != null) {
            String description = event.getDescription();
            // Truncate description if too long for card display
            if (description != null && description.length() > 150) {
                description = description.substring(0, 147) + "...";
            }
            eventDescription.setText(description != null ? description : "No description available");
        }
        
        // Set price
        if (eventPrice != null) {
            eventPrice.setText(formatPrice(event.getSeatprice()));
            
            // Add subtle glow effect to price
            DropShadow glow = new DropShadow();
            glow.setColor(Color.valueOf("#feb47b40"));
            glow.setRadius(3);
            eventPrice.setEffect(glow);
        }
        
        // Set available seats
        if (availableSeats != null) {
            int available = event.getAvailableSeats();
            availableSeats.setText(available + (available == 1 ? " seat" : " seats") + " left");
        }
        
        // Set event status
        if (eventStatus != null) {
            // Check if event is in the future
            if (event.getDateEvenement() != null) {
                boolean isFuture = event.getDateEvenement().isAfter(java.time.LocalDateTime.now());
                eventStatus.setText(isFuture ? "Coming Soon" : "Passed");
                
                // Change style based on status
                if (!isFuture) {
                    eventStatus.getStyleClass().add("event-status-passed");
                }
                
                // Add shadow effect
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.color(0, 0, 0, 0.3));
                shadow.setRadius(3);
                eventStatus.setEffect(shadow);
            }
        }
        
        // Load image
        if (eventImage != null) {
            loadEventImage();
            
            // Add shadow effect to image
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(5.0);
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(1);
            dropShadow.setColor(Color.color(0, 0, 0, 0.3));
            eventImage.setEffect(dropShadow);
        }
        
        // Update ticket button text and style based on modern design
        if (getTicketButton != null) {
            boolean hasAvailableSeats = event.getAvailableSeats() > 0;
            getTicketButton.setDisable(!hasAvailableSeats);
            
            // For modern style, use DETAILS button text
            if (getTicketButton.getStyleClass().contains("get-ticket-button-modern")) {
                getTicketButton.setText("DETAILS");
            } else if (!hasAvailableSeats) {
                getTicketButton.setText("SOLD OUT");
                getTicketButton.getStyleClass().add("sold-out-button");
            }
        }
    }
    
    /**
     * Format price for display
     */
    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(price);
    }
    
    /**
     * Load the event image
     */
    private void loadEventImage() {
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                System.out.println("Attempting to load image: " + event.getImage());
                
                // First try to load as an absolute file path
                File imageFile = new File(event.getImage());
                if (imageFile.exists()) {
                    System.out.println("Loading image as file: " + imageFile.getAbsolutePath());
                    Image image = new Image(imageFile.toURI().toString());
                    eventImage.setImage(image);
                    return;
                } else {
                    System.out.println("File doesn't exist at path: " + imageFile.getAbsolutePath());
                }
                
                // If that doesn't work, try as a resource path
                String resourcePath = event.getImage();
                
                // If the path starts with "Uploads/" (without leading slash)
                // Add the leading slash for resource loading
                if (resourcePath.startsWith("Uploads/")) {
                    resourcePath = "/" + resourcePath;
                    System.out.println("Trying resource path: " + resourcePath);
                }
                
                // Try loading from resources
                URL imageUrl = getClass().getResource(resourcePath);
                if (imageUrl != null) {
                    System.out.println("Loading image from resources: " + imageUrl);
                    Image image = new Image(imageUrl.toString());
                    eventImage.setImage(image);
                    return;
                } else {
                    System.out.println("Resource not found: " + resourcePath);
                }
                
                // Try with direct path to src/main/resources folder
                String normalized = event.getImage().replace('/', File.separatorChar);
                File resourcesFile = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + normalized);
                System.out.println("Trying resources file: " + resourcesFile.getAbsolutePath());
                if (resourcesFile.exists()) {
                    System.out.println("Loading from resources file: " + resourcesFile.getAbsolutePath());
                    Image image = new Image(resourcesFile.toURI().toString());
                    eventImage.setImage(image);
                    return;
                } else {
                    System.out.println("Resources file doesn't exist: " + resourcesFile.getAbsolutePath());
                }
                
                // Try using file protocol with absolute workspace path
                try {
                    // Get the current working directory (project root)
                    String workingDir = System.getProperty("user.dir");
                    String imagePath = event.getImage().replace('/', File.separatorChar);
                    File workspaceFile = new File(workingDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + imagePath);
                    System.out.println("Trying workspace path: " + workspaceFile.getAbsolutePath());
                    
                    if (workspaceFile.exists()) {
                        System.out.println("Loading from workspace file: " + workspaceFile.getAbsolutePath());
                        Image image = new Image(workspaceFile.toURI().toString());
                        eventImage.setImage(image);
                        return;
                    } else {
                        System.out.println("Workspace file doesn't exist: " + workspaceFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("Error with workspace path approach: " + e.getMessage());
                }
                
                // If all attempts fail, load default image
                System.out.println("All image loading attempts failed, using default image");
                loadDefaultImage();
            } else {
                System.out.println("Event has no image path, using default image");
                // Load default image
                loadDefaultImage();
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
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
                // Load the donation view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/donation.fxml"));
                Parent donationView = loader.load();
                
                // Get the controller and set the event
                DonationController controller = loader.getController();
                controller.setEvent(event);
                
                // Replace the content in the main content area
                contentArea.getChildren().clear();
                contentArea.getChildren().add(donationView);
            } else {
                System.err.println("Content area not found for navigation");
                openDonationInNewWindow();
            }
        } catch (Exception e) {
            System.err.println("Error navigating to donation view: " + e.getMessage());
            e.printStackTrace();
            openDonationInNewWindow();
        }
    }
    
    /**
     * Open the donation form in a new window as a fallback
     */
    private void openDonationInNewWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/donation.fxml"));
            Parent donationView = loader.load();
            
            // Get the controller and set the event
            DonationController controller = loader.getController();
            controller.setEvent(event);
            
            // Create and show a new stage
            Stage stage = new Stage();
            stage.setTitle("Donate to " + event.getTitre());
            stage.setScene(new Scene(donationView));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error opening donation in new window: " + e.getMessage());
            e.printStackTrace();
            // Fallback to simple dialog if everything else fails
            showDonationDialog();
        }
    }
    
    /**
     * Show a simple donation dialog as a fallback
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
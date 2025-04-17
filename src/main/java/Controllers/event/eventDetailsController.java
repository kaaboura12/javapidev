package Controllers.event;

import Models.Event;
import Models.Donation;
import Models.Reservation;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class eventDetailsController implements Initializable {
    
    @FXML
    private AnchorPane mainAnchorPane;
    
    @FXML
    private Label lblEventTitle;
    
    @FXML
    private Label lblDescription;
    
    @FXML
    private Label lblDate;
    
    @FXML
    private Label lblTime;
    
    @FXML
    private Label lblLocation;
    
    @FXML
    private Label lblAvailableSeats;
    
    @FXML
    private Label lblPrice;
    
    @FXML
    private Label lblMission;
    
    @FXML
    private Label lblDonationProgress;
    
    @FXML
    private ImageView imgEvent;
    
    @FXML
    private ProgressBar progressDonation;
    
    @FXML
    private Button btnBack;
    
    @FXML
    private Button btnDescriptionDetails;
    
    @FXML
    private Button btnMissionDetails;
    
    @FXML
    private Button btnDonationDetails;
    
    // Store original content for returning back
    private AnchorPane originalContent;
    
    private Event currentEvent;
    private EventService eventService;
    
    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    
    // This will be set by the calling page
    private int eventId;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize service
        eventService = new EventService();
        
        // This is just for testing - in a real app, this would be set by the calling page
        // Set a default event ID for testing
        eventId = 1;
        
        // Apply initial animations
        applyInitialAnimations();
        
        // Load event data
        loadEventData();
    }
    
    // Method to be called by the page that navigates to this one
    public void setEventId(int eventId) {
        this.eventId = eventId;
        loadEventData();
    }
    
    private void applyInitialAnimations() {
        // Create and configure a fade transition for smooth loading
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800));
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setNode(imgEvent);
        fadeIn.play();
    }
    
    private void loadEventData() {
        try {
            // Load event from database
            currentEvent = eventService.findById(eventId);
            
            if (currentEvent != null) {
                // Set event details in UI
                lblEventTitle.setText(currentEvent.getTitre());
                lblDescription.setText(currentEvent.getDescription());
                lblLocation.setText(currentEvent.getLieu());
                lblMission.setText(currentEvent.getEvent_mission());
                
                // Format date and time
                if (currentEvent.getDateEvenement() != null) {
                    lblDate.setText(currentEvent.getDateEvenement().format(dateFormatter));
                }
                
                if (currentEvent.getTimestart() != null) {
                    lblTime.setText(currentEvent.getTimestart().format(timeFormatter));
                }
                
                // Format price
                lblPrice.setText(currencyFormat.format(currentEvent.getSeatprice()));
                
                // Calculate and display available seats
                int totalSeats = currentEvent.getNombreBillets();
                int reservedSeats = currentEvent.getTotalReservedSeats();
                int availableSeats = totalSeats - reservedSeats;
                lblAvailableSeats.setText(availableSeats + " / " + totalSeats);
                
                // Calculate and display donation progress
                double totalDonations = currentEvent.getTotalDonations();
                double donationObjective = currentEvent.getDonation_objective();
                double progressValue = donationObjective > 0 ? totalDonations / donationObjective : 0;
                progressDonation.setProgress(Math.min(progressValue, 1.0));
                lblDonationProgress.setText(currencyFormat.format(totalDonations) + " / " + 
                                           currencyFormat.format(donationObjective));
                
                // Load event image if available
                if (currentEvent.getImage() != null && !currentEvent.getImage().isEmpty()) {
                    System.out.println("Attempting to load image: " + currentEvent.getImage());
                    try {
                        String imagePath = currentEvent.getImage();
                        
                        // Handle database paths that start with "resources/"
                        if (imagePath.startsWith("resources/")) {
                            // Convert to project structure path
                            String projectPath = "src/main/" + imagePath;
                            System.out.println("Trying project path: " + projectPath);
                            
                            File resourceFile = new File(projectPath);
                            if (resourceFile.exists()) {
                                System.out.println("Loading from project path: " + resourceFile.getAbsolutePath());
                                Image image = new Image(resourceFile.toURI().toString());
                                imgEvent.setImage(image);
                                return;
                            }
                            
                            // Try as a classpath resource by removing "resources" prefix
                            String classPathResource = "/" + imagePath.substring("resources/".length());
                            System.out.println("Trying classpath resource: " + classPathResource);
                            
                            URL resourceUrl = getClass().getResource(classPathResource);
                            if (resourceUrl != null) {
                                System.out.println("Loading from classpath: " + resourceUrl);
                                Image image = new Image(resourceUrl.toString());
                                imgEvent.setImage(image);
                                return;
                            }
                        }
                        
                        // Try direct file path
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            System.out.println("Loading from direct path: " + imageFile.getAbsolutePath());
                            Image image = new Image(imageFile.toURI().toString());
                            imgEvent.setImage(image);
                            return;
                        }
                        
                        // Try various project structure paths
                        String[] possiblePaths = {
                            "src/main/resources/Uploads/Events/" + imagePath,
                            "src/main/resources/" + imagePath,
                            imagePath
                        };
                        
                        for (String path : possiblePaths) {
                            System.out.println("Trying path: " + path);
                            File file = new File(path);
                            if (file.exists()) {
                                System.out.println("Loading from: " + file.getAbsolutePath());
                                Image image = new Image(file.toURI().toString());
                                imgEvent.setImage(image);
                                return;
                            }
                        }
                        
                        // Try as classpath resources
                        String[] resourcePaths = {
                            "/Uploads/Events/" + imagePath,
                            "/" + imagePath,
                            imagePath
                        };
                        
                        for (String path : resourcePaths) {
                            System.out.println("Trying resource: " + path);
                            URL url = getClass().getResource(path);
                            if (url != null) {
                                System.out.println("Loading from resource: " + url);
                                Image image = new Image(url.toString());
                                imgEvent.setImage(image);
                                return;
                            }
                        }
                        
                        // If we got here, we couldn't load the image
                        System.out.println("Failed to load image, using default");
                        loadDefaultImage();
                        
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                        e.printStackTrace();
                        loadDefaultImage();
                    }
                } else {
                    loadDefaultImage();
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load event details", e.getMessage());
        }
    }
    
    @FXML
    private void onBackButtonClick() {
        // If we're viewing detail content, return to the main view
        if (originalContent != null) {
            // Replace the current content with the original
            mainAnchorPane.getChildren().clear();
            mainAnchorPane.getChildren().add(originalContent);
            originalContent = null;
            
            // Apply fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), originalContent);
            fadeIn.setFromValue(0.7);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
            // Update button text
            btnBack.setText("← Back");
            return;
        }
        
        // Navigate back to previous screen
        Stage stage = (Stage) btnBack.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void onDescriptionDetailsClick() {
        // Display full description in the same window
        showDetailView("Event Description", "Description of " + currentEvent.getTitre(), getDescriptionContent());
    }
    
    @FXML
    private void onMissionDetailsClick() {
        // Display full mission in the same window
        showDetailView("Event Mission", "Mission Statement for " + currentEvent.getTitre(), getMissionContent());
    }
    
    @FXML
    private void onDonationDetailsClick() {
        // Display donation details in the same window
        showDetailView("Donation Information", "Donation Progress for " + currentEvent.getTitre(), getDonationDetailsContent());
    }
    
    /**
     * Shows detail content in the same window
     */
    private void showDetailView(String title, String header, TextFlow content) {
        // Store the original content for later restoration if not already stored
        if (originalContent == null) {
            // Save all children of the anchor pane
            originalContent = new AnchorPane();
            originalContent.getChildren().addAll(mainAnchorPane.getChildren());
        }
        
        // Create header
        Label headerLabel = new Label(header);
        headerLabel.getStyleClass().add("event-title");
        
        // Change back button text
        btnBack.setText("← Return to Event");
        
        // Create a scrollable content area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(content);
        scrollPane.getStyleClass().add("detail-scroll-pane");
        
        // Create layout for the detail view
        VBox detailLayout = new VBox(15);
        detailLayout.getStyleClass().add("detail-container");
        detailLayout.setPadding(new Insets(20));
        
        // Create header section
        HBox headerSection = new HBox();
        headerSection.getStyleClass().add("header-section");
        headerSection.setPadding(new Insets(20));
        headerSection.getChildren().add(headerLabel);
        
        // Set up the layout
        detailLayout.getChildren().addAll(headerSection, scrollPane);
        
        // Set anchors for the detail layout to fill the available space
        AnchorPane.setTopAnchor(detailLayout, 0.0);
        AnchorPane.setRightAnchor(detailLayout, 0.0);
        AnchorPane.setBottomAnchor(detailLayout, 0.0);
        AnchorPane.setLeftAnchor(detailLayout, 0.0);
        
        // Replace existing content with detail view
        mainAnchorPane.getChildren().clear();
        mainAnchorPane.getChildren().add(btnBack);
        mainAnchorPane.getChildren().add(detailLayout);
        
        // Set button above the detail layout
        AnchorPane.setTopAnchor(btnBack, 20.0);
        AnchorPane.setLeftAnchor(btnBack, 20.0);
        AnchorPane.setTopAnchor(detailLayout, 70.0);
        
        // Apply fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), detailLayout);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    /**
     * Creates content for the description dialog
     */
    private TextFlow getDescriptionContent() {
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("content-text");
        
        Text headerText = new Text("Full Description\n\n");
        headerText.getStyleClass().add("title-label");
        
        Text descriptionContent = new Text(currentEvent.getDescription());
        
        // Add more context if needed for a richer experience
        if (currentEvent.getDescription() != null && !currentEvent.getDescription().isEmpty()) {
            Text additionalInfo = new Text("\n\nThis event is perfect for those interested in community engagement and making a difference. " +
                "Participants will have the opportunity to network with others who share similar interests and values.");
            
            textFlow.getChildren().addAll(headerText, descriptionContent, additionalInfo);
        } else {
            Text noDescription = new Text("No detailed description is available for this event yet.");
            textFlow.getChildren().addAll(headerText, noDescription);
        }
        
        return textFlow;
    }
    
    /**
     * Creates content for the mission dialog
     */
    private TextFlow getMissionContent() {
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("content-text");
        
        Text headerText = new Text("Our Mission\n\n");
        headerText.getStyleClass().add("title-label");
        
        Text missionContent = new Text(currentEvent.getEvent_mission());
        
        // Add more context about the values and goals
        if (currentEvent.getEvent_mission() != null && !currentEvent.getEvent_mission().isEmpty()) {
            Text additionalInfo = new Text("\n\n" +
                "Our Values:\n" +
                "• Community Engagement\n" +
                "• Inclusivity and Diversity\n" +
                "• Environmental Sustainability\n" +
                "• Social Responsibility\n\n" +
                "We believe in creating events that not only entertain but also educate and inspire positive change in our community."
            );
            
            textFlow.getChildren().addAll(headerText, missionContent, additionalInfo);
        } else {
            Text noMission = new Text("No mission statement is available for this event yet.");
            textFlow.getChildren().addAll(headerText, noMission);
        }
        
        return textFlow;
    }
    
    /**
     * Creates content for the donation details dialog
     */
    private TextFlow getDonationDetailsContent() {
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("content-text");
        
        Text headerText = new Text("Donation Information\n\n");
        headerText.getStyleClass().add("title-label");
        
        double totalDonations = currentEvent.getTotalDonations();
        double donationObjective = currentEvent.getDonation_objective();
        double progressPercentage = donationObjective > 0 ? 
            Math.min((totalDonations / donationObjective) * 100, 100) : 0;
        
        Text progressText = new Text(String.format("Current Progress: %.1f%%\n", progressPercentage));
        progressText.setStyle("-fx-font-weight: bold;");
        
        Text amountText = new Text(
            "Total donations: " + currencyFormat.format(totalDonations) + "\n" +
            "Goal: " + currencyFormat.format(donationObjective) + "\n\n"
        );
        
        Text impactHeader = new Text("Your Impact\n");
        impactHeader.setStyle("-fx-font-weight: bold;");
        
        Text impactContent = new Text(
            "Every donation helps us achieve our mission. Here's what your contribution can do:\n\n" +
            "• $25 - Provides resources for one participant\n" +
            "• $50 - Sponsors a student's attendance\n" +
            "• $100 - Covers materials for a workshop\n" +
            "• $500 - Funds a scholarship for underprivileged attendees\n\n" +
            "All donations are tax-deductible. Receipts will be provided for donations over $20."
        );
        
        Text recentHeader = new Text("\nRecent Donors\n");
        recentHeader.setStyle("-fx-font-weight: bold;");
        
        // This would come from your database in a real application
        Text recentContent = new Text(
            "• Anonymous - $100\n" +
            "• John Smith - $50\n" +
            "• Community Foundation - $1,000\n" +
            "• Sarah Johnson - $75\n" +
            "• Local Business Corp. - $500"
        );
        
        textFlow.getChildren().addAll(
            headerText,
            progressText, amountText,
            impactHeader, impactContent,
            recentHeader, recentContent
        );
        
        return textFlow;
    }
    
    /**
     * Load a default placeholder image when the event image is not available
     */
    private void loadDefaultImage() {
        try {
            // Try multiple paths for the default image, starting with Events directory
            String[] possiblePaths = {
                "/Uploads/Events/default.jpg",
                "/Events/default.jpg",
                "/Images/Events/default.jpg",
                "/Images/event_placeholder.png",
                "/Assets/event_default.jpg",
                "/default_image.png"
            };
            
            for (String path : possiblePaths) {
                URL url = getClass().getResource(path);
                if (url != null) {
                    imgEvent.setImage(new Image(url.toString()));
                    return;
                }
            }
            
            // Try loading from the file system directly
            File defaultFile = new File("src/main/resources/Uploads/Events/default.jpg");
            if (defaultFile.exists()) {
                imgEvent.setImage(new Image(defaultFile.toURI().toString()));
                return;
            }
            
            // If all attempts fail, create a simple colored rectangle as placeholder
            System.out.println("Creating placeholder image as no default image was found");
            javafx.scene.image.WritableImage placeholderImage = new javafx.scene.image.WritableImage(450, 300);
            javafx.scene.image.PixelWriter pixelWriter = placeholderImage.getPixelWriter();
            for (int y = 0; y < 300; y++) {
                for (int x = 0; x < 450; x++) {
                    pixelWriter.setColor(x, y, javafx.scene.paint.Color.rgb(30, 30, 48));
                }
            }
            imgEvent.setImage(placeholderImage);
            
        } catch (Exception e) {
            System.err.println("Failed to load any default image: " + e.getMessage());
            // Create a fallback image to avoid crashes
            imgEvent.setImage(new javafx.scene.image.WritableImage(1, 1));
        }
    }
    
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Apply styling
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/Styles/eventdetailsfront.css").toExternalForm());
        
        alert.showAndWait();
    }
}

package Controllers.event;

import Controllers.BasefrontController;
import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ListeventController extends BasefrontController {
    
    @FXML
    private StackPane rootPane;
    
    @FXML
    private FlowPane eventsContainer;
    
    @FXML
    private Pagination eventsPagination;
    
    @FXML
    private VBox eventCardTemplate;
    
    // Event service for database operations
    private EventService eventService;
    
    // List to store all events
    private List<Event> eventsList;
    
    // Number of events to display per page
    private final int EVENTS_PER_PAGE = 6;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        // Initialize service
        eventService = new EventService();
        
        // Load events from database
        loadEvents();
        
        // Set up pagination
        setupPagination();
    }
    
    /**
     * Load events from database
     */
    private void loadEvents() {
        try {
            // Get all upcoming events
            eventsList = eventService.findUpcomingEvents();
            
            if (eventsList.isEmpty()) {
                showNoEventsMessage();
            } else {
                // Calculate total pages
                int pageCount = (int) Math.ceil((double) eventsList.size() / EVENTS_PER_PAGE);
                eventsPagination.setPageCount(pageCount);
                
                // Show first page
                showEventsPage(0);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load events.", e.getMessage());
        }
    }
    
    /**
     * Set up pagination control
     */
    private void setupPagination() {
        eventsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            showEventsPage(newIndex.intValue());
        });
    }
    
    /**
     * Display events for a specific page
     * 
     * @param pageIndex The page index (0-based)
     */
    private void showEventsPage(int pageIndex) {
        // Clear previous events
        eventsContainer.getChildren().clear();
        
        int startIndex = pageIndex * EVENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + EVENTS_PER_PAGE, eventsList.size());
        
        // Create event cards for this page
        for (int i = startIndex; i < endIndex; i++) {
            Node eventCard = createEventCard(eventsList.get(i));
            if (eventCard != null) {
                eventsContainer.getChildren().add(eventCard);
            }
        }
    }
    
    /**
     * Create an event card for display
     * 
     * @param event The event data
     * @return The event card node
     */
    private Node createEventCard(Event event) {
        try {
            // Load the event card template
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/Views/event/event_card.fxml"));
            
            // If the FXML file doesn't exist, create the card programmatically
            if (loader.getLocation() == null) {
                return createEventCardProgrammatically(event);
            }
            
            VBox eventCard = loader.load();
            EventCardController controller = loader.getController();
            controller.setEvent(event);
            
            return eventCard;
        } catch (Exception e) {
            // Fallback to programmatic creation if loading fails
            return createEventCardProgrammatically(event);
        }
    }
    
    /**
     * Create an event card programmatically
     * 
     * @param event The event data
     * @return The event card node
     */
    private Node createEventCardProgrammatically(Event event) {
        try {
            // Clone the template
            VBox eventCard = new VBox();
            eventCard.getStyleClass().add("event-card");
            
            // Date container
            StackPane dateContainer = new StackPane();
            dateContainer.getStyleClass().add("event-date-container");
            
            VBox dateBox = new VBox();
            dateBox.setAlignment(javafx.geometry.Pos.CENTER);
            dateBox.getStyleClass().add("event-date");
            
            // Format the date
            String day = event.getDateEvenement().format(DateTimeFormatter.ofPattern("dd"));
            String month = event.getDateEvenement().format(DateTimeFormatter.ofPattern("MMMM, yyyy"));
            
            Label dayLabel = new Label(day);
            dayLabel.getStyleClass().add("event-day");
            
            Label monthLabel = new Label(month);
            monthLabel.getStyleClass().add("event-month");
            
            dateBox.getChildren().addAll(dayLabel, monthLabel);
            dateContainer.getChildren().add(dateBox);
            
            // Event details
            VBox detailsBox = new VBox();
            detailsBox.getStyleClass().add("event-details");
            detailsBox.setSpacing(10);
            
            Label titleLabel = new Label(event.getTitre());
            titleLabel.getStyleClass().add("event-title");
            titleLabel.setWrapText(true);
            
            HBox locationBox = new HBox();
            locationBox.getStyleClass().add("event-location");
            
            Label locationLabel = new Label(event.getLieu());
            locationLabel.getStyleClass().add("event-location-text");
            
            locationBox.getChildren().add(locationLabel);
            
            HBox actionsBox = new HBox();
            actionsBox.getStyleClass().add("event-actions");
            
            Button ticketButton = new Button("GET TICKET");
            ticketButton.getStyleClass().add("get-ticket-button");
            ticketButton.setOnAction(e -> handleGetTicket(event));
            
            actionsBox.getChildren().add(ticketButton);
            
            detailsBox.getChildren().addAll(titleLabel, locationBox, actionsBox);
            
            // Create event image
            ImageView imageView = new ImageView();
            imageView.getStyleClass().add("event-image");
            
            // Try to load image from path
            try {
                if (event.getImage() != null && !event.getImage().isEmpty()) {
                    File imageFile = new File(event.getImage());
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        imageView.setImage(image);
                    } else {
                        // Try to load from resources
                        URL imageUrl = getClass().getResource(event.getImage());
                        if (imageUrl != null) {
                            Image image = new Image(imageUrl.toString());
                            imageView.setImage(image);
                        } else {
                            // Load default image
                            imageView.setImage(new Image(getClass().getResource("/Assets/event_default.jpg").toString()));
                        }
                    }
                } else {
                    // Load default image
                    imageView.setImage(new Image(getClass().getResource("/Assets/event_default.jpg").toString()));
                }
            } catch (Exception e) {
                // Load default image on error
                try {
                    imageView.setImage(new Image(getClass().getResource("/Assets/event_default.jpg").toString()));
                } catch (Exception ex) {
                    // If default image can't be loaded, leave it empty
                }
            }
            
            imageView.setFitWidth(350);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
            
            // Add all components to the card
            eventCard.getChildren().addAll(dateContainer, detailsBox, imageView);
            
            return eventCard;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Handle get ticket button click
     * 
     * @param event The event to get ticket for
     */
    private void handleGetTicket(Event event) {
        try {
            // Check if tickets are available
            if (event.getAvailableSeats() > 0) {
                // Navigate to ticket reservation page
                // TODO: Implement navigation to ticket reservation page
                showInfoAlert("Ticket Reservation", 
                             "Redirecting to ticket reservation for: " + event.getTitre(),
                             "Available seats: " + event.getAvailableSeats());
            } else {
                showWarningAlert("No Tickets Available", 
                                "Sorry, there are no more tickets available for this event.",
                                "Event: " + event.getTitre());
            }
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to process ticket request.", e.getMessage());
        }
    }
    
    /**
     * Show a message when no events are available
     */
    private void showNoEventsMessage() {
        eventsContainer.getChildren().clear();
        
        VBox messageBox = new VBox();
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPrefHeight(300);
        
        Label messageLabel = new Label("No upcoming events found.");
        messageLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666666;");
        
        messageBox.getChildren().add(messageLabel);
        eventsContainer.getChildren().add(messageBox);
        
        // Hide pagination when no events
        eventsPagination.setVisible(false);
    }
    
    /**
     * Show an information alert
     */
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show a warning alert
     */
    private void showWarningAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
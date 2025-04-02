package Controllers.event;

import Controllers.BasefrontController;
import Models.Event;
import Services.EventService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

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
    private List<Event> eventsList = new ArrayList<>();
    
    // Number of events to display per page
    private final int EVENTS_PER_PAGE = 6;
    
    // Thread pool for loading images
    private ExecutorService executorService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        // Create thread pool for loading images
        executorService = Executors.newFixedThreadPool(3);
        
        // Add a loading indicator
        showLoadingIndicator();
        
        // Initialize service
        eventService = new EventService();
        
        // Load events from database asynchronously
        CompletableFuture.runAsync(this::loadEvents)
            .thenRun(() -> Platform.runLater(() -> {
                hideLoadingIndicator();
                setupPagination();
            }));
        
        // Apply styles to the container
        if (eventsContainer != null) {
            eventsContainer.setHgap(25);
            eventsContainer.setVgap(35);
        }
    }
    
    /**
     * Show loading indicator
     */
    private void showLoadingIndicator() {
        Label loadingLabel = new Label("Loading events...");
        loadingLabel.setId("loadingIndicator");
        loadingLabel.getStyleClass().add("loading-indicator");
        
        // Create a progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(40, 40);
        
        VBox loadingBox = new VBox(10, progressIndicator, loadingLabel);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);
        loadingBox.setStyle("-fx-padding: 30px;");
        
        if (eventsContainer != null) {
            eventsContainer.getChildren().add(loadingBox);
        }
    }
    
    /**
     * Hide loading indicator
     */
    private void hideLoadingIndicator() {
        if (eventsContainer != null) {
            eventsContainer.getChildren().removeIf(node -> 
                node instanceof VBox && ((VBox) node).getChildren().stream()
                    .anyMatch(child -> child instanceof Label && "loadingIndicator".equals(child.getId())));
        }
    }
    
    /**
     * Load events from database
     */
    private void loadEvents() {
        try {
            // Get all upcoming events
            eventsList = eventService.findUpcomingEvents();
            
            Platform.runLater(() -> {
                if (eventsList.isEmpty()) {
                    showNoEventsMessage();
                } else {
                    // Calculate total pages
                    int pageCount = (int) Math.ceil((double) eventsList.size() / EVENTS_PER_PAGE);
                    eventsPagination.setPageCount(pageCount);
                    
                    // Show first page
                    showEventsPage(0);
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> 
                showErrorAlert("Database Error", "Failed to load events.", e.getMessage())
            );
        }
    }
    
    /**
     * Set up pagination control
     */
    private void setupPagination() {
        eventsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            showEventsPage(newIndex.intValue());
        });
        
        // Style the pagination
        eventsPagination.getStyleClass().add("custom-pagination");
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
        
        // Show loading indicator
        showPageLoadingIndicator();
        
        // Create event cards for this page
        for (int i = startIndex; i < endIndex; i++) {
            final int index = i;
            CompletableFuture.supplyAsync(() -> createEventCard(eventsList.get(index)), executorService)
                .thenAccept(eventCard -> {
                    if (eventCard != null) {
                        Platform.runLater(() -> {
                            // Add animation delay based on position
                            int position = index - startIndex;
                            addCardWithAnimation(eventCard, position * 100);
                        });
                    }
                });
        }
        
        // Hide loading indicator after a short delay
        CompletableFuture.delayedExecutor(500, java.util.concurrent.TimeUnit.MILLISECONDS)
            .execute(() -> Platform.runLater(this::hidePageLoadingIndicator));
    }
    
    /**
     * Show loading indicator for page transition
     */
    private void showPageLoadingIndicator() {
        // We could add a subtle loading indicator here if needed
    }
    
    /**
     * Hide page loading indicator
     */
    private void hidePageLoadingIndicator() {
        // Remove loading indicator if exists
    }
    
    /**
     * Add card with animation
     */
    private void addCardWithAnimation(Node eventCard, int delay) {
        // First add the card to the container
        eventsContainer.getChildren().add(eventCard);
        
        // Initial state: transparent and slightly shifted down
        eventCard.setOpacity(0);
        eventCard.setTranslateY(20);
        
        // Create animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), eventCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), eventCard);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), eventCard);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        
        // Combine animations
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideUp, scaleIn);
        parallelTransition.setDelay(Duration.millis(delay));
        
        // Play animation
        parallelTransition.play();
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
            
            // Add mouse hover effect
            addHoverEffect(eventCard);
            
            return eventCard;
        } catch (Exception e) {
            // Fallback to programmatic creation if loading fails
            System.err.println("Error loading event card: " + e.getMessage());
            e.printStackTrace();
            return createEventCardProgrammatically(event);
        }
    }
    
    /**
     * Add hover effect to the event card
     */
    private void addHoverEffect(Node card) {
        // Add hover effect with cursor pointer
        card.setOnMouseEntered(e -> {
            card.getStyleClass().add("event-card-hover");
            card.setCursor(javafx.scene.Cursor.HAND);
        });
        
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("event-card-hover");
            card.setCursor(javafx.scene.Cursor.DEFAULT);
        });
        
        // Add click event to navigate to event details
        card.setOnMouseClicked(e -> {
            // Animation for click
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), card);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), card);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            
            scaleDown.setOnFinished(event -> scaleUp.play());
            scaleDown.play();
        });
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
    
    /**
     * Clean up resources
     */
    public void onClose() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 
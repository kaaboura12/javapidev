package Controllers.event;

import Controllers.BasefrontController;
import Models.Event;
import Services.EventService;
import Controllers.event.DonationController;
import Controllers.event.ReservationController;
import Views.event.Dmodel;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import Controllers.event.AddeventController;

public class ListeventController extends BasefrontController {
    
    @FXML
    private StackPane rootPane;
    
    @FXML
    private FlowPane eventsContainer;
    
    @FXML
    private Pagination eventsPagination;
    
    @FXML
    private VBox eventCardTemplate;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button chatbotButton;
    
    @FXML
    private Circle chatNotificationDot;
    
    // New button for 3D model viewer
    @FXML
    private Button modelViewerButton;
    
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
        
        // Check if user is authenticated before allowing certain actions
        boolean isAuthenticated = getCurrentUser() != null;
        
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
        
        // Set up chatbot button click handler with enhanced animations
        if (chatbotButton != null) {
            chatbotButton.setOnAction(event -> openChatbot());
            
            // Initialize chatbot notification dot
            if (chatNotificationDot != null) {
                setupChatbotNotification();
            }
        }
        
        // Setup model viewer button if not created in FXML
        setupModelViewerButton();
        
        // Setup add event button if user has proper rights
        setupAddEventButton();
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
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), eventCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(500), eventCard);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), eventCard);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        
        // Combine animations
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideUp, scaleIn);
        parallelTransition.setDelay(Duration.millis(delay));
        
        // Play animation
        parallelTransition.play();
        
        // Add hover animation to the card
        addHoverEffect(eventCard);
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
            
            // Add a slight glow effect on hover
            DropShadow glow = new DropShadow();
            glow.setColor(javafx.scene.paint.Color.valueOf("#ff7e5f40"));
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(10);
            card.setEffect(glow);
            
            // Add scale animation
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), card);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.play();
        });
        
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("event-card-hover");
            card.setCursor(javafx.scene.Cursor.DEFAULT);
            
            // Remove the glow effect
            card.setEffect(null);
            
            // Scale back down
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
        
        // Add click event to navigate to event details
        card.setOnMouseClicked(e -> {
            // Animation for click
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), card);
            scaleDown.setToX(0.97);
            scaleDown.setToY(0.97);
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), card);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            
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
            // Create card container
            VBox eventCard = new VBox();
            eventCard.getStyleClass().add("event-card-modern");
            
            // Create image container
            StackPane imageContainer = new StackPane();
            imageContainer.getStyleClass().add("event-image-container");
            
            // Create image view
            ImageView imageView = new ImageView();
            imageView.getStyleClass().add("event-image-modern");
            imageView.setFitWidth(300);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(true);
            
            // Add image effect
            javafx.scene.effect.ColorAdjust colorAdjust = new javafx.scene.effect.ColorAdjust();
            colorAdjust.setBrightness(-0.1);
            colorAdjust.setContrast(0.1);
            imageView.setEffect(colorAdjust);
            
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
            
            // Create status indicator
            HBox statusBox = new HBox();
            statusBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            StackPane.setAlignment(statusBox, javafx.geometry.Pos.TOP_RIGHT);
            statusBox.setPadding(new javafx.geometry.Insets(10, 10, 0, 0));
            
            boolean isFuture = event.getDateEvenement().isAfter(java.time.LocalDateTime.now());
            Label statusLabel = new Label(isFuture ? "Coming Soon" : "Passed");
            statusLabel.getStyleClass().add("event-status-label-modern");
            if (!isFuture) {
                statusLabel.getStyleClass().add("event-status-passed");
            }
            
            statusBox.getChildren().add(statusLabel);
            
            // Add image and status to container
            imageContainer.getChildren().addAll(imageView, statusBox);
            
            // Create details container
            VBox detailsBox = new VBox();
            detailsBox.getStyleClass().add("event-details-modern");
            detailsBox.setSpacing(8);
            
            // Create title and date row
            HBox titleDateBox = new HBox();
            titleDateBox.setSpacing(10);
            titleDateBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            // Create date display
            VBox dateBox = new VBox();
            dateBox.setAlignment(javafx.geometry.Pos.CENTER);
            dateBox.getStyleClass().add("event-date-modern");
            
            // Format the date
            String day = event.getDateEvenement().format(DateTimeFormatter.ofPattern("dd"));
            String month = event.getDateEvenement().format(DateTimeFormatter.ofPattern("MMM"));
            
            Label dayLabel = new Label(day);
            dayLabel.getStyleClass().add("event-day-modern");
            
            Label monthLabel = new Label(month);
            monthLabel.getStyleClass().add("event-month-modern");
            
            dateBox.getChildren().addAll(dayLabel, monthLabel);
            
            // Create title
            Label titleLabel = new Label(event.getTitre());
            titleLabel.getStyleClass().add("event-title-modern");
            titleLabel.setWrapText(true);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            
            // Add date and title to container
            titleDateBox.getChildren().addAll(dateBox, titleLabel);
            
            // Create location display
            HBox locationBox = new HBox();
            locationBox.getStyleClass().add("event-location-modern");
            locationBox.setSpacing(5);
            
            // Create location icon
            Region locationIcon = new Region();
            locationIcon.setMinWidth(14);
            locationIcon.setMinHeight(14);
            locationIcon.getStyleClass().add("location-icon");
            
            Label locationLabel = new Label(event.getLieu());
            locationLabel.getStyleClass().add("event-location-text-modern");
            
            locationBox.getChildren().addAll(locationIcon, locationLabel);
            
            // Create time display
            HBox timeBox = new HBox();
            timeBox.getStyleClass().add("event-time-modern");
            timeBox.setSpacing(5);
            
            // Create time icon
            Region timeIcon = new Region();
            timeIcon.setMinWidth(14);
            timeIcon.setMinHeight(14);
            timeIcon.getStyleClass().add("time-icon");
            
            Label timeLabel = new Label(event.getTimestart() != null ? 
                            event.getTimestart().format(DateTimeFormatter.ofPattern("h:mm a")) : "TBA");
            timeLabel.getStyleClass().add("event-time-text-modern");
            
            timeBox.getChildren().addAll(timeIcon, timeLabel);
            
            // Create action area
            HBox actionArea = new HBox();
            actionArea.getStyleClass().add("event-action-area-modern");
            actionArea.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            // Create price info container
            VBox priceInfo = new VBox();
            priceInfo.getStyleClass().add("event-price-info-modern");
            HBox.setHgrow(priceInfo, Priority.ALWAYS);
            
            // Format price
            java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US);
            Label priceLabel = new Label(formatter.format(event.getSeatprice()));
            priceLabel.getStyleClass().add("price-value-modern");
            
            int available = event.getAvailableSeats();
            Label seatsLabel = new Label(available + (available == 1 ? " seat" : " seats") + " left");
            seatsLabel.getStyleClass().add("seats-value-modern");
            
            priceInfo.getChildren().addAll(priceLabel, seatsLabel);
            
            // Create action buttons
            Button detailsButton = new Button("DETAILS");
            detailsButton.getStyleClass().add("get-ticket-button-modern");
            detailsButton.setOnAction(e -> handleGetTicket(event));
            
            Button donateButton = new Button();
            donateButton.getStyleClass().add("donate-button-modern");
            donateButton.setMinWidth(40);
            
            // Add heart icon
            Region heartIcon = new Region();
            heartIcon.setMinWidth(16);
            heartIcon.setMinHeight(16);
            heartIcon.getStyleClass().add("heart-icon");
            donateButton.setGraphic(heartIcon);
            
            donateButton.setOnAction(e -> handleDonation(event));
            
            // Add all to action area
            actionArea.getChildren().addAll(priceInfo, detailsButton, donateButton);
            
            // Add all components to details box
            detailsBox.getChildren().addAll(titleDateBox, locationBox, timeBox, actionArea);
            
            // Add all components to event card
            eventCard.getChildren().addAll(imageContainer, detailsBox);
            
            // Add hover effect
            addHoverEffect(eventCard);
            
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
        // Check if user is logged in
        if (getCurrentUser() == null) {
            // Show login prompt
            showWarningAlert("Authentication Required", 
                            "Please login to reserve tickets", 
                            "You need to be logged in to reserve tickets for this event.");
            
            // Provide option to login
            ButtonType loginButton = new ButtonType("Login Now", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                                "Would you like to login now?", 
                                loginButton, cancelButton);
            alert.setTitle("Login Required");
            alert.setHeaderText("Authentication needed");
            
            alert.showAndWait().ifPresent(type -> {
                if (type == loginButton) {
                    navigateToUserPage("login");
                }
            });
            
            return;
        }
        
        // User is logged in, proceed with reservation
        if (event != null) {
            openReservationWindow(event);
        }
    }
    
    /**
     * Open reservation in a new window as fallback
     * 
     * @param event The event to get ticket for
     */
    private void openReservationWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/reservation.fxml"));
            Parent reservationView = loader.load();
            
            // Get the controller and set the event
            ReservationController controller = loader.getController();
            controller.setEvent(event);
            
            // Create and show a new stage
            Stage stage = new Stage();
            stage.setTitle("Reserve Tickets - " + event.getTitre());
            stage.setScene(new Scene(reservationView));
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Error", "Failed to open reservation window.", e.getMessage());
            e.printStackTrace();
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
     * Handle donation button click
     * 
     * @param event The event to donate for
     */
    private void handleDonation(Event event) {
        // Check if user is logged in
        if (getCurrentUser() == null) {
            // Show login prompt
            showWarningAlert("Authentication Required", 
                            "Please login to make a donation", 
                            "You need to be logged in to make a donation for this event.");
            
            // Provide option to login
            ButtonType loginButton = new ButtonType("Login Now", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
                                "Would you like to login now?", 
                                loginButton, cancelButton);
            alert.setTitle("Login Required");
            alert.setHeaderText("Authentication needed");
            
            alert.showAndWait().ifPresent(type -> {
                if (type == loginButton) {
                    navigateToUserPage("login");
                }
            });
            
            return;
        }
        
        // User is logged in, proceed with donation
        if (event != null) {
            openDonationWindow(event);
        }
    }
    
    /**
     * Open donation form in a new window
     * 
     * @param event The event to donate to
     */
    private void openDonationWindow(Event event) {
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
            showErrorAlert("Error", "Failed to open donation window.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find the main content area in parent hierarchy
     * 
     * @return The content area StackPane or null if not found
     */
    private StackPane findContentArea() {
        try {
            if (rootPane == null || rootPane.getScene() == null) {
                return null;
            }
            
            // Start from the root of the scene
            Parent parent = rootPane.getScene().getRoot();
            
            // Traverse the scene graph to find a StackPane with id "contentArea"
            return findStackPaneById(parent, "contentArea");
        } catch (Exception e) {
            // If any error occurs, return null
            return null;
        }
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
     * Clean up resources
     */
    public void onClose() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
    
    /**
     * Set up pulsing animation for the chatbot notification dot
     */
    private void setupChatbotNotification() {
        // Create pulsing animation for the notification dot
        Timeline pulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(chatNotificationDot.opacityProperty(), 1.0),
                new KeyValue(chatNotificationDot.scaleXProperty(), 1.0),
                new KeyValue(chatNotificationDot.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(1000), 
                new KeyValue(chatNotificationDot.opacityProperty(), 0.6),
                new KeyValue(chatNotificationDot.scaleXProperty(), 1.3),
                new KeyValue(chatNotificationDot.scaleYProperty(), 1.3)),
            new KeyFrame(Duration.millis(2000), 
                new KeyValue(chatNotificationDot.opacityProperty(), 1.0),
                new KeyValue(chatNotificationDot.scaleXProperty(), 1.0),
                new KeyValue(chatNotificationDot.scaleYProperty(), 1.0))
        );
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();
    }
    
    /**
     * Open the chatbot dialog with enhanced animations
     */
    private void openChatbot() {
        try {
            // Hide notification dot when chat is opened
            if (chatNotificationDot != null) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), chatNotificationDot);
                fadeOut.setToValue(0);
                fadeOut.play();
            }
            
            // Add enhanced button press animation
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), chatbotButton);
            scaleDown.setToX(0.85);
            scaleDown.setToY(0.85);
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), chatbotButton);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            
            // Add rotation effect for a more interactive feel
            javafx.animation.RotateTransition rotateEffect = new javafx.animation.RotateTransition(Duration.millis(150), chatbotButton);
            rotateEffect.setByAngle(360);
            rotateEffect.setCycleCount(1);
            
            ParallelTransition clickEffect = new ParallelTransition(scaleDown);
            
            clickEffect.setOnFinished(event -> {
                ParallelTransition releaseEffect = new ParallelTransition(scaleUp, rotateEffect);
                releaseEffect.play();
                
                // Load the chatbot with a slight delay for better UX
                CompletableFuture.delayedExecutor(200, java.util.concurrent.TimeUnit.MILLISECONDS)
                    .execute(() -> Platform.runLater(this::loadChatbotInScene));
            });
            
            clickEffect.play();
            
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to open chatbot.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load and display the chatbot as an overlay in the current scene
     */
    private void loadChatbotInScene() {
        try {
            // Check if chatbot is already open
            if (rootPane.getChildren().stream().anyMatch(node -> "chatbotOverlay".equals(node.getId()))) {
                // Chatbot is already open, just return
                return;
            }
            
            // Create a semi-transparent background overlay for better focus
            Rectangle backgroundOverlay = new Rectangle();
            backgroundOverlay.setWidth(rootPane.getWidth());
            backgroundOverlay.setHeight(rootPane.getHeight());
            backgroundOverlay.setFill(Color.rgb(0, 0, 0, 0.35));
            backgroundOverlay.setOpacity(0);
            
            // Make the background overlay resize with the window
            backgroundOverlay.widthProperty().bind(rootPane.widthProperty());
            backgroundOverlay.heightProperty().bind(rootPane.heightProperty());
            
            // Load the chatbot FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/chatbot/chatbot.fxml"));
            Parent chatbotView = loader.load();
            
            // Create a container for the chatbot that we can animate and position
            StackPane chatbotContainer = new StackPane();
            chatbotContainer.setId("chatbotOverlay");
            
            // Make the chatbot responsive based on the stage size
            double stageWidth = rootPane.getScene().getWindow().getWidth();
            double stageHeight = rootPane.getScene().getWindow().getHeight();
            
            // Determine chatbot size (responsive)
            double chatbotWidth = Math.min(450, stageWidth * 0.35);
            double chatbotHeight = Math.min(600, stageHeight * 0.85);
            
            // Set minimum size for usability
            chatbotWidth = Math.max(chatbotWidth, 350);
            chatbotHeight = Math.max(chatbotHeight, 500);
            
            // Set size constraints
            chatbotContainer.setMinWidth(chatbotWidth);
            chatbotContainer.setMaxWidth(chatbotWidth);
            chatbotContainer.setPrefWidth(chatbotWidth);
            chatbotContainer.setMinHeight(chatbotHeight);
            chatbotContainer.setMaxHeight(chatbotHeight);
            chatbotContainer.setPrefHeight(chatbotHeight);
            
            // Apply styles
            chatbotContainer.setStyle("-fx-background-color: transparent;");
            
            // Create a wrapper for the chatbot content to apply proper sizing
            VBox chatbotWrapper = new VBox();
            chatbotWrapper.getChildren().add(chatbotView);
            chatbotWrapper.setPrefWidth(chatbotWidth);
            chatbotWrapper.setPrefHeight(chatbotHeight);
            VBox.setVgrow(chatbotView, Priority.ALWAYS);
            HBox.setHgrow(chatbotView, Priority.ALWAYS);
            
            // Apply initial transforms for animation
            chatbotContainer.setOpacity(0);
            chatbotContainer.setTranslateX(100); // Start from right side for slide-in effect
            chatbotContainer.setScaleX(0.95);
            chatbotContainer.setScaleY(0.95);
            
            // Create a more visually appealing close button
            StackPane closeButtonContainer = new StackPane();
            closeButtonContainer.setMaxWidth(36);
            closeButtonContainer.setMaxHeight(36);
            closeButtonContainer.setStyle("-fx-background-color: #ff7e5f; -fx-background-radius: 18px;");
            
            // Add drop shadow to close button
            DropShadow closeButtonShadow = new DropShadow();
            closeButtonShadow.setColor(Color.color(0, 0, 0, 0.3));
            closeButtonShadow.setRadius(5);
            closeButtonShadow.setOffsetY(1);
            closeButtonContainer.setEffect(closeButtonShadow);
            
            Label closeIcon = new Label("×");
            closeIcon.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
            closeButtonContainer.getChildren().add(closeIcon);
            
            // Position the close button in the top-left corner 
            StackPane.setAlignment(closeButtonContainer, Pos.TOP_LEFT);
            StackPane.setMargin(closeButtonContainer, new Insets(-15, 0, 0, -15));
            
            // Add interactive effects to close button
            closeButtonContainer.setOnMouseEntered(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #ff5d3b; -fx-background-radius: 18px; -fx-cursor: hand;");
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), closeButtonContainer);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();
            });
            
            closeButtonContainer.setOnMouseExited(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #ff7e5f; -fx-background-radius: 18px;");
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), closeButtonContainer);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });
            
            // Add press effect
            closeButtonContainer.setOnMousePressed(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #e55e3b; -fx-background-radius: 18px;");
                ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), closeButtonContainer);
                scalePress.setToX(0.9);
                scalePress.setToY(0.9);
                scalePress.play();
            });
            
            closeButtonContainer.setOnMouseReleased(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #ff5d3b; -fx-background-radius: 18px;");
                ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), closeButtonContainer);
                scaleRelease.setToX(1.1);
                scaleRelease.setToY(1.1);
                scaleRelease.play();
            });
            
            // Add close handler
            closeButtonContainer.setOnMouseClicked(e -> closeChatbotOverlay(chatbotContainer, backgroundOverlay));
            
            // Add drop shadow effect to the container for a floating card look
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.color(0, 0, 0, 0.5));
            shadow.setRadius(20);
            shadow.setSpread(0.1);
            shadow.setOffsetX(-5); // Shadow toward left for right-aligned panel
            chatbotWrapper.setEffect(shadow);
            
            // Add the chatbot view to the container
            chatbotContainer.getChildren().addAll(chatbotWrapper, closeButtonContainer);
            
            // Allow clicking outside the chatbot to close it
            backgroundOverlay.setOnMouseClicked(e -> closeChatbotOverlay(chatbotContainer, backgroundOverlay));
            
            // Position the chatbot container on the right side of the scene
            StackPane.setAlignment(chatbotContainer, Pos.CENTER_RIGHT);
            StackPane.setMargin(chatbotContainer, new Insets(0, 30, 0, 0));
            
            // Add the background overlay and chatbot container to the root pane
            rootPane.getChildren().addAll(backgroundOverlay, chatbotContainer);
            
            // Create a responsive resize binding for when window size changes
            rootPane.getScene().getWindow().widthProperty().addListener((obs, oldVal, newVal) -> {
                double newWidth = Math.min(450, newVal.doubleValue() * 0.35);
                newWidth = Math.max(newWidth, 350); // Minimum width
                chatbotContainer.setPrefWidth(newWidth);
                chatbotWrapper.setPrefWidth(newWidth);
            });
            
            rootPane.getScene().getWindow().heightProperty().addListener((obs, oldVal, newVal) -> {
                double newHeight = Math.min(600, newVal.doubleValue() * 0.85);
                newHeight = Math.max(newHeight, 500); // Minimum height
                chatbotContainer.setPrefHeight(newHeight);
                chatbotWrapper.setPrefHeight(newHeight);
            });
            
            // Apply entrance animations
            // 1. Background fade in
            FadeTransition backgroundFadeIn = new FadeTransition(Duration.millis(300), backgroundOverlay);
            backgroundFadeIn.setFromValue(0.0);
            backgroundFadeIn.setToValue(1.0);
            
            // 2. Chatbot view animations - slide in from right
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), chatbotContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), chatbotContainer);
            slideIn.setFromX(100);
            slideIn.setToX(0);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), chatbotContainer);
            scaleIn.setFromX(0.95);
            scaleIn.setFromY(0.95);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            // Combine animations and play
            ParallelTransition parallelTransition = new ParallelTransition(
                backgroundFadeIn, fadeIn, slideIn, scaleIn
            );
            
            parallelTransition.play();
            
        } catch (IOException e) {
            showErrorAlert("Error", "Failed to open chatbot.", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Close the chatbot overlay and reset notification
     */
    private void closeChatbotOverlay(StackPane chatbotContainer, Rectangle backgroundOverlay) {
        // Create fade out animation for background
        FadeTransition backgroundFadeOut = new FadeTransition(Duration.millis(250), backgroundOverlay);
        backgroundFadeOut.setFromValue(1.0);
        backgroundFadeOut.setToValue(0.0);
        
        // Create fade out animation for chatbot
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), chatbotContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        // Create slide animation to right
        TranslateTransition slideRight = new TranslateTransition(Duration.millis(300), chatbotContainer);
        slideRight.setToX(100);
        
        // Create scale animation
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), chatbotContainer);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);
        
        // Combine animations
        ParallelTransition closeAnimation = new ParallelTransition(
            backgroundFadeOut, fadeOut, slideRight, scaleOut
        );
        
        // Remove container when animation completes
        closeAnimation.setOnFinished(e -> {
            rootPane.getChildren().removeAll(chatbotContainer, backgroundOverlay);
            
            // Reset notification after some time
            if (chatNotificationDot != null) {
                CompletableFuture.delayedExecutor(15, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(() -> Platform.runLater(() -> {
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), chatNotificationDot);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    }));
            }
        });
        
        // Play the animation
        closeAnimation.play();
    }
    
    /**
     * Setup the 3D model viewer button if it doesn't exist in the FXML
     */
    private void setupModelViewerButton() {
        // If the button doesn't exist in FXML, create it programmatically
        if (modelViewerButton == null && eventsContainer != null) {
            modelViewerButton = new Button("View 3D City Model");
            modelViewerButton.getStyleClass().add("action-button");
            
            // Create a container to place the button at the top of the page
            HBox buttonContainer = new HBox();
            buttonContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            buttonContainer.setPadding(new javafx.geometry.Insets(0, 0, 10, 0));
            buttonContainer.getChildren().add(modelViewerButton);
            
            // Get the parent VBox to add our button container
            VBox parentContainer = (VBox) eventsContainer.getParent();
            if (parentContainer != null) {
                // Add button container before the eventsContainer
                int eventsContainerIndex = parentContainer.getChildren().indexOf(eventsContainer);
                if (eventsContainerIndex >= 0) {
                    parentContainer.getChildren().add(eventsContainerIndex, buttonContainer);
                } else {
                    // Fallback: add at the beginning
                    parentContainer.getChildren().add(0, buttonContainer);
                }
            }
            
            // Add icon to button
            try {
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/icons/3d-model-icon.png")));
                icon.setFitHeight(18);
                icon.setFitWidth(18);
                modelViewerButton.setGraphic(icon);
            } catch (Exception e) {
                // If icon loading fails, use text only
                System.err.println("Could not load 3D model icon: " + e.getMessage());
            }
            
            // Style the button
            modelViewerButton.setStyle("-fx-background-color: #4A90E2; -fx-text-fill: white; -fx-padding: 8px 15px;");
            
            // Add hover effect
            addButtonEffect(modelViewerButton);
        }
        
        // Add action handler to open 3D model
        if (modelViewerButton != null) {
            modelViewerButton.setOnAction(e -> open3DModelViewer());
        }
    }
    
    /**
     * Open the 3D model viewer as an overlay in the current scene
     */
    private void open3DModelViewer() {
        try {
            // Check if model viewer is already open
            if (rootPane.getChildren().stream().anyMatch(node -> "modelViewerOverlay".equals(node.getId()))) {
                // Model viewer is already open, just return
                return;
            }
            
            // Create a semi-transparent background overlay for better focus
            Rectangle backgroundOverlay = new Rectangle();
            backgroundOverlay.setWidth(rootPane.getWidth());
            backgroundOverlay.setHeight(rootPane.getHeight());
            backgroundOverlay.setFill(Color.rgb(0, 0, 0, 0.5));
            backgroundOverlay.setOpacity(0);
            
            // Make the background overlay resize with the window
            backgroundOverlay.widthProperty().bind(rootPane.widthProperty());
            backgroundOverlay.heightProperty().bind(rootPane.heightProperty());
            
            // Load the 3D model FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/3dmodel.fxml"));
            Parent modelView = loader.load();
            
            // Get the controller
            ModelController controller = loader.getController();
            
            // Create a container for the model viewer that we can animate and position
            StackPane modelContainer = new StackPane();
            modelContainer.setId("modelViewerOverlay");
            
            // Make the model viewer responsive based on the stage size
            double stageWidth = rootPane.getScene().getWindow().getWidth();
            double stageHeight = rootPane.getScene().getWindow().getHeight();
            
            // Determine model viewer size (responsive)
            double modelWidth = Math.min(1000, stageWidth * 0.8);
            double modelHeight = Math.min(800, stageHeight * 0.8);
            
            // Set minimum size for usability
            modelWidth = Math.max(modelWidth, 800);
            modelHeight = Math.max(modelHeight, 600);
            
            // Set size constraints
            modelContainer.setMinWidth(modelWidth);
            modelContainer.setMaxWidth(modelWidth);
            modelContainer.setPrefWidth(modelWidth);
            modelContainer.setMinHeight(modelHeight);
            modelContainer.setMaxHeight(modelHeight);
            modelContainer.setPrefHeight(modelHeight);
            
            // Apply styles
            modelContainer.setStyle("-fx-background-color: #1A202C; -fx-background-radius: 8px;");
            
            // Create a wrapper for the model content to apply proper sizing
            VBox modelWrapper = new VBox();
            modelWrapper.getChildren().add(modelView);
            modelWrapper.setPrefWidth(modelWidth);
            modelWrapper.setPrefHeight(modelHeight);
            VBox.setVgrow(modelView, Priority.ALWAYS);
            HBox.setHgrow(modelView, Priority.ALWAYS);
            
            // Apply initial transforms for animation
            modelContainer.setOpacity(0);
            modelContainer.setScaleX(0.95);
            modelContainer.setScaleY(0.95);
            
            // Create a close button
            StackPane closeButtonContainer = new StackPane();
            closeButtonContainer.setMaxWidth(36);
            closeButtonContainer.setMaxHeight(36);
            closeButtonContainer.setStyle("-fx-background-color: #ff7e5f; -fx-background-radius: 18px;");
            
            // Add drop shadow to close button
            DropShadow closeButtonShadow = new DropShadow();
            closeButtonShadow.setColor(Color.color(0, 0, 0, 0.3));
            closeButtonShadow.setRadius(5);
            closeButtonShadow.setOffsetY(1);
            closeButtonContainer.setEffect(closeButtonShadow);
            
            Label closeIcon = new Label("×");
            closeIcon.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
            closeButtonContainer.getChildren().add(closeIcon);
            
            // Position the close button in the top-right corner 
            StackPane.setAlignment(closeButtonContainer, Pos.TOP_RIGHT);
            StackPane.setMargin(closeButtonContainer, new Insets(-15, -15, 0, 0));
            
            // Add interactive effects to close button
            closeButtonContainer.setOnMouseEntered(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #ff5d3b; -fx-background-radius: 18px; -fx-cursor: hand;");
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), closeButtonContainer);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();
            });
            
            closeButtonContainer.setOnMouseExited(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #ff7e5f; -fx-background-radius: 18px;");
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), closeButtonContainer);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });
            
            // Add press effect
            closeButtonContainer.setOnMousePressed(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #e55e3b; -fx-background-radius: 18px;");
                ScaleTransition scalePress = new ScaleTransition(Duration.millis(100), closeButtonContainer);
                scalePress.setToX(0.9);
                scalePress.setToY(0.9);
                scalePress.play();
            });
            
            closeButtonContainer.setOnMouseReleased(e -> {
                closeButtonContainer.setStyle("-fx-background-color: #ff5d3b; -fx-background-radius: 18px;");
                ScaleTransition scaleRelease = new ScaleTransition(Duration.millis(100), closeButtonContainer);
                scaleRelease.setToX(1.1);
                scaleRelease.setToY(1.1);
                scaleRelease.play();
            });
            
            // Add close handler
            closeButtonContainer.setOnMouseClicked(e -> closeModelViewerOverlay(modelContainer, backgroundOverlay));
            
            // Add drop shadow effect to the container for a floating card look
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.color(0, 0, 0, 0.5));
            shadow.setRadius(20);
            shadow.setSpread(0.1);
            modelWrapper.setEffect(shadow);
            
            // Add the model view to the container
            modelContainer.getChildren().addAll(modelWrapper, closeButtonContainer);
            
            // Allow clicking outside the model viewer to close it
            backgroundOverlay.setOnMouseClicked(e -> closeModelViewerOverlay(modelContainer, backgroundOverlay));
            
            // Position the model container in the center of the scene
            StackPane.setAlignment(modelContainer, Pos.CENTER);
            
            // Add the background overlay and model container to the root pane
            rootPane.getChildren().addAll(backgroundOverlay, modelContainer);
            
            // Create a responsive resize binding for when window size changes
            rootPane.getScene().getWindow().widthProperty().addListener((obs, oldVal, newVal) -> {
                double newWidth = Math.min(1000, newVal.doubleValue() * 0.8);
                newWidth = Math.max(newWidth, 800); // Minimum width
                modelContainer.setPrefWidth(newWidth);
                modelWrapper.setPrefWidth(newWidth);
            });
            
            rootPane.getScene().getWindow().heightProperty().addListener((obs, oldVal, newVal) -> {
                double newHeight = Math.min(800, newVal.doubleValue() * 0.8);
                newHeight = Math.max(newHeight, 600); // Minimum height
                modelContainer.setPrefHeight(newHeight);
                modelWrapper.setPrefHeight(newHeight);
            });
            
            // Apply entrance animations
            // 1. Background fade in
            FadeTransition backgroundFadeIn = new FadeTransition(Duration.millis(300), backgroundOverlay);
            backgroundFadeIn.setFromValue(0.0);
            backgroundFadeIn.setToValue(1.0);
            
            // 2. Model view animations - scale and fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), modelContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), modelContainer);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            // Combine animations and play
            ParallelTransition parallelTransition = new ParallelTransition(
                backgroundFadeIn, fadeIn, scaleIn
            );
            
            parallelTransition.play();
            
            // Add a notification that appears briefly
            showNotification("3D model viewer opened", 3);
            
        } catch (Exception e) {
            System.err.println("Error opening 3D model viewer: " + e.getMessage());
            e.printStackTrace();
            
            // Show error notification
            showNotification("Could not open 3D model viewer: " + e.getMessage(), 5);
        }
    }
    
    /**
     * Close the model viewer overlay
     */
    private void closeModelViewerOverlay(StackPane modelContainer, Rectangle backgroundOverlay) {
        // Create fade out animation for background
        FadeTransition backgroundFadeOut = new FadeTransition(Duration.millis(250), backgroundOverlay);
        backgroundFadeOut.setFromValue(1.0);
        backgroundFadeOut.setToValue(0.0);
        
        // Create fade out animation for model viewer
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), modelContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        // Create scale animation
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(300), modelContainer);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);
        
        // Combine animations
        ParallelTransition closeAnimation = new ParallelTransition(
            backgroundFadeOut, fadeOut, scaleOut
        );
        
        // Remove container when animation completes
        closeAnimation.setOnFinished(e -> {
            rootPane.getChildren().removeAll(modelContainer, backgroundOverlay);
        });
        
        // Play the animation
        closeAnimation.play();
    }
    
    /**
     * Add hover and press effects to a button
     */
    private void addButtonEffect(Button button) {
        // Store original style
        String originalStyle = button.getStyle();
        
        // Create glow effect
        Glow glow = new Glow(0.5);
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
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
                button.setStyle(originalStyle + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
                button.setEffect(glow);
            } else {
                button.setStyle(originalStyle);
                button.setEffect(null);
            }
        });
    }
    
    /**
     * Show a notification message that fades out after a specified duration
     */
    private void showNotification(String message, int seconds) {
        // Create notification label
        Label notification = new Label(message);
        notification.setStyle("-fx-background-color: #333333CC; -fx-text-fill: white; -fx-padding: 10px 20px; " +
                             "-fx-background-radius: 5px; -fx-font-size: 14px;");
        notification.setOpacity(0);
        
        // Add to root pane
        if (rootPane != null) {
            StackPane.setAlignment(notification, javafx.geometry.Pos.BOTTOM_CENTER);
            StackPane.setMargin(notification, new javafx.geometry.Insets(0, 0, 20, 0));
            rootPane.getChildren().add(notification);
            
            // Create fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            // Create fade out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notification);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(seconds));
            fadeOut.setOnFinished(e -> rootPane.getChildren().remove(notification));
            
            // Play animations
            fadeIn.play();
            fadeOut.play();
        }
    }
    
    /**
     * Setup add event button based on user permissions
     */
    private void setupAddEventButton() {
        // Check if user has permission to add events
        if (getCurrentUser() != null && (hasRole("ADMIN") || hasRole("ORGANISATEUR"))) {
            // Create Add Event button
            Button addEventButton = new Button("+ Add Event");
            addEventButton.getStyleClass().add("primary-button");
            addEventButton.setOnAction(event -> openAddEventForm());
            
            // Add the button to the UI (assuming there's a container for it)
            if (rootPane != null && rootPane.getScene() != null) {
                Node buttonBar = rootPane.lookup("#eventButtonBar");
                if (buttonBar instanceof HBox) {
                    ((HBox) buttonBar).getChildren().add(addEventButton);
                } else {
                    // Create button bar if it doesn't exist
                    HBox newButtonBar = new HBox(10);
                    newButtonBar.setId("eventButtonBar");
                    newButtonBar.setAlignment(Pos.CENTER_RIGHT);
                    newButtonBar.setPadding(new Insets(10));
                    newButtonBar.getChildren().add(addEventButton);
                    
                    // Find where to add it in the layout
                    VBox contentBox = (VBox) rootPane.lookup("VBox");
                    if (contentBox != null) {
                        contentBox.getChildren().add(0, newButtonBar);
                    }
                }
            }
            
            // Add hover effects
            addHoverEffectToButton(addEventButton);
        }
    }
    
    /**
     * Open the add event form
     */
    private void openAddEventForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/addevent.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the current user
            AddeventController controller = loader.getController();
            controller.setCurrentUser(getCurrentUser());
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Event");
            stage.setScene(new Scene(root));
            
            // When the add event form is closed, refresh the events list
            stage.setOnHidden(e -> CompletableFuture.runAsync(this::loadEvents)
                .thenRun(() -> Platform.runLater(() -> {
                    hideLoadingIndicator();
                    setupPagination();
                })));
            
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open add event form", e.getMessage());
        }
    }
    
    /**
     * Add hover effect to a button
     * 
     * @param button The button to add effects to
     */
    private void addHoverEffectToButton(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }
} 
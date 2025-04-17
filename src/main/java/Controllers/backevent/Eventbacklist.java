package Controllers.backevent;

import Models.Event;
import Services.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.time.format.FormatStyle;

public class Eventbacklist implements Initializable {

    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button addEventButton;
    
    @FXML
    private ListView<Event> eventListView;
    
    @FXML
    private Label totalEventsLabel;
    
    @FXML
    private Label lastUpdatedLabel;
    
    // Stats cards labels
    @FXML
    private Label totalTaskCount;
    
    @FXML
    private Label inProgressCount;
    
    @FXML
    private Label completedCount;
    
    @FXML
    private Label overdueCount;
    
    // Hidden stats for controller
    @FXML
    private Label totalEventsCount;
    
    @FXML
    private Label upcomingEventsCount;
    
    @FXML
    private Label totalTicketsCount;
    
    @FXML
    private Label totalDonationGoals;
    
    private EventService eventService;
    private ObservableList<Event> eventList;
    private FilteredList<Event> filteredEvents;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        eventService = new EventService();
        
        // Configure the ListView with a custom cell factory
        setupListView();
        
        // Load the events
        loadEvents();
        
        // Configure search functionality
        setupSearch();
        
        // Set current timestamp for last updated
        updateLastUpdatedLabel();
    }
    
    private void setupListView() {
        eventListView.setCellFactory(new Callback<ListView<Event>, ListCell<Event>>() {
            @Override
            public ListCell<Event> call(ListView<Event> param) {
                return new EventListCell();
            }
        });
        
        // Handle double-click on event item
        eventListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Event selectedEvent = eventListView.getSelectionModel().getSelectedItem();
                if (selectedEvent != null) {
                    handleEditEvent(selectedEvent);
                }
            }
        });
    }
    
    private void loadEvents() {
        try {
            // Get all events from the service
            eventList = FXCollections.observableArrayList(eventService.findAll());
            
            // Set up filtered list
            filteredEvents = new FilteredList<>(eventList, p -> true);
            
            // Set the list data
            eventListView.setItems(filteredEvents);
            
            // Update the event counts
            updateEventCounts();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load events", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateEventCounts() {
        // Update total events count
        int totalEvents = eventList.size();
        totalEventsLabel.setText("Total Events: " + totalEvents);
        
        try {
            // Get upcoming events count
            int upcoming = eventService.countUpcomingEvents();
            
            // Calculate total tickets and total donation objectives
            int totalTickets = 0;
            double totalDonations = 0;
            for (Event event : eventList) {
                totalTickets += event.getNombreBillets();
                totalDonations += event.getDonation_objective();
            }
            
            // Update hidden stats
            totalEventsCount.setText(String.valueOf(totalEvents));
            upcomingEventsCount.setText(String.valueOf(upcoming));
            totalTicketsCount.setText(String.valueOf(totalTickets));
            totalDonationGoals.setText(currencyFormatter.format(totalDonations));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateLastUpdatedLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        lastUpdatedLabel.setText("Last Updated: " + formatter.format(LocalDateTime.now()));
    }
    
    private void setupSearch() {
        // Set up the search functionality
        searchButton.setOnAction(event -> handleSearch());
        
        // Enable search as you type
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase();
            filteredEvents.setPredicate(event -> {
                if (searchText == null || searchText.isEmpty()) {
                    return true;
                }
                
                // Search in the title
                if (event.getTitre().toLowerCase().contains(searchText)) {
                    return true;
                }
                
                // Search in the location
                if (event.getLieu().toLowerCase().contains(searchText)) {
                    return true;
                }
                
                // Search in the description if available
                if (event.getDescription() != null && event.getDescription().toLowerCase().contains(searchText)) {
                    return true;
                }
                
                // Search in event mission if available
                if (event.getEvent_mission() != null && event.getEvent_mission().toLowerCase().contains(searchText)) {
                    return true;
                }
                
                return false;
            });
            
            totalEventsLabel.setText("Total Events: " + filteredEvents.size());
        });
    }
    
    @FXML
    private void handleSearch() {
        // The search is already handled by the text listener
    }
    
    @FXML
    private void handleRefresh() {
        loadEvents();
        searchField.clear();
        updateLastUpdatedLabel();
    }
    
    @FXML
    private void handleAddEvent() {
        try {
            // Try to find the main content area (AnchorPane with ID "contentArea")
            AnchorPane contentArea = findContentArea(addEventButton);
            
            if (contentArea != null) {
                // Load the add event form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backevent/addevent.fxml"));
                Parent root = loader.load();
                
                // Set anchors for the new view
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                
                // Clear content area and add new view
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
                
                // Update the title if possible
                updatePageTitle("Add New Event");
            } else {
                // Fallback: Use modal dialog if contentArea not found
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backevent/addevent.fxml"));
                Parent root = loader.load();
                
                // Create and configure the stage
                Stage stage = new Stage();
                stage.setTitle("Add New Event");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Make it modal
                
                // Show the dialog and wait for it to close
                stage.showAndWait();
                
                // Refresh the table to show the new event
                loadEvents();
                updateLastUpdatedLabel();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add event form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find the main content area in parent hierarchy
     * @param node The starting node to search from
     * @return The content area AnchorPane or null if not found
     */
    private AnchorPane findContentArea(javafx.scene.Node node) {
        if (node == null) return null;
        
        // Walk up the parent hierarchy
        Parent parent = node.getParent();
        while (parent != null) {
            // First, check if any children or the parent itself is the content area
            if (parent instanceof AnchorPane && "contentArea".equals(parent.getId())) {
                return (AnchorPane) parent;
            }
            
            // Look for contentArea in all scenes
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
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
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
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
            Scene scene = eventListView.getScene();
            if (scene == null) return;
            
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
    
    private void handleEditEvent(Event event) {
        try {
            // Try to find the main content area (AnchorPane with ID "contentArea")
            AnchorPane contentArea = findContentArea(eventListView);
            
            if (contentArea != null) {
                // Load the edit form
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backevent/eventedit.fxml"));
                Parent root = loader.load();
                
                // Get the controller and set the event
                EventEditController controller = loader.getController();
                controller.setEvent(event);
                
                // Set anchors for the new view
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                
                // Clear content area and add new view
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
                
                // Update the title if possible
                updatePageTitle("Edit Event");
            } else {
                // Fallback: Use modal dialog if contentArea not found
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backevent/eventedit.fxml"));
                Parent root = loader.load();
                
                // Get the controller and set the event
                EventEditController controller = loader.getController();
                controller.setEvent(event);
                
                // Create and configure the stage
                Stage stage = new Stage();
                stage.setTitle("Edit Event");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL); // Make it modal
                
                // Show the dialog and wait for it to close
                stage.showAndWait();
                
                // If changes were saved, refresh the table
                if (controller.isSaved()) {
                    loadEvents();
                    updateLastUpdatedLabel();
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDeleteEvent(Event event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to delete the event: " + event.getTitre() + "?\n\n" +
                "This will also delete all associated:\n" +
                "- Reservations\n" +
                "- Donations\n" +
                "- Any other related data\n\n" +
                "This action cannot be undone.", 
                ButtonType.YES, ButtonType.NO);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Event and All Related Data");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    eventService.delete(event);
                    eventList.remove(event);
                    updateEventCounts();
                    updateLastUpdatedLabel();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event Deleted", 
                            "The event and all its related data were successfully deleted.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete event", 
                            e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Custom ListCell for displaying Event items
    private class EventListCell extends ListCell<Event> {
        private HBox mainContainer;
        private VBox detailsPane;
        private HBox actionsPane;
        private HBox detailsRow;
        
        public EventListCell() {
            mainContainer = new HBox();
            mainContainer.getStyleClass().add("event-card");
            
            detailsPane = new VBox();
            detailsPane.getStyleClass().add("event-details-pane");
            HBox.setHgrow(detailsPane, Priority.ALWAYS);
            
            detailsRow = new HBox();
            detailsRow.getStyleClass().add("event-details-row");
            
            actionsPane = new HBox();
            actionsPane.getStyleClass().add("event-actions-pane");
            
            mainContainer.getChildren().addAll(detailsPane, actionsPane);
        }
        
        @Override
        protected void updateItem(Event event, boolean empty) {
            super.updateItem(event, empty);
            
            if (empty || event == null) {
                setText(null);
                setGraphic(null);
            } else {
                // Clear previous contents
                detailsPane.getChildren().clear();
                actionsPane.getChildren().clear();
                detailsRow.getChildren().clear();
                
                // Create title row with ID
                HBox titleRow = new HBox(10);
                titleRow.setAlignment(Pos.CENTER_LEFT);
                
                Label idLabel = new Label("#" + event.getIdevent());
                idLabel.getStyleClass().add("event-id-label");
                
                Label titleLabel = new Label(event.getTitre());
                titleLabel.getStyleClass().add("event-title-label");
                
                titleRow.getChildren().addAll(idLabel, titleLabel);
                
                // Create details row
                // Date and Time
                Label dateTimeLabel = new Label();
                String dateStr = event.getDateEvenement() != null ? dateFormatter.format(event.getDateEvenement()) : "";
                String timeStr = event.getTimestart() != null ? timeFormatter.format(event.getTimestart()) : "";
                dateTimeLabel.setText("Date: " + dateStr + " | Time: " + timeStr);
                dateTimeLabel.getStyleClass().add("event-info-label");
                
                // Location
                Label locationLabel = new Label("Location: " + event.getLieu());
                locationLabel.getStyleClass().add("event-info-label");
                
                detailsRow.getChildren().addAll(dateTimeLabel, new Label(" | "), locationLabel);
                
                // Create seats and price row
                HBox priceRow = new HBox(10);
                priceRow.setAlignment(Pos.CENTER_LEFT);
                
                // Available Seats with visual indicator
                Label seatsLabel = new Label("Available Seats: " + event.getNombreBillets());
                
                // Color based on availability
                if (event.getNombreBillets() < 10) {
                    seatsLabel.getStyleClass().addAll("event-info-label", "event-seats-low");
                } else if (event.getNombreBillets() < 30) {
                    seatsLabel.getStyleClass().addAll("event-info-label", "event-seats-medium");
                } else {
                    seatsLabel.getStyleClass().addAll("event-info-label", "event-seats-high");
                }
                
                // Seat Price
                Label priceLabel = new Label("Seat Price: " + currencyFormatter.format(event.getSeatprice()));
                priceLabel.getStyleClass().add("event-info-label");
                
                // Donation Goal
                Label donationLabel = new Label("Donation Goal: " + currencyFormatter.format(event.getDonation_objective()));
                donationLabel.getStyleClass().add("event-info-label");
                
                priceRow.getChildren().addAll(seatsLabel, new Label(" | "), priceLabel, new Label(" | "), donationLabel);
                
                // Add all info to details pane
                detailsPane.getChildren().addAll(titleRow, detailsRow, priceRow);
                
                // Create action buttons
                Button editBtn = new Button("Edit");
                editBtn.getStyleClass().addAll("event-button", "edit-button");
                editBtn.setOnAction(e -> handleEditEvent(event));
                
                Button deleteBtn = new Button("Delete");
                deleteBtn.getStyleClass().addAll("event-button", "delete-button");
                deleteBtn.setOnAction(e -> handleDeleteEvent(event));
                
                actionsPane.getChildren().addAll(editBtn, deleteBtn);
                
                setGraphic(mainContainer);
            }
        }
    }
} 
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class Eventbacklist implements Initializable {

    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button addEventButton;
    
    @FXML
    private TableView<Event> eventTable;
    
    @FXML
    private TableColumn<Event, Integer> idColumn;
    
    @FXML
    private TableColumn<Event, String> titleColumn;
    
    @FXML
    private TableColumn<Event, LocalDateTime> dateColumn;
    
    @FXML
    private TableColumn<Event, LocalTime> timeColumn;
    
    @FXML
    private TableColumn<Event, String> locationColumn;
    
    @FXML
    private TableColumn<Event, Integer> ticketsColumn;
    
    @FXML
    private TableColumn<Event, Double> priceColumn;
    
    @FXML
    private TableColumn<Event, Double> donationObjectiveColumn;
    
    @FXML
    private TableColumn<Event, Void> actionsColumn;
    
    @FXML
    private Label totalEventsLabel;
    
    // New dashboard stat labels
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
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        eventService = new EventService();
        
        // Initialize the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idevent"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        
        // Format the date column
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEvenement"));
        dateColumn.setCellFactory(column -> new TableCell<Event, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        
        // Format the time column
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timestart"));
        timeColumn.setCellFactory(column -> new TableCell<Event, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(formatter.format(time));
                }
            }
        });
        
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        ticketsColumn.setCellValueFactory(new PropertyValueFactory<>("nombreBillets"));
        
        // Format the price column with currency
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("seatprice"));
        priceColumn.setCellFactory(column -> new TableCell<Event, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(price));
                }
            }
        });
        
        // Format the donation objective column with currency
        donationObjectiveColumn.setCellValueFactory(new PropertyValueFactory<>("donation_objective"));
        donationObjectiveColumn.setCellFactory(column -> new TableCell<Event, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormatter.format(amount));
                }
            }
        });
        
        // Configure the actions column
        setupActionsColumn();
        
        // Load the events
        loadEvents();
        
        // Configure search functionality
        setupSearch();
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Event, Void> call(final TableColumn<Event, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);
                    
                    {
                        // Configure edit button
                        editBtn.setOnAction(event -> {
                            Event data = getTableView().getItems().get(getIndex());
                            handleEditEvent(data);
                        });
                        
                        // Configure delete button
                        deleteBtn.setOnAction(event -> {
                            Event data = getTableView().getItems().get(getIndex());
                            handleDeleteEvent(data);
                        });
                        
                        // Style buttons
                        editBtn.getStyleClass().add("table-edit-button");
                        deleteBtn.getStyleClass().add("table-delete-button");
                        editBtn.getStyleClass().add("table-action-button");
                        deleteBtn.getStyleClass().add("table-action-button");
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void loadEvents() {
        try {
            // Get all events from the service
            eventList = FXCollections.observableArrayList(eventService.findAll());
            
            // Set up filtered list
            filteredEvents = new FilteredList<>(eventList, p -> true);
            
            // Set the table data
            eventTable.setItems(filteredEvents);
            
            // Update the total events label
            totalEventsLabel.setText("Total Events: " + eventList.size());
            
            // Update dashboard stats
            updateDashboardStats();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load events", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateDashboardStats() {
        try {
            // Total events
            int totalEvents = eventList.size();
            totalEventsCount.setText(String.valueOf(totalEvents));
            
            // Upcoming events
            int upcoming = eventService.countUpcomingEvents();
            upcomingEventsCount.setText(String.valueOf(upcoming));
            
            // Calculate total tickets
            int totalTickets = 0;
            for (Event event : eventList) {
                totalTickets += event.getNombreBillets();
            }
            totalTicketsCount.setText(String.valueOf(totalTickets));
            
            // Calculate total donation goals
            double totalDonations = 0;
            for (Event event : eventList) {
                totalDonations += event.getDonation_objective();
            }
            totalDonationGoals.setText(currencyFormatter.format(totalDonations));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                
                // Search in the description
                return event.getDescription().toLowerCase().contains(searchText);
            });
            
            totalEventsLabel.setText("Total Events: " + filteredEvents.size());
        });
    }
    
    @FXML
    private void handleSearch() {
        // The search is already handled by the text listener
        // This is just for the button click
    }
    
    @FXML
    private void handleRefresh() {
        loadEvents();
        searchField.clear();
    }
    
    @FXML
    private void handleAddEvent() {
        try {
            // Load the add event form FXML
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
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add event form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleEditEvent(Event event) {
        try {
            // Load the edit form FXML
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
            }
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit form", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleDeleteEvent(Event event) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to delete the event: " + event.getTitre() + "?", 
                ButtonType.YES, ButtonType.NO);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Event");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    eventService.delete(event);
                    eventList.remove(event);
                    totalEventsLabel.setText("Total Events: " + eventList.size());
                    
                    // Update dashboard stats after deletion
                    updateDashboardStats();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Event Deleted", 
                            "The event was successfully deleted.");
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
} 
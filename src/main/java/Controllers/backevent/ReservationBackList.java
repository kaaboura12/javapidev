package Controllers.backevent;

import Models.Reservation;
import Models.Event;
import Models.User;
import Services.ReservationService;
import Services.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReservationBackList implements Initializable {

    @FXML
    private Label totalReservationsCount, pendingReservationsCount, completedReservationsCount, totalAmountRaised;
    
    @FXML
    private Label totalCount, pendingCount, completedCount, cancelledCount;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton, addReservationButton;
    
    @FXML
    private TableView<Reservation> reservationTable;
    
    @FXML
    private TableColumn<Reservation, Integer> idColumn;
    
    @FXML
    private TableColumn<Reservation, String> userColumn, eventColumn, statusColumn, seatsColumn;
    
    @FXML
    private TableColumn<Reservation, Double> amountColumn;
    
    @FXML
    private TableColumn<Reservation, String> dateColumn;
    
    @FXML
    private TableColumn<Reservation, Void> actionsColumn;
    
    @FXML
    private Pagination pagination;
    
    @FXML
    private Label totalReservationsLabel, lastUpdatedLabel;
    
    private ReservationService reservationService;
    private EventService eventService;
    private ObservableList<Reservation> reservationsList = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 10;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reservationService = new ReservationService();
        eventService = new EventService();
        
        setupTableColumns();
        loadReservationData();
        setupPagination();
        updateStatistics();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_reservation"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats_reserved"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("total_amount"));
        
        // Custom cell factory for date
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getReservation_date() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> cellData.getValue().getReservation_date().format(dateFormatter)
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(() -> "");
        });
        
        // Custom cell factory for event name
        eventColumn.setCellValueFactory(cellData -> {
            Event event = cellData.getValue().getEvent();
            if (event != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> event.getTitre()
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> "Event #" + cellData.getValue().getIdevent()
            );
        });
        
        // Custom cell factory for user name
        userColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            if (user != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> user.getNom() + " " + user.getPrenom()
                );
            }
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> "User #" + cellData.getValue().getUserid()
            );
        });
        
        // Format the amount column to display currency
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // We don't have status field in Reservation model, but we can add it later or use a placeholder
        statusColumn.setCellValueFactory(cellData -> {
            // Use a placeholder status based on date for now
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> "Confirmed"
            );
        });
        
        // Setup Actions column with Details button
        setupActionsColumn();
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> {
            return new TableCell<Reservation, Void>() {
                private final Button detailsButton = new Button("Details");
                
                {
                    detailsButton.getStyleClass().add("details-button");
                    detailsButton.setOnAction(event -> {
                        Reservation reservation = getTableView().getItems().get(getIndex());
                        openReservationDetails(reservation);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox container = new HBox(5);
                        container.getChildren().add(detailsButton);
                        setGraphic(container);
                    }
                }
            };
        });
    }
    
    private void openReservationDetails(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backreservation/editreservation.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the reservation
            Controller.Backend.editreservationController controller = loader.getController();
            controller.setReservation(reservation);
            
            // Set the refresh callback to reload data when returning from edit
            controller.setRefreshCallback(new Controller.Backend.editreservationController.RefreshCallback() {
                @Override
                public void onRefresh() {
                    // Reload reservation data and update UI
                    loadReservationData();
                    updateStatistics();
                    setupPagination();
                }
            });
            
            // Create a new stage for the details view
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Reservation Details");
            detailsStage.setScene(new Scene(root));
            detailsStage.show();
            
        } catch (IOException e) {
            showAlert("Error opening reservation details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupPagination() {
        int pageCount = Math.max(1, (reservationsList.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        
        pagination.setPageFactory(pageIndex -> {
            int fromIndex = pageIndex * ROWS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, reservationsList.size());
            
            if (fromIndex >= reservationsList.size()) {
                reservationTable.setItems(FXCollections.emptyObservableList());
            } else {
                reservationTable.setItems(FXCollections.observableArrayList(
                    reservationsList.subList(fromIndex, toIndex)));
            }
            
            return reservationTable;
        });
    }
    
    private void loadReservationData() {
        try {
            List<Reservation> reservations = reservationService.findAll();
            reservationsList.clear();
            reservationsList.addAll(reservations);
            
            if (!reservationsList.isEmpty()) {
                reservationTable.setItems(FXCollections.observableArrayList(
                    reservationsList.subList(0, Math.min(ROWS_PER_PAGE, reservationsList.size()))));
            }
        } catch (SQLException e) {
            showAlert("Error loading reservations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatistics() {
        try {
            // Count total reservations
            int total = reservationsList.size();
            totalCount.setText(String.valueOf(total));
            
            // Calculate total amount
            double totalAmount = reservationsList.stream()
                .mapToDouble(Reservation::getTotal_amount)
                .sum();
            
            // Update counters for display
            totalReservationsLabel.setText("Total Reservations: " + total);
            lastUpdatedLabel.setText("Last Updated: " + java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            
            // For now, use placeholder values for pending, completed, etc.
            // In a real implementation, you would categorize reservations by status
            pendingCount.setText("0");
            completedCount.setText(String.valueOf(total));
            cancelledCount.setText("0");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase();
        
        try {
            List<Reservation> allReservations = reservationService.findAll();
            
            ObservableList<Reservation> filteredList = allReservations.stream()
                .filter(reservation -> 
                    (reservation.getUser() != null && (
                        reservation.getUser().getNom().toLowerCase().contains(searchTerm) ||
                        reservation.getUser().getPrenom().toLowerCase().contains(searchTerm) ||
                        reservation.getUser().getEmail().toLowerCase().contains(searchTerm))) ||
                    (reservation.getEvent() != null && 
                        reservation.getEvent().getTitre().toLowerCase().contains(searchTerm))
                )
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            
            reservationsList.clear();
            reservationsList.addAll(filteredList);
            
            // Update pagination for filtered results
            setupPagination();
            
        } catch (SQLException e) {
            showAlert("Error searching reservations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddReservation() {
        // TODO: Implement adding a new reservation
        showAlert("Add reservation functionality not yet implemented.");
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package Controllers.backevent;

import Models.Donation;
import Models.Event;
import Services.DonationService;
import Services.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class donationBackList implements Initializable {

    @FXML
    private Label totalDonationsCount, pendingDonationsCount, completedDonationsCount, totalAmountRaised;
    
    @FXML
    private Label totalCount, pendingCount, completedCount, rejectedCount;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton, addDonationButton;
    
    @FXML
    private TableView<Donation> donationTable;
    
    @FXML
    private TableColumn<Donation, Integer> idColumn;
    
    @FXML
    private TableColumn<Donation, String> donorColumn, eventColumn, statusColumn, typeColumn, notesColumn;
    
    @FXML
    private TableColumn<Donation, Double> amountColumn;
    
    @FXML
    private TableColumn<Donation, String> dateColumn;
    
    @FXML
    private Pagination pagination;
    
    @FXML
    private Label totalDonationsLabel, lastUpdatedLabel;
    
    private DonationService donationService;
    private EventService eventService;
    private ObservableList<Donation> donationsList = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 10;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        donationService = new DonationService();
        eventService = new EventService();
        
        setupTableColumns();
        loadDonationData();
        setupPagination();
        updateStatistics();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("iddon"));
        donorColumn.setCellValueFactory(new PropertyValueFactory<>("donorname"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        
        // Custom cell factory for date
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return javafx.beans.binding.Bindings.createStringBinding(
                    () -> cellData.getValue().getDate().format(dateFormatter)
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
        
        // For now, we don't have status in Donation model, but we can use payment method as type
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("payment_method"));
        
        // Note is a placeholder for num_tlf
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("num_tlf"));
        
        // Format the amount column to display currency
        amountColumn.setCellFactory(column -> new TableCell<Donation, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f", amount));
                }
            }
        });
        
        // For now, status is not in the model, but we can simulate it
        statusColumn.setCellFactory(column -> new TableCell<Donation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty) {
                    setText(null);
                    getStyleClass().removeAll("status-completed", "status-pending", "status-rejected");
                } else {
                    Donation donation = getTableView().getItems().get(getIndex());
                    // Simulate status based on donation amount
                    String simulatedStatus;
                    if (donation.getMontant() > 300) {
                        simulatedStatus = "Completed";
                        getStyleClass().add("status-completed");
                    } else if (donation.getMontant() > 100) {
                        simulatedStatus = "Pending";
                        getStyleClass().add("status-pending");
                    } else {
                        simulatedStatus = "Rejected";
                        getStyleClass().add("status-rejected");
                    }
                    setText(simulatedStatus);
                }
            }
        });
    }
    
    private void setupPagination() {
        int pageCount = Math.max(1, (donationsList.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        
        pagination.setPageFactory(pageIndex -> {
            int fromIndex = pageIndex * ROWS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, donationsList.size());
            
            if (fromIndex >= donationsList.size()) {
                donationTable.setItems(FXCollections.emptyObservableList());
            } else {
                donationTable.setItems(FXCollections.observableArrayList(
                    donationsList.subList(fromIndex, toIndex)));
            }
            
            return donationTable;
        });
    }
    
    private void loadDonationData() {
        try {
            List<Donation> donations = donationService.findAll();
            donationsList.clear();
            donationsList.addAll(donations);
            
            if (!donationsList.isEmpty()) {
                donationTable.setItems(FXCollections.observableArrayList(
                    donationsList.subList(0, Math.min(ROWS_PER_PAGE, donationsList.size()))));
            }
        } catch (SQLException e) {
            showAlert("Error loading donations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatistics() {
        int total = donationsList.size();
        int pending = 0;
        int completed = 0;
        int rejected = 0;
        double totalAmount = 0;
        
        for (Donation donation : donationsList) {
            // Simulate status based on donation amount (same logic as in cell factory)
            if (donation.getMontant() > 300) {
                completed++;
            } else if (donation.getMontant() > 100) {
                pending++;
            } else {
                rejected++;
            }
            totalAmount += donation.getMontant();
        }
        
        // Update the counts
        totalCount.setText(String.valueOf(total));
        pendingCount.setText(String.valueOf(pending));
        completedCount.setText(String.valueOf(completed));
        rejectedCount.setText(String.valueOf(rejected));
        
        // Update the hidden counts for use elsewhere
        totalDonationsCount.setText(String.valueOf(total));
        pendingDonationsCount.setText(String.valueOf(pending));
        completedDonationsCount.setText(String.valueOf(completed));
        totalAmountRaised.setText(String.format("$%.2f", totalAmount));
        
        // Update footer
        totalDonationsLabel.setText("Total Donations: " + total);
        lastUpdatedLabel.setText("Last Updated: " + java.time.LocalDate.now().toString());
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase();
        
        try {
            List<Donation> allDonations = donationService.findAll();
            
            ObservableList<Donation> filteredList = allDonations.stream()
                .filter(donation -> 
                    (donation.getDonorname() != null && donation.getDonorname().toLowerCase().contains(searchTerm)) ||
                    (donation.getEmail() != null && donation.getEmail().toLowerCase().contains(searchTerm)) ||
                    (donation.getEvent() != null && donation.getEvent().getTitre().toLowerCase().contains(searchTerm)) ||
                    (donation.getPayment_method() != null && donation.getPayment_method().toLowerCase().contains(searchTerm)) ||
                    (donation.getNum_tlf() != null && donation.getNum_tlf().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            
            donationsList.clear();
            donationsList.addAll(filteredList);
            
            // Update pagination for filtered results
            setupPagination();
            
        } catch (SQLException e) {
            showAlert("Error searching donations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddDonation() {
        // TODO: Implement adding a new donation
        showAlert("Add donation functionality not yet implemented.");
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

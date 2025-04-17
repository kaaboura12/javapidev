package Controllers.backevent;

import Models.Donation;
import Models.Event;
import Services.DonationService;
import Services.EventService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class donationBackList implements Initializable {

    @FXML
    private Label totalDonationsCount, pendingDonationsCount, completedDonationsCount, totalAmountRaised;
    
    @FXML
    private Label completedCount;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TableView<Donation> donationTable;
    
    @FXML
    private TableColumn<Donation, Integer> idColumn;
    
    @FXML
    private TableColumn<Donation, String> donorColumn, eventColumn, typeColumn, notesColumn;
    
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
    private FilteredList<Donation> filteredDonations;
    private static final int ROWS_PER_PAGE = 10;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        donationService = new DonationService();
        eventService = new EventService();
        
        // Setup search field listener for dynamic filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                resetSearchFilter();
            }
        });
        
        // Make table responsive
        makeTableResponsive();
        
        setupTableColumns();
        loadDonationData();
        setupPagination();
        updateStatistics();
        updateLastUpdatedLabel();
    }
    
    private void makeTableResponsive() {
        // Make columns resize with the table
        donationTable.widthProperty().addListener((source, oldWidth, newWidth) -> {
            double tableWidth = (double) newWidth;
            idColumn.setPrefWidth(tableWidth * 0.05); // 5%
            donorColumn.setPrefWidth(tableWidth * 0.17); // 17%
            amountColumn.setPrefWidth(tableWidth * 0.12); // 12%
            dateColumn.setPrefWidth(tableWidth * 0.12); // 12%
            eventColumn.setPrefWidth(tableWidth * 0.22); // 22%
            typeColumn.setPrefWidth(tableWidth * 0.12); // 12%
            notesColumn.setPrefWidth(tableWidth * 0.2); // 20%
        });
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("iddon"));
        donorColumn.setCellValueFactory(new PropertyValueFactory<>("donorname"));
        
        // Format amount with currency
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        amountColumn.setCellFactory(column -> new TableCell<Donation, Double>() {
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
        
        // Custom cell factory for date
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter));
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Custom cell factory for event name
        eventColumn.setCellValueFactory(cellData -> {
            Event event = cellData.getValue().getEvent();
            if (event != null) {
                return new SimpleStringProperty(event.getTitre());
            }
            return new SimpleStringProperty("Event #" + cellData.getValue().getIdevent());
        });
        
        // For now, we don't have status in Donation model, but we can use payment method as type
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("payment_method"));
        
        // Note is a placeholder for num_tlf
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("num_tlf"));

        // Add action buttons to table - only View and Delete (no Edit button)
        TableColumn<Donation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(120);
        actionsCol.setCellFactory(param -> new TableCell<Donation, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, viewBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                viewBtn.getStyleClass().addAll("table-action-button");
                deleteBtn.getStyleClass().addAll("table-action-button", "delete-button");
                
                viewBtn.setOnAction(event -> {
                    Donation donation = getTableView().getItems().get(getIndex());
                    handleViewDonation(donation);
                });
                
                deleteBtn.setOnAction(event -> {
                    Donation donation = getTableView().getItems().get(getIndex());
                    handleDeleteDonation(donation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        donationTable.getColumns().add(actionsCol);
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
            
            return null;
        });
    }
    
    private void loadDonationData() {
        try {
            List<Donation> donations = donationService.findAll();
            donationsList.clear();
            donationsList.addAll(donations);
            
            // Create filtered list wrapper
            filteredDonations = new FilteredList<>(donationsList, p -> true);
            
            if (!donationsList.isEmpty()) {
                refreshTableView();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading donations", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void refreshTableView() {
        pagination.setPageCount(calculatePageCount());
        int currentPageIndex = Math.min(pagination.getCurrentPageIndex(), calculatePageCount() - 1);
        pagination.setCurrentPageIndex(currentPageIndex);
        
        // Update displayed items
        int fromIndex = currentPageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredDonations.size());
        
        if (fromIndex >= filteredDonations.size()) {
            donationTable.setItems(FXCollections.emptyObservableList());
        } else {
            ObservableList<Donation> pageItems = FXCollections.observableArrayList(
                filteredDonations.subList(fromIndex, toIndex));
            donationTable.setItems(pageItems);
        }
    }
    
    private int calculatePageCount() {
        return Math.max(1, (filteredDonations.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
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
        
        // Update the counts - only update if label exists
        if (completedCount != null) {
            completedCount.setText(String.valueOf(completed));
        }
        
        // Keep track of all stats for potential future use
        totalDonationsCount.setText(String.valueOf(total));
        pendingDonationsCount.setText(String.valueOf(pending));
        completedDonationsCount.setText(String.valueOf(completed));
        totalAmountRaised.setText(currencyFormatter.format(totalAmount));
        
        // Update footer
        totalDonationsLabel.setText("Total Donations: " + total);
    }
    
    private void updateLastUpdatedLabel() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm");
        lastUpdatedLabel.setText("Last Updated: " + formatter.format(now));
    }
    
    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        
        if (searchTerm.isEmpty()) {
            resetSearchFilter();
            return;
        }
        
        filteredDonations.setPredicate(donation -> 
            (donation.getDonorname() != null && donation.getDonorname().toLowerCase().contains(searchTerm)) ||
            (donation.getEmail() != null && donation.getEmail().toLowerCase().contains(searchTerm)) ||
            (donation.getEvent() != null && donation.getEvent().getTitre().toLowerCase().contains(searchTerm)) ||
            (donation.getPayment_method() != null && donation.getPayment_method().toLowerCase().contains(searchTerm)) ||
            (donation.getNum_tlf() != null && donation.getNum_tlf().toLowerCase().contains(searchTerm))
        );
        
        refreshTableView();
    }
    
    private void resetSearchFilter() {
        filteredDonations.setPredicate(p -> true);
        refreshTableView();
    }
    
    private void handleViewDonation(Donation donation) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backdonation/viewdonation.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the donation ID
            donationviewcontroller controller = loader.getController();
            controller.loadDonationDetails(donation.getIddon());
            
            // Find the main content area (AnchorPane with ID "contentArea")
            AnchorPane contentArea = findContentArea(donationTable);
            
            if (contentArea != null) {
                // Set anchors for the new view
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                
                // Clear content area and add new view
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
                
                // Update the title if possible
                updatePageTitle("Donation Details");
            } else {
                // Fallback: Create a new window/stage
                Stage stage = new Stage();
                stage.setTitle("Donation Details");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(donationTable.getScene().getWindow());
                stage.show();
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Failed to load donation details view: " + e.getMessage());
            alert.showAndWait();
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
            Scene scene = donationTable.getScene();
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
    
    private void handleDeleteDonation(Donation donation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Donation");
        confirmDialog.setContentText("Are you sure you want to delete the donation from " + 
                donation.getDonorname() + " for " + currencyFormatter.format(donation.getMontant()) + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    donationService.delete(donation.getIddon());
                    
                    // Refresh the table and statistics
                    loadDonationData();
                    updateStatistics();
                    updateLastUpdatedLabel();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Donation Deleted", 
                            "The donation has been successfully deleted.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete donation", e.getMessage());
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

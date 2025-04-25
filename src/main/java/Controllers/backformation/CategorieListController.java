package Controllers.backformation;

import Models.Categorie;
import Models.Formation;
import Services.CategorieService;
import Services.FormationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.util.Callback;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CategorieListController {
    @FXML private TableView<Categorie> categorieTable;
    @FXML private TableColumn<Categorie, Integer> idColumn;
    @FXML private TableColumn<Categorie, String> nomColumn;
    @FXML private TableColumn<Categorie, String> descriptionColumn;
    @FXML private TableColumn<Categorie, String> dateCreationColumn;
    @FXML private TableColumn<Categorie, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Label categorieCountLabel;
    
    private CategorieService categorieService;
    private ObservableList<Categorie> categoriesList;
    private FilteredList<Categorie> filteredCategories;
    
    @FXML
    public void initialize() {
        categorieService = new CategorieService();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Format date column
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateCreationColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getDateCreation().format(formatter)));
        
        // Configure action buttons column
        setupActionColumn();
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredCategories != null) {
                filteredCategories.setPredicate(categorie -> {
                    // If filter text is empty, display all categories
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    
                    // Check if name or description contains the filter
                    if (categorie.getNom().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (categorie.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    
                    return false;
                });
                
                updateCategorieCount();
            }
        });
        
        loadCategories();
    }
    
    private void setupActionColumn() {
        Callback<TableColumn<Categorie, Void>, TableCell<Categorie, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Categorie, Void> call(final TableColumn<Categorie, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);
                    
                    {
                        // Configure edit button
                        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        editBtn.setOnAction(event -> {
                            Categorie categorie = getTableView().getItems().get(getIndex());
                            handleEditCategorie(categorie);
                        });
                        
                        // Configure delete button
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        deleteBtn.setOnAction(event -> {
                            Categorie categorie = getTableView().getItems().get(getIndex());
                            handleDeleteCategorie(categorie);
                        });
                        
                        pane.setAlignment(Pos.CENTER);
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void loadCategories() {
        try {
            categoriesList = FXCollections.observableArrayList(categorieService.getAll());
            filteredCategories = new FilteredList<>(categoriesList, p -> true);
            categorieTable.setItems(filteredCategories);
            updateCategorieCount();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    private void updateCategorieCount() {
        categorieCountLabel.setText("Total Categories: " + filteredCategories.size());
    }
    
    @FXML
    private void handleResetFilter() {
        searchField.clear();
        
        if (filteredCategories != null) {
            filteredCategories.setPredicate(p -> true);
            updateCategorieCount();
        }
    }
    
    @FXML
    private void handleRefreshButton() {
        loadCategories();
    }
    
    @FXML
    private void handleAddCategorieButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backformation/categorie_add.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh the table after adding a new category
            loadCategories();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add category form: " + e.getMessage());
        }
    }
    
    private void handleEditCategorie(Categorie categorie) {
        if (categorie == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Warning", "Please select a category to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backformation/categorie_edit.fxml"));
            Parent root = loader.load();
            
            CategorieEditController controller = loader.getController();
            controller.setCategorie(categorie);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Category");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh the table after editing
            loadCategories();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit category form: " + e.getMessage());
        }
    }
    
    private void handleDeleteCategorie(Categorie categorie) {
        if (categorie == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Warning", "Please select a category to delete.");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete the category: " + categorie.getNom() + "?\n\nThis will also delete all formations associated with this category.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete the category (and associated formations) using the service
                    categorieService.delete(categorie);
                    
                    // Refresh the table
                    loadCategories();
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Category and all associated formations were deleted successfully.");
                    
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete category: " + e.getMessage());
                }
            }
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
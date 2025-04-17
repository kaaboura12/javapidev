package Controllers.backformation;

import Models.Formation;
import Models.Categorie;
import Services.FormationService;
import Services.CategorieService;
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

public class FormationListController {
    @FXML private TableView<Formation> formationTable;
    @FXML private TableColumn<Formation, Integer> idColumn;
    @FXML private TableColumn<Formation, String> titreColumn;
    @FXML private TableColumn<Formation, String> descriptionColumn;
    @FXML private TableColumn<Formation, String> dateDebutColumn;
    @FXML private TableColumn<Formation, String> dateFinColumn;
    @FXML private TableColumn<Formation, Integer> nbrpartColumn;
    @FXML private TableColumn<Formation, Double> prixColumn;
    @FXML private TableColumn<Formation, String> categorieColumn;
    @FXML private TableColumn<Formation, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<Categorie> categorieFilterCombo;
    @FXML private Label formationCountLabel;
    
    private FormationService formationService;
    private CategorieService categorieService;
    private ObservableList<Formation> formationsList;
    private FilteredList<Formation> filteredFormations;
    
    @FXML
    public void initialize() {
        formationService = new FormationService();
        categorieService = new CategorieService();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        // Format date columns
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        dateDebutColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getDateDebut().format(formatter)));
        
        dateFinColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getDateFin().format(formatter)));
        
        nbrpartColumn.setCellValueFactory(new PropertyValueFactory<>("nbrpart"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        
        // Display category name instead of object
        categorieColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getCategorie().getNom()));
        
        // Configure action buttons column
        setupActionColumn();
        
        // Load categories for filter dropdown
        try {
            // Add 'All Categories' option
            Categorie allCategories = new Categorie();
            allCategories.setId(-1);
            allCategories.setNom("All Categories");
            
            ObservableList<Categorie> categories = FXCollections.observableArrayList();
            categories.add(allCategories);
            categories.addAll(categorieService.getAll());
            
            categorieFilterCombo.setItems(categories);
            categorieFilterCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredFormations != null) {
                filteredFormations.setPredicate(formation -> {
                    // If filter text is empty, display all formations
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    
                    // Check if title or description contains the filter
                    if (formation.getTitre().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (formation.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    
                    return false;
                });
                
                updateFormationCount();
            }
        });
        
        loadFormations();
    }
    
    private void setupActionColumn() {
        Callback<TableColumn<Formation, Void>, TableCell<Formation, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Formation, Void> call(final TableColumn<Formation, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);
                    
                    {
                        // Configure edit button
                        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        editBtn.setOnAction(event -> {
                            Formation formation = getTableView().getItems().get(getIndex());
                            handleEditFormation(formation);
                        });
                        
                        // Configure delete button
                        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        deleteBtn.setOnAction(event -> {
                            Formation formation = getTableView().getItems().get(getIndex());
                            handleDeleteFormation(formation);
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
    
    private void loadFormations() {
        try {
            formationsList = FXCollections.observableArrayList(formationService.getAll());
            filteredFormations = new FilteredList<>(formationsList, p -> true);
            formationTable.setItems(filteredFormations);
            updateFormationCount();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load formations: " + e.getMessage());
        }
    }
    
    private void updateFormationCount() {
        formationCountLabel.setText("Total Formations: " + filteredFormations.size());
    }
    
    @FXML
    private void handleFilterFormations() {
        if (filteredFormations != null) {
            Categorie selectedCategorie = categorieFilterCombo.getValue();
            
            filteredFormations.setPredicate(formation -> {
                // If "All Categories" is selected or no category is selected
                if (selectedCategorie == null || selectedCategorie.getId() == -1) {
                    return true;
                }
                
                // Compare category IDs
                return formation.getCategorie().getId() == selectedCategorie.getId();
            });
            
            updateFormationCount();
        }
    }
    
    @FXML
    private void handleResetFilter() {
        searchField.clear();
        categorieFilterCombo.getSelectionModel().selectFirst();
        
        if (filteredFormations != null) {
            filteredFormations.setPredicate(p -> true);
            updateFormationCount();
        }
    }
    
    @FXML
    private void handleRefreshButton() {
        loadFormations();
    }
    
    @FXML
    private void handleCategoriesManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backformation/categorie_list.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Categories Management");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh categories filter and formations when returning from categories management
            loadCategoriesForFilter();
            loadFormations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open categories management: " + e.getMessage());
        }
    }
    
    private void loadCategoriesForFilter() {
        try {
            // Add 'All Categories' option
            Categorie allCategories = new Categorie();
            allCategories.setId(-1);
            allCategories.setNom("All Categories");
            
            ObservableList<Categorie> categories = FXCollections.observableArrayList();
            categories.add(allCategories);
            categories.addAll(categorieService.getAll());
            
            categorieFilterCombo.setItems(categories);
            categorieFilterCombo.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAddFormationButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backformation/formation_add.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Formation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh the table after adding a new formation
            loadFormations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open add formation form: " + e.getMessage());
        }
    }
    
    private void handleEditFormation(Formation formation) {
        if (formation == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Warning", "Please select a formation to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backformation/formation_edit.fxml"));
            Parent root = loader.load();
            
            FormationEditController controller = loader.getController();
            controller.setFormation(formation);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Formation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh the table after editing
            loadFormations();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit formation form: " + e.getMessage());
        }
    }
    
    private void handleDeleteFormation(Formation formation) {
        if (formation == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Warning", "Please select a formation to delete.");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete the formation: " + formation.getTitre() + "?");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    formationService.delete(formation);
                    loadFormations();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Formation deleted successfully.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete formation: " + e.getMessage());
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
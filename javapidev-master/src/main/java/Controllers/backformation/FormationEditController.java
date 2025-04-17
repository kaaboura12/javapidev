package Controllers.backformation;

import Models.Formation;
import Models.Categorie;
import Services.FormationService;
import Services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.sql.SQLException;

public class FormationEditController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField nbrpartField;
    @FXML private TextField prixField;
    @FXML private ComboBox<Categorie> categorieCombo;
    @FXML private TextField videoField;
    
    private FormationService formationService;
    private CategorieService categorieService;
    private Formation formation;
    
    @FXML
    public void initialize() {
        formationService = new FormationService();
        categorieService = new CategorieService();
        
        try {
            categorieCombo.getItems().addAll(categorieService.getAll());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load categories: " + e.getMessage());
        }
        
        // Add input validation for numeric fields
        nbrpartField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                nbrpartField.setText(oldValue);
            }
        });
        
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*\\.?\\d*")) {
                prixField.setText(oldValue);
            }
        });
    }
    
    public void setFormation(Formation formation) {
        this.formation = formation;
        populateFields();
    }
    
    private void populateFields() {
        titreField.setText(formation.getTitre());
        descriptionArea.setText(formation.getDescription());
        dateDebutPicker.setValue(formation.getDateDebut().toLocalDate());
        dateFinPicker.setValue(formation.getDateFin().toLocalDate());
        nbrpartField.setText(String.valueOf(formation.getNbrpart()));
        prixField.setText(String.valueOf(formation.getPrix()));
        videoField.setText(formation.getVideo());
        
        // Find and select the correct category in the combobox
        for (Categorie categorie : categorieCombo.getItems()) {
            if (categorie.getId() == formation.getCategorie().getId()) {
                categorieCombo.setValue(categorie);
                break;
            }
        }
    }
    
    @FXML
    private void handleUpdateFormation(ActionEvent event) {
        if (validateInputs()) {
            try {
                formation.setTitre(titreField.getText());
                formation.setDescription(descriptionArea.getText());
                formation.setDateDebut(dateDebutPicker.getValue().atStartOfDay());
                formation.setDateFin(dateFinPicker.getValue().atStartOfDay());
                formation.setNbrpart(Integer.parseInt(nbrpartField.getText()));
                formation.setPrix(Double.parseDouble(prixField.getText()));
                formation.setCategorie(categorieCombo.getValue());
                formation.setVideo(videoField.getText());
                
                formationService.update(formation);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Formation updated successfully!");
                closeWindow(event);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update formation: " + e.getMessage());
            }
        }
    }
    
    private boolean validateInputs() {
        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Title is required");
            return false;
        }
        
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Description is required");
            return false;
        }
        
        if (dateDebutPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Start date is required");
            return false;
        }
        
        if (dateFinPicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "End date is required");
            return false;
        }
        
        if (dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Start date cannot be after end date");
            return false;
        }
        
        if (nbrpartField.getText() == null || nbrpartField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Number of participants is required");
            return false;
        }
        
        if (prixField.getText() == null || prixField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Price is required");
            return false;
        }
        
        if (categorieCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category is required");
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow(event);
    }
    
    private void closeWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
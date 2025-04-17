package Controllers.backformation;

import Models.Categorie;
import Services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.sql.SQLException;

public class CategorieEditController {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;
    
    private CategorieService categorieService;
    private Categorie categorie;
    
    @FXML
    public void initialize() {
        categorieService = new CategorieService();
    }
    
    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
        populateFields();
    }
    
    private void populateFields() {
        nomField.setText(categorie.getNom());
        descriptionArea.setText(categorie.getDescription());
    }
    
    @FXML
    private void handleUpdateCategorie(ActionEvent event) {
        if (validateInputs()) {
            try {
                categorie.setNom(nomField.getText());
                categorie.setDescription(descriptionArea.getText());
                
                categorieService.update(categorie);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category updated successfully!");
                closeWindow(event);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update category: " + e.getMessage());
            }
        }
    }
    
    private boolean validateInputs() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Category name is required");
            return false;
        }
        
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Description is required");
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
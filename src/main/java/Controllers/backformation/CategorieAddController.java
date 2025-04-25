package Controllers.backformation;

import Models.Categorie;
import Services.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.time.LocalDateTime;
import java.sql.SQLException;

public class CategorieAddController {
    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;

    private CategorieService categorieService;

    @FXML
    public void initialize() {
        categorieService = new CategorieService();
    }

    @FXML
    private void handleAddCategorie(ActionEvent event) {
        if (validateInputs()) {
            try {
                Categorie categorie = new Categorie();
                categorie.setNom(nomField.getText());
                categorie.setDescription(descriptionArea.getText());
                categorie.setDateCreation(LocalDateTime.now());

                categorieService.insert(categorie);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category added successfully!");
                closeWindow(event);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add category: " + e.getMessage());
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
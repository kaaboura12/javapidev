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
    @FXML private Label nomError;
    @FXML private Label descriptionError;

    private CategorieService categorieService;

    @FXML
    public void initialize() {
        categorieService = new CategorieService();
        
        // Initialize error labels
        nomError.setVisible(false);
        descriptionError.setVisible(false);

        // Validation en temps réel pour le nom
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                nomError.setText("Le nom est obligatoire");
                nomError.setVisible(true);
            } else if (newValue.length() < 3) {
                nomError.setText("Le nom doit contenir au moins 3 caractères");
                nomError.setVisible(true);
            } else if (!newValue.matches("^[a-zA-Z0-9\\s]+$")) {
                nomError.setText("Le nom ne doit contenir que des lettres, des chiffres et des espaces");
                nomError.setVisible(true);
            } else {
                nomError.setVisible(false);
            }
        });

        // Validation en temps réel pour la description
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descriptionError.setText("La description est obligatoire");
                descriptionError.setVisible(true);
            } else if (newValue.length() < 10) {
                descriptionError.setText("La description doit contenir au moins 10 caractères");
                descriptionError.setVisible(true);
            } else if (newValue.length() > 500) {
                descriptionError.setText("La description ne doit pas dépasser 500 caractères");
                descriptionError.setVisible(true);
            } else {
                descriptionError.setVisible(false);
            }
        });
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
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès!");
                closeWindow(event);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de la catégorie: " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        // Validation du nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            nomError.setVisible(true);
            isValid = false;
        } else if (nomField.getText().length() < 3) {
            nomError.setText("Le nom doit contenir au moins 3 caractères");
            nomError.setVisible(true);
            isValid = false;
        } else if (!nomField.getText().matches("^[a-zA-Z0-9\\s]+$")) {
            nomError.setText("Le nom ne doit contenir que des lettres, des chiffres et des espaces");
            nomError.setVisible(true);
            isValid = false;
        }
        
        // Validation de la description
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            descriptionError.setText("La description est obligatoire");
            descriptionError.setVisible(true);
            isValid = false;
        } else if (descriptionArea.getText().length() < 10) {
            descriptionError.setText("La description doit contenir au moins 10 caractères");
            descriptionError.setVisible(true);
            isValid = false;
        } else if (descriptionArea.getText().length() > 500) {
            descriptionError.setText("La description ne doit pas dépasser 500 caractères");
            descriptionError.setVisible(true);
            isValid = false;
        }
        
        return isValid;
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
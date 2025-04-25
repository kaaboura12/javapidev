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
import java.time.LocalDateTime;
import java.sql.SQLException;

public class FormationAddController {
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

    @FXML
    private void handleAddFormation(ActionEvent event) {
        if (validateInputs()) {
            try {
                Formation formation = new Formation();
                formation.setTitre(titreField.getText());
                formation.setDescription(descriptionArea.getText());
                formation.setDateDebut(dateDebutPicker.getValue().atStartOfDay());
                formation.setDateFin(dateFinPicker.getValue().atStartOfDay());
                formation.setNbrpart(Integer.parseInt(nbrpartField.getText()));
                formation.setPrix(Double.parseDouble(prixField.getText()));
                formation.setCategorie(categorieCombo.getValue());
                formation.setDateCreation(LocalDateTime.now());
                formation.setVideo(videoField.getText());

                formationService.insert(formation);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Formation added successfully!");
                closeWindow(event);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add formation: " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() {
        // Add your validation logic here similar to EventAddController
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

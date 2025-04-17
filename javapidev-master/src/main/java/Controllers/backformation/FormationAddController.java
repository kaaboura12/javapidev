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

    @FXML private Label titreError;
    @FXML private Label descriptionError;
    @FXML private Label dateDebutError;
    @FXML private Label dateFinError;
    @FXML private Label nbrpartError;
    @FXML private Label prixError;
    @FXML private Label categorieError;
    @FXML private Label videoError;

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

        // Validation en temps réel
        titreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                titreError.setText("Le titre est obligatoire");
                titreError.setVisible(true);
            } else if (newValue.length() < 3) {
                titreError.setText("Le titre doit contenir au moins 3 caractères");
                titreError.setVisible(true);
            } else {
                titreError.setVisible(false);
            }
        });

        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                descriptionError.setText("La description est obligatoire");
                descriptionError.setVisible(true);
            } else if (newValue.length() < 10) {
                descriptionError.setText("La description doit contenir au moins 10 caractères");
                descriptionError.setVisible(true);
            } else {
                descriptionError.setVisible(false);
            }
        });

        dateDebutPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                dateDebutError.setText("La date de début est obligatoire");
                dateDebutError.setVisible(true);
            } else if (newValue.isBefore(LocalDate.now())) {
                dateDebutError.setText("La date ne peut pas être dans le passé");
                dateDebutError.setVisible(true);
            } else {
                dateDebutError.setVisible(false);
            }
            validateDates();
        });

        dateFinPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateDates();
        });

        nbrpartField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.trim().isEmpty()) {
                    nbrpartError.setText("Le nombre de participants est obligatoire");
                    nbrpartError.setVisible(true);
                } else if (Integer.parseInt(newValue) <= 0) {
                    nbrpartError.setText("Le nombre doit être supérieur à 0");
                    nbrpartError.setVisible(true);
                } else {
                    nbrpartError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                nbrpartError.setText("Veuillez saisir un nombre valide");
                nbrpartError.setVisible(true);
            }
        });

        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue == null || newValue.trim().isEmpty()) {
                    prixError.setText("Le prix est obligatoire");
                    prixError.setVisible(true);
                } else if (Double.parseDouble(newValue) < 0) {
                    prixError.setText("Le prix ne peut pas être négatif");
                    prixError.setVisible(true);
                } else {
                    prixError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                prixError.setText("Veuillez saisir un prix valide");
                prixError.setVisible(true);
            }
        });

        categorieCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                categorieError.setText("La catégorie est obligatoire");
                categorieError.setVisible(true);
            } else {
                categorieError.setVisible(false);
            }
        });

        videoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty() && 
                !newValue.matches("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$")) {
                videoError.setText("L'URL de la vidéo n'est pas valide");
                videoError.setVisible(true);
            } else {
                videoError.setVisible(false);
            }
        });
    }

    private void validateDates() {
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            if (dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
                dateFinError.setText("La date de fin doit être après la date de début");
                dateFinError.setVisible(true);
            } else {
                dateFinError.setVisible(false);
            }
        }
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
        boolean isValid = true;
        
        // Titre validation
        if (titreField.getText() == null || titreField.getText().trim().isEmpty() || titreField.getText().length() < 3) {
            titreError.setVisible(true);
            isValid = false;
        }
        
        // Description validation
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty() || 
            descriptionArea.getText().length() < 10) {
            descriptionError.setVisible(true);
            isValid = false;
        }
        
        // Date début validation
        if (dateDebutPicker.getValue() == null || dateDebutPicker.getValue().isBefore(LocalDate.now())) {
            dateDebutError.setVisible(true);
            isValid = false;
        }
        
        // Date fin validation
        if (dateFinPicker.getValue() == null) {
            dateFinError.setVisible(true);
            isValid = false;
        }
        
        // Validation des dates
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null && 
            dateDebutPicker.getValue().isAfter(dateFinPicker.getValue())) {
            dateFinError.setVisible(true);
            isValid = false;
        }
        
        // Nombre de participants validation
        try {
            int nbrPart = Integer.parseInt(nbrpartField.getText());
            if (nbrPart <= 0) {
                nbrpartError.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            nbrpartError.setVisible(true);
            isValid = false;
        }
        
        // Prix validation
        try {
            double prix = Double.parseDouble(prixField.getText());
            if (prix < 0) {
                prixError.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            prixError.setVisible(true);
            isValid = false;
        }
        
        // Catégorie validation
        if (categorieCombo.getValue() == null) {
            categorieError.setVisible(true);
            isValid = false;
        }
        
        // URL vidéo validation (optionnel)
        if (videoField.getText() != null && !videoField.getText().trim().isEmpty() && 
            !videoField.getText().matches("^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$")) {
            videoError.setVisible(true);
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

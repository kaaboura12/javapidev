package Controllers.Article;

import Models.Galerie;
import Services.ServiceGalerie;
import Utils.MyDb;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class FrontAddGalerieController implements Initializable {
    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    @FXML
    private Label errorLabel;

    private ServiceGalerie serviceGalerie;
    private Galerie galerie;
    private boolean isEditMode = false;
    private FrontMonespaceController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialiser le service
            Connection connection = MyDb.getInstance().getConnection();
            if (connection == null) {
                showErrorInLabel("Impossible de se connecter à la base de données", true);
                return;
            }

            serviceGalerie = new ServiceGalerie(connection);

            // Configurer les écouteurs d'événements
            setupEventListeners();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorInLabel("Erreur lors de l'initialisation: " + e.getMessage(), true);
        }
    }

    private void setupEventListeners() {
        // Valider les champs en temps réel
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateFields();
        });

        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            validateFields();
        });
    }

    private boolean validateFields() {
        boolean isValid = !nomField.getText().trim().isEmpty() && 
                          !descriptionArea.getText().trim().isEmpty();
        
        saveBtn.setDisable(!isValid);
        return isValid;
    }

    public void setGalerie(Galerie galerie) {
        this.galerie = galerie;
        
        if (galerie != null) {
            nomField.setText(galerie.getNom());
            descriptionArea.setText(galerie.getDescription());
            validateFields();
        }
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        
        if (editMode) {
            saveBtn.setText("Modifier ma galerie");
        } else {
            saveBtn.setText("Créer ma galerie");
        }
    }

    public void setParentController(FrontMonespaceController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleSave() {
        if (validateFields()) {
            try {
                if (isEditMode && galerie != null) {
                    // Mise à jour de la galerie existante
                    galerie.setNom(nomField.getText());
                    galerie.setDescription(descriptionArea.getText());
                    // Mettre à jour la date de modification si nécessaire
                    serviceGalerie.update(galerie);
                    showErrorInLabel("Galerie modifiée avec succès", false);
                } else {
                    // Création d'une nouvelle galerie
                    Galerie newGalerie = new Galerie();
                    newGalerie.setNom(nomField.getText());
                    newGalerie.setDescription(descriptionArea.getText());
                    newGalerie.setDateCreation(LocalDateTime.now());
                    newGalerie.setUserId(15); // ID utilisateur fixe
                    serviceGalerie.insert(newGalerie);
                    showErrorInLabel("Galerie créée avec succès", false);
                }
                // Retourner à la vue principale via le parent
                if (parentController != null) {
                    parentController.reloadContentAndShowMainView();
                } else {
                    showErrorInLabel("Erreur : Contrôleur parent non défini.", true);
                }
            } catch (Exception e) {
                showErrorInLabel("Erreur lors de l'enregistrement: " + e.getMessage(), true);
            }
        } else {
             showErrorInLabel("Veuillez remplir tous les champs requis.", true);
        }
    }

    @FXML
    private void handleCancel() {
         // Retourner à la vue principale via le parent
        if (parentController != null) {
            parentController.reloadContentAndShowMainView();
        } else {
             showErrorInLabel("Erreur : Contrôleur parent non défini.", true);
        }
    }

    private void showErrorInLabel(String message, boolean isError) {
        errorLabel.setText(message);
        errorLabel.setStyle(isError ? "-fx-text-fill: #ff7e5f;" : "-fx-text-fill: #4CAF50;"); // Rouge pour erreur, Vert pour succès
        errorLabel.setVisible(true);
    }

    private StackPane findContentArea() {
        // Cette méthode est probablement obsolète maintenant
        return null;
    }
} 
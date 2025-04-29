package Controllers.Galerie;

import Models.Galerie;
import Services.ServiceGalerie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AddGalerieController implements Initializable {
    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField userIdField;
    
    private ServiceGalerie serviceGalerie;
    private GalerieRefreshListener refreshListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidatabase", "root", "");
            serviceGalerie = new ServiceGalerie(con);
        } catch (Exception e) {
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données", e);
        }
    }

    public void setRefreshListener(GalerieRefreshListener listener) {
        this.refreshListener = listener;
    }

    @FXML
    private void handleAjouter() {
        try {
            String nom = nomField.getText().trim();
            String description = descriptionField.getText().trim();
            String userIdStr = userIdField.getText().trim();

            if (nom.isEmpty()) {
                afficherErreur("Erreur", "Le nom est obligatoire", null);
                return;
            }

            if (userIdStr.isEmpty()) {
                afficherErreur("Erreur", "L'ID utilisateur est obligatoire", null);
                return;
            }

            try {
                int userId = Integer.parseInt(userIdStr);

                Galerie nouvelleGalerie = new Galerie();
                nouvelleGalerie.setNom(nom);
                nouvelleGalerie.setDescription(description);
                nouvelleGalerie.setUserId(userId);
                nouvelleGalerie.setDateCreation(LocalDateTime.now());

                serviceGalerie.insert(nouvelleGalerie);

                if (refreshListener != null) {
                    refreshListener.onGalerieAdded();
                }

                loadInContentArea("/Views/Galerie/ShowGalerie.fxml");

            } catch (NumberFormatException e) {
                afficherErreur("Erreur", "L'ID utilisateur doit être un nombre", null);
            }

        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'ajouter la galerie", e);
        }
    }


    @FXML
    private void handleAnnuler() {
        try {
            loadInContentArea("/Views/Galerie/ShowGalerie.fxml");
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de retourner à la page précédente", e);
        }
    }


    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle
            Scene currentScene = nomField.getScene();
            if (currentScene != null) {
                // Remplacer le contenu de la scène
                currentScene.setRoot(root);
            }
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de charger la page", e);
        }
    }

    private void afficherErreur(String titre, String message, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(message);
        if (e != null) {
            alert.setContentText(e.getMessage());
        }
        alert.showAndWait();
    }
    private void loadInContentArea(String fxmlPath) {
        try {
            AnchorPane contentArea = findContentArea(nomField); // ou un autre champ existant
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);

                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                System.err.println("contentArea introuvable");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de : " + fxmlPath);
        }
    }

    private AnchorPane findContentArea(Node node) {
        if (node == null) return null;
        Scene scene = node.getScene();
        if (scene != null) {
            return (AnchorPane) scene.lookup("#contentArea");
        }
        return null;
    }

} 
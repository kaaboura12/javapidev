package Controllers.Galerie;

import Models.Galerie;
import Services.ServiceGalerie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import Controllers.Basebackcontroller;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GalerieController implements GalerieRefreshListener {
    @FXML
    private TableView<Galerie> galeriesTableView;
    @FXML
    private TableColumn<Galerie, Integer> idColumn;
    @FXML
    private TableColumn<Galerie, String> nomColumn;
    @FXML
    private TableColumn<Galerie, String> descriptionColumn;
    @FXML
    private TableColumn<Galerie, LocalDateTime> dateCreationColumn;
    @FXML
    private TableColumn<Galerie, Integer> userIdColumn;
    @FXML
    private TableColumn<Galerie, Integer> nbrArticlesColumn;
    
    @FXML
    private TextField nomField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField userIdField;
    @FXML
    private TextField dateCreationField;
    @FXML
    private TextField nbrArticlesField;

    @FXML
    private Button newGalerieBtn;
    
    @FXML
    private Button articlesBtn;

    @FXML
    private TextField searchField;

    private ServiceGalerie serviceGalerie;
    private Galerie galerieSelectionnee;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private ObservableList<Galerie> allGaleries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidatabase", "root", "");
            serviceGalerie = new ServiceGalerie(con);
            
            // Configuration des colonnes
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
            dateCreationColumn.setCellFactory(column -> new TableCell<Galerie, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DATE_FORMATTER));
                    }
                }
            });
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
            nbrArticlesColumn.setCellValueFactory(new PropertyValueFactory<>("nombreTotalArticles"));
            
            // Charger les données
            List<Galerie> galeries = serviceGalerie.findAll();
            allGaleries.setAll(galeries);
            afficherGaleries(allGaleries);
            
            // Ajouter un listener pour la sélection
            galeriesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    galerieSelectionnee = newSelection;
                    afficherDetailsGalerie(newSelection);
                }
            });
            
            // Définir explicitement le texte et le style des boutons
            if (newGalerieBtn != null) {
                newGalerieBtn.setText("Nouvelle Galerie");
                newGalerieBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
            }
            
            if (articlesBtn != null) {
                articlesBtn.setText("Liste des Articles");
                articlesBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
            }
            
            // Recherche dynamique
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterGaleries(newValue);
                });
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données", e);
        }
    }
    
    private void afficherGaleries(List<Galerie> galeries) {
        allGaleries.setAll(galeries);
        galeriesTableView.setItems(allGaleries);
    }
    
    private void afficherGaleries(ObservableList<Galerie> galeries) {
        galeriesTableView.setItems(galeries);
    }
    
    private void afficherDetailsGalerie(Galerie galerie) {
        nomField.setText(galerie.getNom());
        descriptionField.setText(galerie.getDescription());
        userIdField.setText(galerie.getUserId() != null ? String.valueOf(galerie.getUserId()) : "");
        dateCreationField.setText(galerie.getDateCreation() != null ? 
                                 galerie.getDateCreation().format(DATE_FORMATTER) : "");
        nbrArticlesField.setText(String.valueOf(galerie.getNombreTotalArticles()));
    }
    
    @FXML
    private void handleNouvelleGalerie() {
        try {
            AnchorPane contentArea = findContentArea(); // ↙ Fonction ci-dessous
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Galerie/AddGalerie.fxml"));
                Parent root = loader.load();

                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);

                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleModifier() {
        try {
            if (galerieSelectionnee == null) {
                afficherErreur("Erreur", "Veuillez sélectionner une galerie à modifier", null);
                return;
            }
            
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
                
                galerieSelectionnee.setNom(nom);
                galerieSelectionnee.setDescription(description);
                galerieSelectionnee.setUserId(userId);
                
                serviceGalerie.update(galerieSelectionnee);
                
                // Rafraîchir la liste
                List<Galerie> galeries = serviceGalerie.findAll();
                afficherGaleries(galeries);
                
            } catch (NumberFormatException e) {
                afficherErreur("Erreur", "L'ID utilisateur doit être un nombre", null);
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de modifier la galerie", e);
        }
    }
    
    @FXML
    private void handleSupprimer() {
        try {
            if (galerieSelectionnee == null) {
                afficherErreur("Erreur", "Veuillez sélectionner une galerie à supprimer", null);
                return;
            }
            
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer la galerie");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette galerie ?");
            
            if (confirmation.showAndWait().get() == ButtonType.OK) {
                serviceGalerie.delete(galerieSelectionnee);
                
                // Rafraîchir la liste
                List<Galerie> galeries = serviceGalerie.findAll();
                afficherGaleries(galeries);
                
                // Réinitialiser les champs
                nomField.clear();
                descriptionField.clear();
                userIdField.clear();
                dateCreationField.clear();
                nbrArticlesField.clear();
                galerieSelectionnee = null;
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de supprimer la galerie", e);
        }
    }
    //AJOUTER POUR LA NAVIGATION
    @FXML
    private void handleListeArticles() {
        try {
            AnchorPane contentArea = findContentArea(); // ↙ Fonction ci-dessous
            if (contentArea != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/ShowArticle.fxml"));
                Parent root = loader.load();

                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);

                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle
            Scene currentScene = galeriesTableView.getScene();
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
    
    @Override
    public void onGalerieAdded() {
        try {
            List<Galerie> galeries = serviceGalerie.findAll();
            afficherGaleries(galeries);
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de rafraîchir la liste", e);
        }
    }
    //AJOUTER POUR LA NAVIGATION
    private AnchorPane findContentArea() {
        if (nomField == null) return null; // Ou un autre composant FXML de ta vue
        Scene scene = nomField.getScene();
        if (scene != null) {
            return (AnchorPane) scene.lookup("#contentArea");
        }
        return null;
    }

    private void filterGaleries(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            galeriesTableView.setItems(allGaleries);
        } else {
            String lower = searchText.toLowerCase();
            ObservableList<Galerie> filtered = allGaleries.filtered(g ->
                (g.getNom() != null && g.getNom().toLowerCase().contains(lower)) ||
                (g.getDescription() != null && g.getDescription().toLowerCase().contains(lower))
            );
            galeriesTableView.setItems(filtered);
        }
    }

}


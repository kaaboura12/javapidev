package Controllers.Article;

import Models.Galerie;
import Models.Article;
import Services.ServiceGalerie;
import Services.ServiceArticle;
import Utils.MyDb;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontMonespaceController implements Initializable {
    @FXML
    private VBox galerieDetailsView;
    @FXML
    private VBox noGalerieView;
    @FXML
    private Label galerieNameLabel;
    @FXML
    private Label galerieDescriptionLabel;
    @FXML
    private Label galerieDateLabel;
    @FXML
    private GridPane articlesGrid;
    @FXML
    private Label errorLabel;
    @FXML
    private Button editGalerieBtn;
    @FXML
    private Button deleteGalerieBtn;
    @FXML
    private Button createGalerieBtn;
    @FXML
    private Button addArticleBtn;
    @FXML
    private Button showOrdersBtn;
    @FXML
    private StackPane contentArea;

    private ServiceGalerie serviceGalerie;
    private ServiceArticle serviceArticle;
    private Galerie userGalerie;
    private List<Article> userArticles;
    private static final int COLUMNS = 3;
    private static final int USER_ID = 15; // ID de l'utilisateur connecté

    private Node mainContentNode; // Stocke la vue principale (ScrollPane)

    private static final String HEART_FILLED = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z";
    private static final String HEART_OUTLINE = "M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialiser les services
            Connection connection = MyDb.getInstance().getConnection();
            if (connection == null) {
                showErrorInLabel("Impossible de se connecter à la base de données");
                return;
            }

            serviceGalerie = new ServiceGalerie(connection);
            serviceArticle = new ServiceArticle(connection);

            // Stocker le contenu principal initial (le ScrollPane)
            if (!contentArea.getChildren().isEmpty()) {
                mainContentNode = contentArea.getChildren().get(0);
            } else {
                System.err.println("❌ Erreur: contentArea est vide initialement !");
            }

            // Charger la galerie de l'utilisateur
            loadUserGalerie();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorInLabel("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadUserGalerie() {
        try {
            // Récupérer les galeries de l'utilisateur
            List<Galerie> galeries = serviceGalerie.findByUserId(USER_ID);
            
            if (galeries != null && !galeries.isEmpty()) {
                // L'utilisateur a au moins une galerie, on prend la première
                userGalerie = galeries.get(0);
                
                // Afficher les détails de la galerie
                displayGalerieDetails();
                
                // Charger les articles de la galerie
                loadUserArticles();
                
                // Afficher la vue des détails et activer les boutons
                galerieDetailsView.setVisible(true);
                noGalerieView.setVisible(false);
                showOrdersBtn.setVisible(true);
                editGalerieBtn.setVisible(true);
                deleteGalerieBtn.setVisible(true);
                addArticleBtn.setVisible(true);
            } else {
                // L'utilisateur n'a pas de galerie
                galerieDetailsView.setVisible(false);
                noGalerieView.setVisible(true);
                showOrdersBtn.setVisible(false);
                editGalerieBtn.setVisible(false);
                deleteGalerieBtn.setVisible(false);
                addArticleBtn.setVisible(false);
            }
            // Assurez-vous que la vue principale est affichée
            if (mainContentNode != null && !contentArea.getChildren().contains(mainContentNode)) {
                 contentArea.getChildren().setAll(mainContentNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorInLabel("Erreur lors du chargement de la galerie: " + e.getMessage());
        }
    }

    private void reloadContent() {
        try {
            // Recharger uniquement le contenu nécessaire
            if (userGalerie != null) {
                displayGalerieDetails();
                loadUserArticles();
            } else {
                loadUserGalerie();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorInLabel("Erreur lors du rechargement du contenu: " + e.getMessage());
        }
    }

    private void displayGalerieDetails() {
        if (userGalerie != null) {
            galerieNameLabel.setText(userGalerie.getNom());
            galerieDescriptionLabel.setText(userGalerie.getDescription());
            
            // Formater la date de création
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            galerieDateLabel.setText(userGalerie.getDateCreation().format(formatter));
        }
    }

    private void loadUserArticles() {
        try {
            // Récupérer tous les articles
            List<Article> allArticles = serviceArticle.findAll();
            
            if (allArticles != null) {
                // Filtrer les articles de la galerie de l'utilisateur
                userArticles = allArticles.stream()
                        .filter(article -> article.getGalerieId() != null && 
                                         article.getGalerieId() == userGalerie.getId())
                        .collect(Collectors.toList());
                
                // Afficher les articles
                displayArticles();
            } else {
                userArticles = new ArrayList<>();
                showErrorInLabel("Aucun article disponible");
                displayArticles(); // Afficher une grille vide
            }
        } catch (Exception e) {
            e.printStackTrace();
            userArticles = new ArrayList<>();
            showErrorInLabel("Erreur lors du chargement des articles: " + e.getMessage());
            displayArticles(); // Afficher une grille vide
        }
    }

    private void displayArticles() {
        articlesGrid.getChildren().clear();

        if (userArticles == null || userArticles.isEmpty()) {
            // Afficher un message si aucun article n'est disponible
            Label noArticlesLabel = new Label("Aucun article dans votre galerie");
            noArticlesLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 16px;");
            articlesGrid.add(noArticlesLabel, 0, 0);
            return;
        }

        int row = 0;
        int col = 0;

        for (Article article : userArticles) {
            VBox articleCard = createArticleCard(article);
            articlesGrid.add(articleCard, col, row);

            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createArticleCard(Article article) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 15; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(10, Color.BLACK));

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        try {
            if (article.getContenu() != null && !article.getContenu().isEmpty()) {
                String imagePath = article.getContenu();
                if (!imagePath.startsWith("src/main/resources/UploadsGalerie/")) {
                    imagePath = "src/main/resources/UploadsGalerie/" + imagePath;
                }
                InputStream input = getClass().getResourceAsStream("/" + imagePath);
                if (input != null) {
                    imageView.setImage(new Image(input));
                } else {
                    imageView.setImage(new Image("file:" + imagePath));
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
        }

        // Titre
        Label titleLabel = new Label(article.getTitre());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        // Prix et Likes dans un HBox
        HBox infoBox = new HBox(15);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(String.format("%.2f DT", article.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #ff7e5f; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Conteneur pour l'icône de like et le compteur
        HBox likeBox = new HBox(5);
        likeBox.setAlignment(Pos.CENTER);
        likeBox.setStyle("-fx-background-color: rgba(255,126,95,0.1); -fx-padding: 5 10; -fx-background-radius: 15;");

        // Icône de cœur SVG
        Node heartIcon = createHeartIcon(true);

        // Compteur de likes
        Label likeCount = new Label(String.valueOf(article.getNbrLikes()));
        likeCount.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        likeBox.getChildren().addAll(heartIcon, likeCount);
        infoBox.getChildren().addAll(priceLabel, likeBox);

        // Boutons d'action
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 8 15; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        editBtn.setOnAction(e -> handleEditArticle(article));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); " +
                         "-fx-text-fill: white; " +
                         "-fx-font-weight: bold; " +
                         "-fx-background-radius: 5; " +
                         "-fx-padding: 8 15;");
        deleteBtn.setOnAction(e -> handleDeleteArticle(article));

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(imageView, titleLabel, infoBox, buttonsBox);
        return card;
    }

    private Node createHeartIcon(boolean filled) {
        Region heart = new Region();
        heart.setStyle(String.format(
            "-fx-shape: \"%s\"; -fx-background-color: %s; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20;",
            filled ? HEART_FILLED : HEART_OUTLINE,
            "#ff7e5f"
        ));
        return heart;
    }

    @FXML
    private void handleAddArticle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontAddArticle.fxml"));
            Parent root = loader.load();
            FrontAddArticleController controller = loader.getController();
            controller.setParentController(this); // Passer la référence
            controller.setGalerieId(userGalerie.getId());
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showErrorInLabel("Erreur lors du chargement de la page d'ajout d'article: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateGalerie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontAddGalerie.fxml"));
            Parent root = loader.load();
            FrontAddGalerieController controller = loader.getController();
            controller.setParentController(this); // Passer la référence
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showErrorInLabel("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditGalerie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontAddGalerie.fxml"));
            Parent root = loader.load();
            FrontAddGalerieController controller = loader.getController();
            controller.setParentController(this); // Passer la référence
            controller.setGalerie(userGalerie);
            controller.setEditMode(true);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showErrorInLabel("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteGalerie() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ma galerie");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer votre galerie ? Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceGalerie.delete(userGalerie);
                    userGalerie = null;
                    reloadContentAndShowMainView();
                    showErrorInLabel("Galerie supprimée avec succès");
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorInLabel("Erreur lors de la suppression de la galerie: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleEditArticle(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontEditArticle.fxml"));
            Parent root = loader.load();
            FrontEditArticleController controller = loader.getController();
            controller.setParentController(this); // Passer la référence
            controller.setArticle(article);
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            showErrorInLabel("Erreur lors du chargement de la page de modification d'article: " + e.getMessage());
        }
    }

    private void handleDeleteArticle(Article article) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'article");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet article ? Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    serviceArticle.delete(article);
                    reloadContentAndShowMainView();
                    showErrorInLabel("Article supprimé avec succès");
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorInLabel("Erreur lors de la suppression de l'article: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleShowOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontShowCommandes.fxml"));
            Parent root = loader.load();
            
            FrontShowCommandesController controller = loader.getController();
            controller.setContentArea(contentArea);
            
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                showErrorInLabel("Erreur: Zone de contenu non trouvée");
            }
        } catch (Exception e) {
            showErrorInLabel("Erreur lors du chargement de la page des commandes: " + e.getMessage());
        }
    }

    private void showErrorInLabel(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #ff7e5f; -fx-font-weight: bold;");
            errorLabel.setVisible(true);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }
    }

    public void reloadContentAndShowMainView() {
        try {
            // Recharger les données
            loadUserGalerie(); // Recharge la galerie et les articles si nécessaire

            // Restaurer la vue principale dans contentArea
            if (mainContentNode != null) {
                contentArea.getChildren().setAll(mainContentNode);
                // S'assurer que les vues internes sont correctement mises à jour
                if (userGalerie != null) {
                    galerieDetailsView.setVisible(true);
                    noGalerieView.setVisible(false);
                } else {
                    galerieDetailsView.setVisible(false);
                    noGalerieView.setVisible(true);
                }
            } else {
                 showErrorInLabel("Erreur : Impossible de restaurer la vue principale.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorInLabel("Erreur lors du rechargement du contenu: " + e.getMessage());
        }
    }
} 
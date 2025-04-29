package Controllers.Article;

import Models.Article;
import Models.Galerie;
import Services.ServiceArticle;
import Services.ServiceGalerie;
import Services.ServiceArtLike;
import Utils.MyDb;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.layout.Priority;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontArticleController implements Initializable {
    @FXML
    private GridPane articlesGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categorieFilter;
    @FXML
    private ComboBox<String> triFilter;
    @FXML
    private Label errorLabel;

    @FXML
    private Button monEspaceBtn;

    private List<Article> allArticlesLoaded; // Garde tous les articles originaux
    private List<Article> displayedArticles; // Articles à afficher après filtrage/tri
    private ServiceArticle serviceArticle;
    private ServiceArtLike serviceArtLike;
    private static final int COLUMNS = 4;
    private static final int CURRENT_USER_ID = 15;

    private static final String HEART_FILLED = "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z";
    private static final String HEART_OUTLINE = "M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialiser le service avec la connexion à la base de données
            Connection connection = MyDb.getInstance().getConnection();
            if (connection == null) {
                showError("Impossible de se connecter à la base de données");
                return;
            }

            // Vérifier si la connexion est valide
            if (connection.isClosed()) {
                showError("La connexion à la base de données est fermée");
                return;
            }

            // Afficher les informations de la base de données
            System.out.println("Connexion à la base de données établie");
            System.out.println("URL: " + connection.getMetaData().getURL());
            System.out.println("Utilisateur: " + connection.getMetaData().getUserName());

            serviceArticle = new ServiceArticle(connection);
            serviceArtLike = new ServiceArtLike(connection);

            // Initialiser les filtres
            initializeFilters();

            // Charger les articles
            loadArticles();

            // Configurer les écouteurs d'événements
            setupEventListeners();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void initializeFilters() {
        // Options de tri
        triFilter.getItems().addAll(
                "Prix croissant",
                "Prix décroissant",
                "Date récente"
        );

        try {
            // Charger les catégories uniques depuis la base de données
            List<Article> allArticles = serviceArticle.findAll();
            System.out.println("Nombre d'articles trouvés: " + (allArticles != null ? allArticles.size() : 0));

            if (allArticles != null) {
                List<String> categories = allArticles.stream()
                        .map(Article::getCategorie)
                        .filter(cat -> cat != null && !cat.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                System.out.println("Catégories trouvées: " + categories);

                categorieFilter.getItems().add("Toutes");
                categorieFilter.getItems().addAll(categories);
                categorieFilter.setValue("Toutes");
            } else {
                showError("Impossible de charger les catégories: données non disponibles");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    private void loadArticles() {
        try {
            allArticlesLoaded = serviceArticle.findAll(); // Charger dans la liste originale
            if (allArticlesLoaded == null) { // Vérifier si null
                allArticlesLoaded = new ArrayList<>();
            }
            displayedArticles = new ArrayList<>(allArticlesLoaded); // Initialiser la liste affichée
            System.out.println("Nombre d'articles chargés: " + allArticlesLoaded.size());

            if (!displayedArticles.isEmpty()) {
                displayArticles(); // Afficher la liste initiale
            } else {
                showError("Aucun article disponible");
                displayArticles(); // Afficher une grille vide
            }
        } catch (Exception e) {
            e.printStackTrace();
            allArticlesLoaded = new ArrayList<>();
            displayedArticles = new ArrayList<>();
            showError("Erreur lors du chargement des articles: " + e.getMessage());
            displayArticles(); // Afficher une grille vide
        }
    }

    private void displayArticles() {
        articlesGrid.getChildren().clear();

        // Utiliser displayedArticles pour l'affichage
        if (displayedArticles == null || displayedArticles.isEmpty()) {
            Label noArticlesLabel = new Label("Aucun article disponible ou correspondant aux filtres.");
            noArticlesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
            articlesGrid.add(noArticlesLabel, 0, 0, COLUMNS, 1); // Span across columns
            GridPane.setHalignment(noArticlesLabel, javafx.geometry.HPos.CENTER);
            return;
        }

        int row = 0;
        int col = 0;

        for (Article article : displayedArticles) { // Itérer sur displayedArticles
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
        VBox card = new VBox(8);
        card.getStyleClass().add("article-card");
        card.setPrefWidth(240);
        card.setMinWidth(240);
        card.setMaxWidth(240);

        // Container pour l'image et les badges
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-radius: 8 8 0 0; -fx-background-color: #2d2d2d;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(false);
        imageView.setStyle("-fx-background-radius: 8 8 0 0;");

        try {
            if (article.getContenu() != null && !article.getContenu().isEmpty()) {
                String imagePath = article.getContenu();
                if (!imagePath.startsWith("src/main/resources/UploadsGalerie/")) {
                    imagePath = "src/main/resources/UploadsGalerie/" + imagePath;
                }
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    System.err.println("Image non trouvée: " + imagePath);
                    loadDefaultImage(imageView);
                }
            } else {
                loadDefaultImage(imageView);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement image (" + article.getId() + "): " + e.getMessage());
            loadDefaultImage(imageView);
        }

        // Ajouter les badges
        HBox badgesContainer = new HBox(5);
        badgesContainer.setAlignment(Pos.TOP_RIGHT);
        badgesContainer.setPadding(new Insets(8));
        StackPane.setAlignment(badgesContainer, Pos.TOP_RIGHT);

        // Badge "Nouveau" (pour les articles de moins de 7 jours)
        if (article.getDatePub() != null) {
            java.time.LocalDate articleDate = article.getDatePub();
            java.time.LocalDate sevenDaysAgo = java.time.LocalDate.now().minusDays(7);
            if (articleDate.isAfter(sevenDaysAgo)) {
                Label newBadge = new Label("NOUVEAU");
                newBadge.getStyleClass().addAll("badge", "new-badge");
                badgesContainer.getChildren().add(newBadge);
            }
        }

        // Badge "Populaire" (pour les articles avec plus de 10 likes)
        if (article.getNbrLikes() >= 5) {
            Label popularBadge = new Label("POPULAIRE");
            popularBadge.getStyleClass().addAll("badge", "popular-badge");
            badgesContainer.getChildren().add(popularBadge);
        }

        imageContainer.getChildren().addAll(imageView, badgesContainer);

        Label title = new Label(article.getTitre());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setPadding(new Insets(8, 10, 0, 10));

        // Prix et stock dans un HBox
        HBox infoContainer = new HBox(10);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.setPadding(new Insets(2, 10, 5, 10));

        Label price = new Label(String.format("%.2f DT", article.getPrix()));
        price.setStyle("-fx-text-fill: #ff7e5f; -fx-font-size: 15px; -fx-font-weight: bold;");

        // Indicateur de stock
        Label stockLabel = new Label();
        stockLabel.getStyleClass().addAll("stock-badge");
        
        if (article.getNbrArticle() > 10) {
            stockLabel.setText("En stock");
            stockLabel.getStyleClass().add("in-stock");
        } else if (article.getNbrArticle() > 0) {
            stockLabel.setText("Stock limité");
            stockLabel.getStyleClass().add("low-stock");
        } else {
            stockLabel.setText("Épuisé");
            stockLabel.getStyleClass().add("out-of-stock");
        }

        infoContainer.getChildren().addAll(price, stockLabel);

        HBox actionBar = new HBox();
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(0, 10, 10, 10));
        actionBar.setSpacing(8);

        Button detailsButton = new Button("Détails");
        detailsButton.getStyleClass().add("details-button");
        detailsButton.setOnAction(e -> showArticleDetails(article));

        HBox likeBox = new HBox(4);
        likeBox.setAlignment(Pos.CENTER_LEFT);
        likeBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 15;");
        likeBox.setPadding(new Insets(3, 8, 3, 8));

        Button likeButton = new Button();
        likeButton.setStyle("-fx-background-color: transparent; -fx-padding: 2 5 2 5; -fx-cursor: hand;");

        Label likeCountLabel = new Label();
        likeCountLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px; -fx-font-weight: bold;");
        likeCountLabel.setMinWidth(Region.USE_PREF_SIZE);

        Runnable updateLikeUI = () -> {
            try {
                boolean isLiked = serviceArtLike.isLiked(CURRENT_USER_ID, article.getId());
                int likeCount = serviceArtLike.getLikeCount(article.getId());

                likeCountLabel.setText(String.valueOf(likeCount));
                Node heartIcon = createHeartIcon(isLiked);
                likeButton.setGraphic(heartIcon);
                
                if (isLiked) {
                    likeBox.setStyle("-fx-background-color: rgba(255,126,95,0.1); -fx-background-radius: 15;");
                    likeButton.setTooltip(new Tooltip("Ne plus aimer"));
                } else {
                    likeBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 15;");
                    likeButton.setTooltip(new Tooltip("Aimer"));
                }

                // Ajouter un effet hover
                likeButton.setOnMouseEntered(e -> {
                    heartIcon.setStyle(String.format(
                        "-fx-shape: \"%s\"; -fx-background-color: %s; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20; -fx-scale-x: 1.1; -fx-scale-y: 1.1;",
                        isLiked ? HEART_FILLED : HEART_OUTLINE,
                        "#ff7e5f"
                    ));
                });
                
                likeButton.setOnMouseExited(e -> {
                    heartIcon.setStyle(String.format(
                        "-fx-shape: \"%s\"; -fx-background-color: %s; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20;",
                        isLiked ? HEART_FILLED : HEART_OUTLINE,
                        isLiked ? "#ff7e5f" : "white"
                    ));
                });

            } catch (SQLException ex) {
                Platform.runLater(() -> showError("Erreur MàJ like: " + ex.getMessage()));
            }
        };

        likeButton.setOnAction(e -> {
            try {
                boolean isCurrentlyLiked = serviceArtLike.isLiked(CURRENT_USER_ID, article.getId());
                if (isCurrentlyLiked) {
                    serviceArtLike.removeLike(CURRENT_USER_ID, article.getId());
                    serviceArticle.decrementLikeCount(article.getId());
                } else {
                    serviceArtLike.addLike(CURRENT_USER_ID, article.getId());
                    serviceArticle.incrementLikeCount(article.getId());
                }
                updateLikeUI.run();
            } catch (SQLException ex) {
                Platform.runLater(() -> showError("Erreur like/unlike: " + ex.getMessage()));
                ex.printStackTrace();
            }
        });

        updateLikeUI.run();

        likeBox.getChildren().addAll(likeButton, likeCountLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionBar.getChildren().addAll(detailsButton, spacer, likeBox);

        card.getChildren().addAll(imageContainer, title, infoContainer, actionBar);
        return card;
    }

    private void setupEventListeners() {
        // Recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFiltersAndSort();
        });

        // Filtres
        categorieFilter.setOnAction(e -> {
            applyFiltersAndSort();
        });

        triFilter.setOnAction(e -> {
            applyFiltersAndSort();
        });
    }

    private void applyFiltersAndSort() {
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        String selectedCategory = categorieFilter.getValue();
        String selectedSort = triFilter.getValue();

        try {
            // Toujours partir de la liste complète originale
            displayedArticles = allArticlesLoaded.stream()
                    .filter(article -> {
                        boolean categoryMatch = (selectedCategory == null || selectedCategory.equals("Toutes") || (article.getCategorie() != null && article.getCategorie().equals(selectedCategory)));
                        boolean searchMatch = (searchText.isEmpty() ||
                                (article.getTitre() != null && article.getTitre().toLowerCase().contains(searchText)) ||
                                (article.getDescription() != null && article.getDescription().toLowerCase().contains(searchText)));
                        return categoryMatch && searchMatch;
                    })
                    .sorted((a1, a2) -> { // Appliquer le tri
                        if (selectedSort == null) return 0;
                        switch (selectedSort) {
                            case "Prix croissant":
                                return Double.compare(a1.getPrix(), a2.getPrix());
                            case "Prix décroissant":
                                return Double.compare(a2.getPrix(), a1.getPrix());
                            case "Date récente":
                            default:
                                // Gérer les dates nulles potentiellement
                                if (a1.getDatePub() == null && a2.getDatePub() == null) return 0;
                                if (a1.getDatePub() == null) return 1; // nulls last
                                if (a2.getDatePub() == null) return -1; // nulls last
                                return a2.getDatePub().compareTo(a1.getDatePub());
                        }
                    })
                    .collect(Collectors.toList()); // Collecter dans displayedArticles

            displayArticles(); // Afficher la liste résultante
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du filtrage et tri des articles: " + e.getMessage());
        }
    }

    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent root = loader.load();
            Scene scene = articlesGrid.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page: " + e.getMessage());
        }
    }

    private void showArticleDetails(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/ArticleDetails.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer l'article
            ArticleDetailsController controller = loader.getController();
            controller.setArticle(article);

            // Rechercher dynamiquement le contentArea (StackPane)
            StackPane contentArea = findContentArea(articlesGrid);

            if (contentArea != null) {
                StackPane.setMargin(root, Insets.EMPTY); // Supprimer les marges si nécessaire
                contentArea.getChildren().setAll(root);
            } else {
                System.err.println("❌ contentArea introuvable !");
                showError("Erreur : contentArea introuvable !");
            }
        } catch (IOException e) {
            showError("Erreur lors de l'affichage des détails : " + e.getMessage());
        }
    }

    @FXML
    private void handleMonEspace() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontShowMonespace.fxml"));
            Parent root = loader.load();

            // Trouver dynamiquement le StackPane contentArea
            StackPane contentArea = findContentArea(monEspaceBtn);

            if (contentArea != null) {
                StackPane.setMargin(root, new Insets(0));
                contentArea.getChildren().setAll(root);
            } else {
                System.err.println("❌ contentArea introuvable !");
                showError("Erreur : contentArea introuvable !");
            }
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de mon espace: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            errorLabel.setVisible(true);
        } else {
            // Si le label n'est pas disponible, utiliser Platform.runLater pour afficher une alerte
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }
    }

    private void loadDefaultImage(ImageView imageView) {
        InputStream input = getClass().getResourceAsStream("/images/default-product.png");
        if (input != null) {
            imageView.setImage(new Image(input));
        } else {
            System.err.println("⚠️ Image par défaut introuvable à /images/default-product.png");
            imageView.setImage(null); // ou mets une couleur, ou une forme
        }
    }

    private StackPane findContentArea(Node node) {
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof StackPane && "contentArea".equals(parent.getId())) {
                return (StackPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private Node createHeartIcon(boolean filled) {
        Region heart = new Region();
        heart.setStyle(String.format(
            "-fx-shape: \"%s\"; -fx-background-color: %s; -fx-min-width: 20; -fx-min-height: 20; -fx-max-width: 20; -fx-max-height: 20;",
            filled ? HEART_FILLED : HEART_OUTLINE,
            filled ? "#ff7e5f" : "white"
        ));
        return heart;
    }
}
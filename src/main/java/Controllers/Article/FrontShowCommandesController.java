package Controllers.Article;

import Models.Article;
import Models.Commande;
import Services.ServiceCommande;
import Services.ServiceArticle;
import Utils.MyDb;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FrontShowCommandesController implements Initializable {
    @FXML private VBox ordersContainer;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalProductsLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private StackPane contentArea;

    private static final int USER_ID = 15; // ID statique de l'utilisateur
    private ServiceCommande serviceCommande;
    private ServiceArticle serviceArticle;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private ObservableList<Commande> allOrders;
    private FilteredList<Commande> filteredOrders;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialiser la connexion et les services
            Connection connection = MyDb.getInstance().getConnection();
            serviceCommande = new ServiceCommande(connection);
            serviceArticle = new ServiceArticle(connection);

            // Configuration du ComboBox de filtrage
            filterComboBox.setItems(FXCollections.observableArrayList(
                "Toutes les commandes",
                "Aujourd'hui",
                "Cette semaine",
                "Ce mois"
            ));
            filterComboBox.getSelectionModel().selectFirst();

            // Charger les données
            loadOrders();

            // Configuration des listeners pour la recherche et le filtrage
            setupSearchAndFilter();

        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    private void loadOrders() {
        try {
            // Récupération des commandes
            List<Commande> orders = serviceCommande.getCommandesByUserId(USER_ID);
            allOrders = FXCollections.observableArrayList(orders);
            filteredOrders = new FilteredList<>(allOrders);

            // Mise à jour des statistiques
            updateStatistics();

            // Affichage des commandes
            displayOrders();

        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les commandes: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        double totalSales = filteredOrders.stream()
            .mapToDouble(commande -> {
                try {
                    Article article = serviceArticle.findById(commande.getArticleId());
                    return commande.getQuantite() * article.getPrix();
                } catch (SQLException e) {
                    return 0.0;
                }
            })
            .sum();
        int totalProducts = filteredOrders.stream()
            .mapToInt(Commande::getQuantite)
            .sum();

        totalSalesLabel.setText(String.format("%.2f Dt", totalSales));
        totalProductsLabel.setText(String.valueOf(totalProducts));
    }

    private void setupSearchAndFilter() {
        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOrders.setPredicate(commande -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                try {
                    String articleTitle = serviceArticle.findById(commande.getArticleId()).getTitre();
                    return articleTitle.toLowerCase().contains(lowerCaseFilter);
                } catch (Exception e) {
                    return false;
                }
            });
            displayOrders();
            updateStatistics();
        });

        // Configuration du filtre par date
        filterComboBox.setOnAction(e -> {
            String filter = filterComboBox.getValue();
            filteredOrders.setPredicate(commande -> {
                if (filter == null || filter.equals("Toutes les commandes")) {
                    return true;
                }
                // Ajoutez ici la logique de filtrage par date selon vos besoins
                return true;
            });
            displayOrders();
            updateStatistics();
        });
    }

    private void displayOrders() {
        ordersContainer.getChildren().clear();

        if (filteredOrders.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Commande commande : filteredOrders) {
            ordersContainer.getChildren().add(createOrderCard(commande));
        }
    }

    private Node createOrderCard(Commande commande) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #2d2d45; -fx-padding: 15; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));

        try {
            // En-tête de la carte
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);

            Label orderNumber = new Label("Commande #" + commande.getId());
            orderNumber.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff7e5f;");

            Label date = new Label(commande.getDateCommande().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            date.setStyle("-fx-text-fill: #e0e0e0;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label status = new Label("Confirmée");
            status.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");

            header.getChildren().addAll(orderNumber, date, spacer, status);

            // Détails de l'article
            VBox details = new VBox(5);
            details.setStyle("-fx-padding: 10 0;");

            Article article = serviceArticle.findById(commande.getArticleId());
            String articleTitle = article.getTitre();
            Label articleName = new Label(articleTitle);
            articleName.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

            Label quantity = new Label("Quantité: " + commande.getQuantite());
            quantity.setStyle("-fx-text-fill: #e0e0e0;");

            Label price = new Label(String.format("Prix unitaire: %.2f Dt", article.getPrix()));
            price.setStyle("-fx-text-fill: #e0e0e0;");

            Label total = new Label(String.format("Total: %.2f Dt", commande.getQuantite() * article.getPrix()));
            total.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ff7e5f;");

            details.getChildren().addAll(articleName, quantity, price, total);

            // Ajout de tous les éléments à la carte
            card.getChildren().addAll(header, new Separator(), details);

        } catch (Exception e) {
            Label errorLabel = new Label("Erreur de chargement des détails");
            errorLabel.setStyle("-fx-text-fill: #ff4444;");
            card.getChildren().add(errorLabel);
        }

        return card;
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-padding: 50;");

        Label message = new Label("Aucune commande trouvée");
        message.setStyle("-fx-font-size: 18px; -fx-text-fill: #e0e0e0;");

        Label subMessage = new Label("Les commandes de vos articles apparaîtront ici");
        subMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #8d8d8d;");

        emptyState.getChildren().addAll(message, subMessage);
        ordersContainer.getChildren().add(emptyState);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontShowMonespace.fxml"));
            Parent root = loader.load();
            
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } else {
                showError("Erreur de navigation", "Impossible de retourner à mon espace");
            }
        } catch (Exception e) {
            showError("Erreur de navigation", "Impossible de charger la page mon espace: " + e.getMessage());
        }
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }
} 
package Controllers.Article;

import Models.Article;
import Models.Commande;
import Services.ServiceArticle;
import Services.ServiceCommande;
import Utils.MyDb;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

public class ArticleDetailsController implements Initializable {
    @FXML
    private ImageView articleImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Text descriptionText;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label likesLabel;
    @FXML
    private Label galerieLabel;
    @FXML
    private Label disponibleLabel;
    @FXML
    private Button closeBtn;
    @FXML
    private VBox achatSection;
    @FXML
    private TextField quantityField;
    @FXML
    private Button decrementBtn;
    @FXML
    private Button incrementBtn;
    @FXML
    private Button buyBtn;
    @FXML
    private Label totalPriceLabel;

    private Article article;
    private int currentQuantity = 1;
    private static final int CURRENT_USER_ID = 15;

    private ServiceCommande serviceCommande;
    private ServiceArticle serviceArticle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection connection = MyDb.getInstance().getConnection();
            serviceCommande = new ServiceCommande(connection);
            serviceArticle = new ServiceArticle(connection);
        } catch (Exception e) {
            System.err.println("Erreur initialisation services: " + e.getMessage());
        }

        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?([\\.,][0-9]*)?");
        TextFormatter<Integer> textFormatter = new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return change;
            } else {
                return null;
            }
        });
        quantityField.setTextFormatter(textFormatter);

        quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    int newQuantity = Integer.parseInt(newVal);
                    updateQuantity(newQuantity);
                } catch (NumberFormatException e) {
                }
            } else {
                updateTotalPrice();
            }
        });
    }

    public void setArticle(Article article) {
        this.article = article;
        if (article != null) {
            titleLabel.setText(article.getTitre());
            priceLabel.setText(String.format("%.2f DT", article.getPrix()));
            descriptionText.setText(article.getDescription());
            categoryLabel.setText(article.getCategorie());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateLabel.setText(article.getDatePub() != null ? article.getDatePub().format(formatter) : "-");
            likesLabel.setText(String.valueOf(article.getNbrLikes()));
            updateStockLabel();
            loadImage();

            if (article.isDisponible() && article.getNbrArticle() > 0) {
                achatSection.setVisible(true);
                achatSection.setManaged(true);
                currentQuantity = 1;
                quantityField.setText(String.valueOf(currentQuantity));
                updateQuantityButtons();
                updateTotalPrice();
            } else {
                achatSection.setVisible(false);
                achatSection.setManaged(false);
            }
        }
    }

    private void loadImage() {
                if (article.getContenu() != null && !article.getContenu().isEmpty()) {
            try {
                    String imagePath = article.getContenu();
                        if (!imagePath.startsWith("src/main/resources/UploadsGalerie/")) {
                            imagePath = "src/main/resources/UploadsGalerie/" + imagePath;
                        }
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    articleImage.setImage(new Image(imageFile.toURI().toString()));
                            } else {
                    System.err.println("Image non trouvée (détails): " + imagePath);
                    loadDefaultImage();
                }
            } catch (Exception e) {
                System.err.println("Erreur chargement image (détails): " + e.getMessage());
                loadDefaultImage();
            }
        } else {
            loadDefaultImage();
        }
    }

    private void loadDefaultImage() {
        try {
            InputStream input = getClass().getResourceAsStream("/images/default-product.png");
            if (input != null) {
                articleImage.setImage(new Image(input));
            } else {
                articleImage.setImage(null);
            }
        } catch (Exception e) {
            articleImage.setImage(null);
        }
    }

    @FXML
    private void handleClose() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/FrontShowArticle.fxml"));
            Parent root = loader.load();

            StackPane contentArea = findContentArea(titleLabel);

            if (contentArea != null) {
                StackPane.setMargin(root, new Insets(0));
                contentArea.getChildren().setAll(root);
            } else {
                Scene scene = titleLabel.getScene();
                scene.setRoot(root);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du retour à la page précédente: " + e.getMessage());
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

    @FXML
    private void handleDecrementQuantity() {
        updateQuantity(currentQuantity - 1);
    }

    @FXML
    private void handleIncrementQuantity() {
        updateQuantity(currentQuantity + 1);
    }

    private void updateQuantity(int newQuantity) {
        if (article == null) return;

        int maxQuantity = article.getNbrArticle();
        
        if (newQuantity < 1) {
            newQuantity = 1;
        } else if (newQuantity > maxQuantity) {
            newQuantity = maxQuantity;
        }

        if (newQuantity != currentQuantity) {
             currentQuantity = newQuantity;
             quantityField.setText(String.valueOf(currentQuantity));
        }
        updateQuantityButtons(); 
        updateTotalPrice();
    }

    private void updateQuantityButtons() {
        if (article == null) return;
        decrementBtn.setDisable(currentQuantity <= 1);
        incrementBtn.setDisable(currentQuantity >= article.getNbrArticle());
    }

    private void updateTotalPrice() {
         if (article == null) return;
         double total = currentQuantity * article.getPrix();
         totalPriceLabel.setText(String.format("Total: %.2f DT", total));
    }
    
    private void updateStockLabel() {
         if (article != null) {
             disponibleLabel.setText(String.format("En stock: %d", article.getNbrArticle()));
             if (article.getNbrArticle() <= 0) {
                 disponibleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff8080;");
                 disponibleLabel.setText("Stock épuisé");
             } else {
                 disponibleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #a0ffa0;");
             }
         } else {
             disponibleLabel.setText("En stock: -");
         }
    }

    @FXML
    private void handleAcheter() {
        if (article == null || serviceCommande == null || serviceArticle == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de traiter la commande (services non initialisés).");
            return;
        }

        if (currentQuantity > article.getNbrArticle()) {
             showAlert(Alert.AlertType.WARNING, "Stock insuffisant", "La quantité demandée dépasse le stock disponible.");
             return;
        }
        
        if (currentQuantity <= 0) {
            showAlert(Alert.AlertType.WARNING, "Quantité invalide", "Veuillez sélectionner une quantité valide (au moins 1).");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/StripePayment.fxml"));
            Parent root = loader.load();
            
            StripePaymentController paymentController = loader.getController();
            paymentController.initData(article, currentQuantity, success -> {
                if (success) {
                    article.setNbrArticle(article.getNbrArticle() - currentQuantity);
                    updateStockLabel();
                    updateQuantity(1);
                    if (article.getNbrArticle() <= 0) {
                        achatSection.setVisible(false);
                        achatSection.setManaged(false);
                    }
                }
            });

            Stage paymentStage = new Stage();
            paymentStage.setTitle("Paiement sécurisé");
            paymentStage.setScene(new Scene(root));
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'interface de paiement: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
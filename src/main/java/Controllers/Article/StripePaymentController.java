package Controllers.Article;

import Models.Article;
import Models.Commande;
import Services.ServiceArticle;
import Services.ServiceCommande;
import Utils.MyDb;
import Utils.StripePayment;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Consumer;

public class StripePaymentController implements Initializable {
    @FXML private WebView stripeWebView;
    @FXML private Label amountLabel;
    @FXML private Label errorLabel;
    @FXML private Button cancelButton;

    private WebEngine webEngine;
    private Article article;
    private int quantity;
    private double amount;
    private Consumer<Boolean> onPaymentComplete;
    private static final int CURRENT_USER_ID = 15;

    private ServiceCommande serviceCommande;
    private ServiceArticle serviceArticle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            serviceCommande = new ServiceCommande(MyDb.getInstance().getConnection());
            serviceArticle = new ServiceArticle(MyDb.getInstance().getConnection());
        } catch (Exception e) {
            showError("Erreur d'initialisation: " + e.getMessage());
        }

        webEngine = stripeWebView.getEngine();
    }

    public void initData(Article article, int quantity, Consumer<Boolean> onPaymentComplete) {
        this.article = article;
        this.quantity = quantity;
        this.amount = article.getPrix() * quantity;
        this.onPaymentComplete = onPaymentComplete;
        
        amountLabel.setText(String.format("%.2f Dt", amount));
        
        try {
            PaymentIntent paymentIntent = StripePayment.createPaymentIntent(amount);
            loadStripeForm(paymentIntent.getClientSecret());
        } catch (StripeException e) {
            showError("Erreur lors de l'initialisation du paiement: " + e.getMessage());
        }
    }

    private void loadStripeForm(String clientSecret) {
        String stripePublicKey = StripePayment.getPublicKey();
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <title>Stripe Payment</title>
                <script src="https://js.stripe.com/v3/"></script>
                <style>
                    .payment-form { max-width: 500px; margin: 0 auto; padding: 20px; }
                    #payment-element { margin: 20px 0; }
                    #submit-button {
                        background: #5469d4;
                        color: #ffffff;
                        border-radius: 4px;
                        border: 0;
                        padding: 12px 16px;
                        font-size: 16px;
                        cursor: pointer;
                        width: 100%%;
                    }
                    #error-message { color: #df1b41; }
                </style>
            </head>
            <body>
                <form id="payment-form" class="payment-form">
                    <div id="payment-element"></div>
                    <button id="submit-button">Payer</button>
                    <div id="error-message"></div>
                </form>
                <script>
                    const stripe = Stripe('%s');
                    const elements = stripe.elements({clientSecret: '%s'});
                    const paymentElement = elements.create('payment');
                    paymentElement.mount('#payment-element');

                    const form = document.getElementById('payment-form');
                    form.addEventListener('submit', async (event) => {
                        event.preventDefault();
                        const {error} = await stripe.confirmPayment({
                            elements,
                            redirect: 'if_required'
                        });
                        
                        if (error) {
                            const messageDiv = document.getElementById('error-message');
                            messageDiv.textContent = error.message;
                        } else {
                            window.status = 'completed';
                        }
                    });
                </script>
            </body>
            </html>
        """, stripePublicKey, clientSecret);

        webEngine.loadContent(htmlContent);

        webEngine.setOnStatusChanged(event -> {
            if ("completed".equals(event.getData())) {
                handlePaymentSuccess();
            }
        });
    }

    private void handlePaymentSuccess() {
        Platform.runLater(() -> {
            try {
                String numeroCommande = "CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Commande newCommande = new Commande(
                    CURRENT_USER_ID,
                    article.getId(),
                    numeroCommande,
                    quantity,
                    article.getPrix()
                );

                serviceCommande.insert(newCommande);
                serviceArticle.decrementQuantity(article.getId(), quantity);

                // Load order confirmation page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/OrderConfirmation.fxml"));
                Parent root = loader.load();

                OrderConfirmationController controller = loader.getController();
                controller.initData(newCommande, article);

                Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                Scene scene = new Scene(root);
                currentStage.setScene(scene);
                currentStage.setTitle("Confirmation de commande");

                if (onPaymentComplete != null) {
                    onPaymentComplete.accept(true);
                }
            } catch (SQLException | IOException e) {
                showError("Erreur lors de l'enregistrement de la commande: " + e.getMessage());
                if (onPaymentComplete != null) {
                    onPaymentComplete.accept(false);
                }
            }
        });
    }

    @FXML
    private void handleCancel() {
        if (onPaymentComplete != null) {
            onPaymentComplete.accept(false);
        }
        closeWindow();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
} 
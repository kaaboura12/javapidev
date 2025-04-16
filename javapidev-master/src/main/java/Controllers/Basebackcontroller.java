package Controllers;

import Models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Control;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Basebackcontroller implements Initializable {
    
    @FXML
    private AnchorPane contentArea;
    
    @FXML
    private Label currentUserLabel;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private MenuButton eventsBtn;
    
    @FXML
    private Button usersBtn;
    
    @FXML
    private Button donationsBtn;
    
    @FXML
    private Button reservationsBtn;
    
    @FXML
    private Button statisticsBtn;
    
    @FXML
    private Button logoutBtn;
    
    // Keep track of the currently active control to highlight it
    private Control currentActiveControl;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set dashboard as the default view
        currentActiveControl = dashboardBtn;
        setActiveControl(dashboardBtn);
        loadDashboard();
        
        // Setup menu item actions
        for (MenuItem item : eventsBtn.getItems()) {
            if (item.getText().equals("Event Dashboard")) {
                item.setOnAction(e -> navigateToEventDashboard());
            } else if (item.getText().equals("Event List")) {
                item.setOnAction(e -> navigateToEventList());
            }
        }
    }
    
    @FXML
    private void navigateToDashboard() {
        setActiveControl(dashboardBtn);
        loadPage("/Views/dashboard/dashboard.fxml");
    }
    
    @FXML
    private void navigateToEventDashboard() {
        setActiveControl(eventsBtn);
        loadPage("/Views/backevent/eventdashboard.fxml");
    }
    
    @FXML
    private void navigateToEventList() {
        setActiveControl(eventsBtn);
        loadPage("/Views/backevent/eventbacklist.fxml");
    }
    
    @FXML
    private void navigateToUsers() {
        setActiveControl(usersBtn);
        loadPage("/Views/users/listuser.fxml");
    }
    
    @FXML
    private void navigateToDonations() {
        setActiveControl(donationsBtn);
        loadPage("/Views/backdonation/donationbacklist.fxml");
    }
    
    @FXML
    private void navigateToReservations() {
        setActiveControl(reservationsBtn);
        loadPage("/Views/backreservation/reservationbacklist.fxml");
    }
    
    @FXML
    private void navigateToStatistics() {
        setActiveControl(statisticsBtn);
        loadPage("/Views/statistics/statistics.fxml");
    }
    
    @FXML
    private void logout() {
        // Implementation for logout functionality
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/login.fxml"));
            Parent root = loader.load();
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Helper method to load the dashboard view
    private void loadDashboard() {
        loadPage("/Views/dashboard/dashboard.fxml");
    }
    
    // Helper method to load a page into the content area
    private void loadPage(String fxmlPath) {
        try {
            System.out.println("Tentative de chargement de: " + fxmlPath);
            
            // Obtenir l'URL de la ressource
            URL resourceUrl = getClass().getResource(fxmlPath);
            
            if (resourceUrl == null) {
                System.err.println("Erreur: Ressource introuvable: " + fxmlPath);
                // Essayer de charger une vue par défaut ou afficher un message d'erreur
                return;
            }
            
            System.out.println("Ressource trouvée: " + resourceUrl);
            
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent page = loader.load();
            
            // Set the loaded page in the content area
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur détaillée lors du chargement de la page: " + fxmlPath);
            System.err.println("Message d'erreur: " + e.getMessage());
        }
    }
    
    // Helper method to set the active control in the sidebar
    private void setActiveControl(Control control) {
        // Remove active style from previous control
        if (currentActiveControl != null) {
            currentActiveControl.getStyleClass().remove("active");
        }
        
        // Add active style to current control
        if (!control.getStyleClass().contains("active")) {
            control.getStyleClass().add("active");
        }
        
        // Update current active control
        currentActiveControl = control;
    }
    
    // Method to set the current user's name in the top bar
    public void setCurrentUser(String username) {
        if (username != null && !username.isEmpty()) {
            currentUserLabel.setText("Welcome, " + username);
        }
    }
    
    // Méthode pour afficher une interface utilisateur simple lorsque le fichier FXML n'est pas trouvé
    private void showSimpleUserInterface() {
        try {
            // Créer une interface utilisateur basique
            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 20px; -fx-background-color: #f8f9fa;");
            
            Label title = new Label("Gestion des Utilisateurs");
            title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
            
            Button refreshButton = new Button("Rafraîchir la liste");
            refreshButton.setStyle("-fx-background-color: #3182ce; -fx-text-fill: white; -fx-padding: 10px 20px;");
            
            TableView<User> usersTable = new TableView<>();
            TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
            TableColumn<User, String> nomColumn = new TableColumn<>("Nom");
            TableColumn<User, String> prenomColumn = new TableColumn<>("Prénom");
            TableColumn<User, String> emailColumn = new TableColumn<>("Email");
            
            usersTable.getColumns().addAll(idColumn, nomColumn, prenomColumn, emailColumn);
            
            content.getChildren().addAll(title, refreshButton, usersTable);
            
            // Ajouter à la zone de contenu
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
            
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            
            System.out.println("Interface utilisateur simple créée avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de la création de l'interface utilisateur simple: " + e.getMessage());
        }
    }
}

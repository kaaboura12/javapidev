package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Control;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Basebackcontroller implements Initializable {
    
    @FXML
    private AnchorPane contentArea;
    
    @FXML
    private Label currentUserLabel;
    
    @FXML
    private Label pageTitle;
    
    @FXML
    private Label usernameLabel;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private Button eventDashboardBtn;
    
    @FXML
    private Button eventListBtn;
    
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
    
    @FXML
    private Button forumPostsBtn;
    
    @FXML
    private Button badWordsBtn;
    
    // Keep track of the currently active control to highlight it
    private Control currentActiveControl;
    
    // Keep track of current username
    private String username = "Admin";
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set dashboard as the default view
        currentActiveControl = dashboardBtn;
        setActiveControl(dashboardBtn);
        updatePageTitle("Dashboard");
        
        // Set default username in profile section
        updateUserInfo(username);
        
        // Load the dashboard
        loadDashboard();
    }
    
    @FXML
    private void navigateToDashboard() {
        setActiveControl(dashboardBtn);
        updatePageTitle("Dashboard");
        loadPage("/Views/dashboard/dashboard.fxml");
    }
    
    @FXML
    private void navigateToEventDashboard() {
        setActiveControl(eventDashboardBtn);
        updatePageTitle("Event Dashboard");
        loadPage("/Views/backevent/eventdashboard.fxml");
    }
    
    @FXML
    private void navigateToEventList() {
        setActiveControl(eventListBtn);
        updatePageTitle("Event List");
        loadPage("/Views/backevent/eventbacklist.fxml");
    }
    
    @FXML
    private void navigateToUsers() {
        setActiveControl(usersBtn);
        updatePageTitle("Users Management");
        loadPage("/Views/users/listuser.fxml");
    }
    
    @FXML
    private void navigateToDonations() {
        setActiveControl(donationsBtn);
        updatePageTitle("Donations");
        loadPage("/Views/backdonation/donationbacklist.fxml");
    }
    
    @FXML
    private void navigateToReservations() {
        setActiveControl(reservationsBtn);
        updatePageTitle("Reservations");
        loadPage("/Views/backreservation/reservationbacklist.fxml");
    }
    
    @FXML
    private void navigateToStatistics() {
        setActiveControl(statisticsBtn);
        updatePageTitle("Statistics");
        loadPage("/Views/statistics/statistics.fxml");
    }
    
    @FXML
    private void navigateToForumPosts() {
        setActiveControl(forumPostsBtn);
        updatePageTitle("Forum Posts");
        loadPage("/Views/backforum/backposts.fxml");
    }
    
    @FXML
    private void navigateToBadWords() {
        setActiveControl(badWordsBtn);
        updatePageTitle("Bad Words Management");
        loadPage("/Views/backforum/badwords.fxml");
    }
    
    @FXML
    private void logout() {
        try {
            // Get the current stage from any node
            Stage stage = (Stage) contentArea.getScene().getWindow();
            
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/login.fxml"));
            Parent root = loader.load();
            
            // Create new scene and set it on the stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating to login screen: " + e.getMessage());
        }
    }
    
    // Helper method to load the dashboard view
    private void loadDashboard() {
        loadPage("/Views/dashboard/dashboard.fxml");
    }
    
    // Helper method to load a page into the content area
    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            
            // Set the loaded page in the content area
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
            
            // Try to get controller to pass user info if needed
            Object controller = loader.getController();
            if (controller != null && controller instanceof BaseController) {
                ((BaseController) controller).setCurrentUser(username);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading page: " + fxmlPath);
            System.err.println(e.getMessage());
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
    
    // Method to update the page title
    private void updatePageTitle(String title) {
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
    }
    
    // Method to update user info displayed in sidebar
    private void updateUserInfo(String username) {
        if (username != null && !username.isEmpty()) {
            this.username = username;
            
            // Update top bar welcome message
            if (currentUserLabel != null) {
                currentUserLabel.setText("Welcome, " + username);
            }
            
            // Update sidebar username
            if (usernameLabel != null) {
                usernameLabel.setText(username);
            }
        }
    }
    
    // Method to set the current user's name 
    public void setCurrentUser(String username) {
        updateUserInfo(username);
    }
    
    // Interface for child controllers
    public interface BaseController {
        void setCurrentUser(String username);
    }
}

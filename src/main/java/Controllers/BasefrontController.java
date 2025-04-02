package Controllers;

import Utils.ViewLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class BasefrontController implements Initializable {
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private Button accueilBtn;
    
    @FXML
    private Button galerieBtn;
    
    @FXML
    private Button formationsBtn;
    
    @FXML
    private Button eventsBtn;
    
    @FXML
    private Button emploiBtn;
    
    @FXML
    private Button forumBtn;
    
    @FXML
    private Label usernameLabel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set username (can be replaced with actual user data)
        if (usernameLabel != null) {
            usernameLabel.setText("Username");
        }
        
        // Store this controller in scene user data for access by child controllers
        if (contentArea != null && contentArea.getScene() != null) {
            contentArea.getScene().setUserData(this);
        }
        
        // Initialize components
        setupButtons();
        
        // Load the default view
        loadDefaultView();
    }
    
    private void setupButtons() {
        // Setup navigation buttons
        setupNavButton(accueilBtn, "/Views/home.fxml");
        setupNavButton(galerieBtn, "/Views/galerie.fxml");
        setupNavButton(formationsBtn, "/Views/formations.fxml");
        setupNavButton(eventsBtn, "/Views/event/listevent.fxml");
        setupNavButton(emploiBtn, "/Views/emploi.fxml");
        setupNavButton(forumBtn, "/Views/forum.fxml");
        
        // Setup logout button
        if (logoutBtn != null) {
            logoutBtn.setOnAction(event -> handleLogout());
        }
    }
    
    /**
     * Set up a navigation button
     * 
     * @param button The button to set up
     * @param fxmlPath The path to the FXML file to load
     */
    private void setupNavButton(Button button, String fxmlPath) {
        if (button != null) {
            button.setOnAction(event -> navigateTo(fxmlPath));
        }
    }
    
    /**
     * Navigate to a view
     * 
     * @param fxmlPath The path to the FXML file to load
     */
    public void navigateTo(String fxmlPath) {
        if (contentArea != null) {
            System.out.println("Navigating to: " + fxmlPath);
            // Check if ViewLoader class exists
            try {
                // Set this controller as userData if the scene is null
                if (contentArea.getScene() != null && contentArea.getScene().getUserData() == null) {
                    contentArea.getScene().setUserData(this);
                }
                
                Class.forName("Utils.ViewLoader");
                ViewLoader.loadView(contentArea, fxmlPath);
            } catch (ClassNotFoundException e) {
                System.err.println("ViewLoader class not found. Navigation not implemented.");
                // You could implement a fallback navigation here
            } catch (Exception e) {
                System.err.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
            }
        }
    }
    
    private void loadDefaultView() {
        System.out.println("Loading default view");
        navigateTo("/Views/home.fxml");
    }
    
    private void handleLogout() {
        System.out.println("Logging out");
        // Add logout logic here
    }
} 
package Controllers;

import Utils.ViewLoader;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
    
    @FXML
    private HBox mainNavBar;
    
    @FXML
    private HBox navButtonsContainer;
    
    @FXML
    private Button hamburgerBtn;
    
    @FXML
    private VBox mobileNavMenu;
    
    @FXML
    private Button mobileAccueilBtn;
    
    @FXML
    private Button mobileGalerieBtn;
    
    @FXML
    private Button mobileFormationsBtn;
    
    @FXML
    private Button mobileEventsBtn;
    
    @FXML
    private Button mobileEmploiBtn;
    
    @FXML
    private Button mobileForumBtn;
    
    private Button activeButton;
    private List<Button> navButtons;
    private List<Button> mobileNavButtons;
    private boolean isMobileMenuOpen = false;
    
    // Screen size breakpoints
    private static final double SMALL_SCREEN_WIDTH = 600;
    private static final double MEDIUM_SCREEN_WIDTH = 900;
    
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
        
        // Setup responsive behavior
        setupResponsiveBehavior();
        
        // Load the default view
        loadDefaultView();
    }
    
    private void setupButtons() {
        // Store all nav buttons in a list for easier management
        navButtons = Arrays.asList(accueilBtn, galerieBtn, formationsBtn, eventsBtn, emploiBtn, forumBtn);
        mobileNavButtons = Arrays.asList(mobileAccueilBtn, mobileGalerieBtn, mobileFormationsBtn, mobileEventsBtn, mobileEmploiBtn, mobileForumBtn);
        
        // Setup navigation buttons
        setupNavButton(accueilBtn, "/Views/home.fxml");
        setupNavButton(galerieBtn, "/Views/Article/FrontShowArticle.fxml");
        setupNavButton(formationsBtn, "/Views/formations.fxml");
        setupNavButton(eventsBtn, "/Views/event/listevent.fxml");
        setupNavButton(emploiBtn, "/Views/emploi.fxml");
        setupNavButton(forumBtn, "/Views/forum.fxml");
        
        // Setup mobile navigation buttons
        setupNavButton(mobileAccueilBtn, "/Views/home.fxml");
        setupNavButton(mobileGalerieBtn, "/Views/galerie.fxml");
        setupNavButton(mobileFormationsBtn, "/Views/formations.fxml");
        setupNavButton(mobileEventsBtn, "/Views/event/listevent.fxml");
        setupNavButton(mobileEmploiBtn, "/Views/emploi.fxml");
        setupNavButton(mobileForumBtn, "/Views/forum.fxml");
        
        // Setup hamburger button
        if (hamburgerBtn != null) {
            hamburgerBtn.setOnAction(event -> toggleMobileMenu());
        }
        
        // Setup logout button
        if (logoutBtn != null) {
            logoutBtn.setOnAction(event -> handleLogout());
            
            // Add hover animation to logout button
            logoutBtn.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), logoutBtn);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });
            
            logoutBtn.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), logoutBtn);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });
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
            button.setOnAction(event -> {
                if (activeButton != button) {
                    updateActiveButton(button);
                    
                    // If it's a mobile button, also close the mobile menu
                    if (mobileNavButtons.contains(button)) {
                        closeMobileMenu();
                    }
                    
                    navigateTo(fxmlPath);
                }
            });
        }
    }
    
    /**
     * Update the active button styling
     * 
     * @param newActiveButton The button to set as active
     */
    private void updateActiveButton(Button newActiveButton) {
        List<Button> allButtons = new java.util.ArrayList<>(navButtons);
        allButtons.addAll(mobileNavButtons);
        
        // Reset all buttons
        for (Button btn : allButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
            }
        }
        
        // Set new active button
        if (newActiveButton != null) {
            newActiveButton.getStyleClass().add("active");
            activeButton = newActiveButton;
            
            // Also update the corresponding mobile/desktop button
            int index;
            if (navButtons.contains(newActiveButton)) {
                index = navButtons.indexOf(newActiveButton);
                if (index >= 0 && index < mobileNavButtons.size()) {
                    mobileNavButtons.get(index).getStyleClass().add("active");
                }
            } else if (mobileNavButtons.contains(newActiveButton)) {
                index = mobileNavButtons.indexOf(newActiveButton);
                if (index >= 0 && index < navButtons.size()) {
                    navButtons.get(index).getStyleClass().add("active");
                }
            }
        }
    }
    
    /**
     * Navigate to a view with transition animation
     * 
     * @param fxmlPath The path to the FXML file to load
     */
    public void navigateTo(String fxmlPath) {
        if (contentArea != null) {
            System.out.println("Navigating to: " + fxmlPath);
            
            // Set this controller as userData if the scene is null
            if (contentArea.getScene() != null && contentArea.getScene().getUserData() == null) {
                contentArea.getScene().setUserData(this);
            }
            
            try {
                // Fade out current content
                FadeTransition fadeOut = new FadeTransition(Duration.millis(180), contentArea);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.9);
                
                // After fade out, load new view
                fadeOut.setOnFinished(e -> {
                    try {
                        // Load the new view
                        ViewLoader.loadView(contentArea, fxmlPath);
                        
                        // Create fade in and scale transition
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), contentArea);
                        fadeIn.setFromValue(0.9);
                        fadeIn.setToValue(1.0);
                        
                        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), contentArea);
                        scaleIn.setFromX(0.99);
                        scaleIn.setFromY(0.99);
                        scaleIn.setToX(1.0);
                        scaleIn.setToY(1.0);
                        
                        // Play parallel transition
                        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
                        parallelTransition.play();
                    } catch (Exception ex) {
                        System.err.println("Error loading view: " + ex.getMessage());
                    }
                });
                
                // Start the transition
                fadeOut.play();
            } catch (Exception e) {
                System.err.println("Error navigating to " + fxmlPath + ": " + e.getMessage());
            }
        }
    }
    
    private void loadDefaultView() {
        System.out.println("Loading default view");
        if (accueilBtn != null) {
            updateActiveButton(accueilBtn);
        }
        navigateTo("/Views/home.fxml");
    }
    
    private void handleLogout() {
        System.out.println("Logging out");
        // Add logout logic here
        
        // Example: fade out animation before logout
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), contentArea.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Here you would handle actual logout logic, like redirecting to login screen
            System.out.println("Logout complete");
        });
        fadeOut.play();
    }
    
    /**
     * Setup responsive behavior based on window size
     */
    private void setupResponsiveBehavior() {
        // We need the scene to be available
        if (contentArea != null) {
            // Use a runLater because the scene might not be ready yet
            javafx.application.Platform.runLater(() -> {
                Scene scene = contentArea.getScene();
                if (scene != null) {
                    // Initial size check
                    adjustToScreenSize(scene.getWidth());
                    
                    // Listen for window size changes
                    ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
                        adjustToScreenSize(scene.getWidth());
                    };
                    
                    // Add listeners for window width and height
                    scene.widthProperty().addListener(stageSizeListener);
                    
                    // Also listen for window focus to close mobile menu when clicking outside
                    scene.getWindow().focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (isNowFocused && isMobileMenuOpen) {
                            closeMobileMenu();
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Adjust UI based on screen size
     * 
     * @param width Current screen width
     */
    private void adjustToScreenSize(double width) {
        Parent root = contentArea.getScene().getRoot();
        
        // Remove all screen size classes first
        root.getStyleClass().removeAll(
            "screen-size-small", 
            "screen-size-medium", 
            "screen-size-large"
        );
        
        // Apply appropriate class based on width
        if (width <= SMALL_SCREEN_WIDTH) {
            root.getStyleClass().add("screen-size-small");
            hamburgerBtn.setVisible(true);
            hamburgerBtn.setManaged(true);
        } else if (width <= MEDIUM_SCREEN_WIDTH) {
            root.getStyleClass().add("screen-size-medium");
            hamburgerBtn.setVisible(false);
            hamburgerBtn.setManaged(false);
            closeMobileMenu();
        } else {
            root.getStyleClass().add("screen-size-large");
            hamburgerBtn.setVisible(false);
            hamburgerBtn.setManaged(false);
            closeMobileMenu();
        }
    }
    
    /**
     * Toggle the mobile menu visibility
     */
    private void toggleMobileMenu() {
        if (isMobileMenuOpen) {
            closeMobileMenu();
        } else {
            openMobileMenu();
        }
    }
    
    /**
     * Open the mobile menu with animation
     */
    private void openMobileMenu() {
        if (mobileNavMenu != null) {
            mobileNavMenu.getStyleClass().add("slide-down");
            mobileNavMenu.setVisible(true);
            mobileNavMenu.setManaged(true);
            
            // Add animation
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), mobileNavMenu);
            slideDown.setFromY(-20);
            slideDown.setToY(0);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mobileNavMenu);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ParallelTransition parallelTransition = new ParallelTransition(slideDown, fadeIn);
            parallelTransition.play();
            
            isMobileMenuOpen = true;
        }
    }
    
    /**
     * Close the mobile menu with animation
     */
    private void closeMobileMenu() {
        if (mobileNavMenu != null && mobileNavMenu.isVisible()) {
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(150), mobileNavMenu);
            slideUp.setFromY(0);
            slideUp.setToY(-20);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), mobileNavMenu);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            ParallelTransition parallelTransition = new ParallelTransition(slideUp, fadeOut);
            parallelTransition.setOnFinished(event -> {
                mobileNavMenu.setVisible(false);
                mobileNavMenu.setManaged(false);
                mobileNavMenu.getStyleClass().remove("slide-down");
            });
            parallelTransition.play();
            
            isMobileMenuOpen = false;
        }
    }
} 
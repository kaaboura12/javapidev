package Controllers;

import Models.User;
import Utils.ViewLoader;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private Button createAccountBtn;
    
    @FXML
    private Button loginBtn;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private Button backOfficeBtn;
    
    @FXML
    private Button artisteResidentBtn;
    
    @FXML
    private Label usernameLabel;
    
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
    
    // Add current user field
    private User currentUser;
    
    // Screen size breakpoints
    private static final double SMALL_SCREEN_WIDTH = 600;
    private static final double MEDIUM_SCREEN_WIDTH = 900;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup username label for profile navigation
        if (usernameLabel != null) {
            // Add click handler to username label to go to profile
            usernameLabel.setOnMouseClicked(event -> navigateToUserProfile());
            
            // Add cursor style to show it's clickable
            usernameLabel.getStyleClass().add("clickable");
        }
        
        // Store this controller in scene user data for access by child controllers
        if (contentArea != null && contentArea.getScene() != null) {
            contentArea.getScene().setUserData(this);
        }
        
        // Set controller in scene even after scene changes
        javafx.application.Platform.runLater(() -> {
            if (contentArea != null && contentArea.getScene() != null && contentArea.getScene().getUserData() == null) {
                contentArea.getScene().setUserData(this);
            }
            
            // Set window to fullscreen
            setSceneFullScreen();
        });
        
        // Initialize components
        setupButtons();
        
        // Setup responsive behavior
        setupResponsiveBehavior();
        
        // Load the default view
        loadDefaultView();
    }
    
    /**
     * Set the current user and update the UI accordingly
     * 
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // Update UI elements based on user permissions
        updateUIForUserPermissions();
        
        if (user != null) {
            System.out.println("User set: " + user.getPrenom() + " " + user.getNom() + " with role: " + user.getRoles());
        }
    }
    
    /**
     * Get the current logged-in user
     * 
     * @return The current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    private void setupButtons() {
        // Store all nav buttons in a list for easier management
        navButtons = Arrays.asList(accueilBtn, galerieBtn, formationsBtn, eventsBtn, emploiBtn, forumBtn);
        mobileNavButtons = Arrays.asList(mobileAccueilBtn, mobileGalerieBtn, mobileFormationsBtn, mobileEventsBtn, mobileEmploiBtn, mobileForumBtn);
        
        // Setup navigation buttons
        setupNavButton(accueilBtn, "/Views/home.fxml");
        setupNavButton(galerieBtn, "/Views/galerie.fxml");
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
        
        // Setup create account button
        if (createAccountBtn != null) {
            createAccountBtn.setOnAction(event -> navigateToRegistration());
            
            // Add hover animation
            createAccountBtn.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), createAccountBtn);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });
            
            createAccountBtn.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), createAccountBtn);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });
        }
        
        // Setup login button
        if (loginBtn != null) {
            loginBtn.setOnAction(event -> navigateToLogin());
            
            // Add hover animation
            loginBtn.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), loginBtn);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });
            
            loginBtn.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), loginBtn);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });
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
        
        // Setup back office button
        if (backOfficeBtn != null) {
            backOfficeBtn.setOnAction(event -> navigateToBackOffice());
            
            // Add hover animation to back office button
            backOfficeBtn.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), backOfficeBtn);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });
            
            backOfficeBtn.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), backOfficeBtn);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });
        }
        
        // Setup artiste resident button
        if (artisteResidentBtn != null) {
            artisteResidentBtn.setOnAction(event -> navigateToArtisteResidentForm());
            
            // Add hover animation
            artisteResidentBtn.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), artisteResidentBtn);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });
            
            artisteResidentBtn.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), artisteResidentBtn);
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
                        
                        // Ensure scene stays fullscreen
                        setSceneFullScreen();
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
        
        // You could check authentication here if home page requires login
        // if (!checkUserAuthentication()) return;
        
        navigateTo("/Views/home.fxml");
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
    
    /**
     * Check if a user is currently logged in
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Check if the current user has a specific role
     * 
     * @param role The role to check for
     * @return true if the user has the specified role, false otherwise
     */
    public boolean hasRole(String role) {
        if (currentUser == null || currentUser.getRoles() == null) {
            return false;
        }
        
        // Check if the user's roles contain the specified role
        return currentUser.getRoles().toUpperCase().contains(role.toUpperCase());
    }
    
    /**
     * Update UI elements based on user permissions
     * This method should be called whenever the user changes
     */
    public void updateUIForUserPermissions() {
        if (currentUser == null) {
            // Logic for non-logged in users            
            // Show login and registration buttons, hide username and logout
            if (loginBtn != null) loginBtn.setVisible(true);
            if (createAccountBtn != null) createAccountBtn.setVisible(true);
            if (usernameLabel != null) usernameLabel.setVisible(false);
            if (logoutBtn != null) logoutBtn.setVisible(false);
            if (backOfficeBtn != null) backOfficeBtn.setVisible(false);
            if (artisteResidentBtn != null) artisteResidentBtn.setVisible(false);
            
            return;
        }
        
        // Logic for logged in users
        
        // Update username display
        if (usernameLabel != null) {
            usernameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            usernameLabel.setVisible(true);
        }
        
        // Hide login and registration buttons, show logout button
        if (loginBtn != null) loginBtn.setVisible(false);
        if (createAccountBtn != null) createAccountBtn.setVisible(false);
        if (logoutBtn != null) logoutBtn.setVisible(true);
        
        // Add role-specific UI adjustments here
        // Show back office button only for admin users
        boolean isAdmin = hasRole("ADMIN");
        boolean isUser = hasRole("USER");
        boolean isArtist = hasRole("ARTIST");
        
        if (backOfficeBtn != null) {
            backOfficeBtn.setVisible(isAdmin);
        }
        
        // Show artiste resident button only for regular users and artists
        if (artisteResidentBtn != null) {
            artisteResidentBtn.setVisible(isUser || isArtist);
        }
        
        // You can add more UI adjustments based on user roles here
    }
    
    /**
     * Navigate to user profile page
     */
    private void navigateToUserProfile() {
        if (currentUser != null) {
            navigateTo("/Views/user/profile.fxml");
        }
    }
    
    /**
     * Check if user is authenticated, redirect to login if not
     * Should be called when loading secure pages
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean checkUserAuthentication() {
        if (currentUser == null) {
            try {
                // If not authenticated, redirect to login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/user/login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setScene(new Scene(root));
                
                System.out.println("User not authenticated, redirecting to login");
                return false;
            } catch (Exception e) {
                System.err.println("Error redirecting to login: " + e.getMessage());
            }
            return false;
        }
        return true;
    }
    
    /**
     * Navigate to the registration page
     */
    private void navigateToRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/user/registration.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Set fullscreen
            stage.setMaximized(true);
            stage.setFullScreen(true);
            
            System.out.println("Navigated to registration page");
        } catch (Exception e) {
            System.err.println("Error navigating to registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to the login page
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/user/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Set fullscreen
            stage.setMaximized(true);
            stage.setFullScreen(true);
            
            System.out.println("Navigated to login page");
        } catch (Exception e) {
            System.err.println("Error navigating to login page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to user-related pages - Made public so it can be called from other controllers
     * 
     * @param page The page to navigate to: "login", "register", "profile", etc.
     */
    public void navigateToUserPage(String page) {
        try {
            String path = "";
            switch (page.toLowerCase()) {
                case "login":
                    path = "/Views/user/login.fxml";
                    break;
                case "register":
                case "registration":
                    path = "/Views/user/registration.fxml";
                    break;
                case "profile":
                    path = "/Views/user/profile.fxml";
                    break;
                case "settings":
                    path = "/Views/user/settings.fxml";
                    break;
                default:
                    path = "/Views/user/login.fxml";
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Set fullscreen
            stage.setMaximized(true);
            stage.setFullScreen(true);
            
            System.out.println("Navigated to " + page + " page");
        } catch (Exception e) {
            System.err.println("Error navigating to " + page + " page: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleLogout() {
        System.out.println("Logging out");
        
        // Add logout logic here
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), contentArea.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            try {
                // Clear current user
                currentUser = null;
                
                // Navigate to login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/user/login.fxml"));
                Parent root = loader.load();
                
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                
                // Set fullscreen
                stage.setMaximized(true);
                stage.setFullScreen(true);
                
                System.out.println("Redirected to login screen");
            } catch (Exception ex) {
                System.err.println("Error during logout: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        fadeOut.play();
    }
    
    /**
     * Navigate to the back office admin panel
     */
    private void navigateToBackOffice() {
        if (hasRole("ADMIN") || hasRole("ROLE_ADMIN")) {
            try {
                // Use FadeTransition for smooth transition
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), contentArea.getScene().getRoot());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    try {
                        // Load the back office view
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/baseback.fxml"));
                        Parent root = loader.load();
                        
                        // Get the back office controller and pass the current user info
                        Basebackcontroller controller = loader.getController();
                        controller.setCurrentUser(currentUser.getPrenom() + " " + currentUser.getNom());
                        
                        // Set the scene
                        Stage stage = (Stage) backOfficeBtn.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        
                        // Set fullscreen
                        stage.setMaximized(true);
                        stage.setFullScreen(true);
                        
                        // Fade in transition
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                        
                        System.out.println("Navigated to back office");
                    } catch (Exception ex) {
                        System.err.println("Error navigating to back office: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
                fadeOut.play();
            } catch (Exception e) {
                System.err.println("Error preparing navigation to back office: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Access denied: User doesn't have admin privileges");
        }
    }
    
    /**
     * Navigate to the artiste resident application form
     */
    private void navigateToArtisteResidentForm() {
        if (currentUser != null && (hasRole("USER") || hasRole("ARTIST"))) {
            try {
                System.out.println("Navigating to artiste resident form");
                
                // Use FadeTransition for smooth transition
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), contentArea);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.3);
                
                fadeOut.setOnFinished(e -> {
                    try {
                        // Load the view with ViewLoader
                        ArtisteResidentFormController controller = Utils.ViewLoader.loadView(contentArea, "/Views/user/artiste_resident_form.fxml");
                        
                        // If controller is loaded successfully, set the current user
                        if (controller != null) {
                            controller.setCurrentUser(currentUser);
                            System.out.println("Controller loaded and user set");
                        } else {
                            System.err.println("Failed to get controller");
                        }
                        
                        // Add fade in effect
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentArea);
                        fadeIn.setFromValue(0.3);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                        
                        System.out.println("Navigated to artiste resident application form");
                    } catch (Exception ex) {
                        System.err.println("Error navigating to artiste resident form: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
                
                fadeOut.play();
            } catch (Exception e) {
                System.err.println("Error preparing navigation to artiste resident form: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Access denied: User not logged in or doesn't have required privileges");
        }
    }
    
    /**
     * Set the application window to fullscreen
     */
    private void setSceneFullScreen() {
        if (contentArea != null && contentArea.getScene() != null) {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
                stage.setFullScreen(true);
                // Optional: Disable fullscreen exit hint
                stage.setFullScreenExitHint("");
            }
        }
    }
} 
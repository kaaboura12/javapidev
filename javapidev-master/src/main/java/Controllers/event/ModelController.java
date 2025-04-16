package Controllers.event;

import Utils.ModelLoader;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelController implements Initializable {

    @FXML
    private AnchorPane modelContainer;

    @FXML
    private SubScene modelScene;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Button resetButton;
    
    private Button backButton;
    private ToggleButton wireframeToggle;
    private Slider lightingSlider;
    private Label modelInfoLabel;
    private ProgressIndicator loadingIndicator;
    private StackPane loadingPane;

    private Group modelGroup;
    private Group worldGroup;
    private PerspectiveCamera camera;
    private ExecutorService executor;

    // Variables for mouse interaction
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    
    // Rotation transforms
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
    
    // Scale for zoom
    private final Scale scale = new Scale(1, 1, 1);
    
    // Animation for automatic rotation
    private RotateTransition rotateTransition;
    
    // Lighting for the model
    private Lighting lighting;
    private boolean wireframeMode = false;
    
    // Original materials for wireframe toggle
    private PhongMaterial[] originalMaterials;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize executor service
        executor = Executors.newSingleThreadExecutor();
        
        // Create loading overlay
        setupLoadingIndicator();
        
        // Create camera
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1000);

        // Create model group
        modelGroup = new Group();
        worldGroup = new Group();
        worldGroup.getChildren().add(modelGroup);
        
        // Add transformation capabilities
        modelGroup.getTransforms().addAll(rotateX, rotateY, rotateZ, scale);
        
        // Create and setup the scene
        modelScene = new SubScene(worldGroup, 600, 400, true, javafx.scene.SceneAntialiasing.BALANCED);
        modelScene.setFill(Color.DARKGRAY);
        modelScene.setCamera(camera);
        
        // Add the scene to the container
        AnchorPane.setTopAnchor(modelScene, 0.0);
        AnchorPane.setLeftAnchor(modelScene, 0.0);
        AnchorPane.setRightAnchor(modelScene, 0.0);
        AnchorPane.setBottomAnchor(modelScene, 50.0);
        modelContainer.getChildren().add(modelScene);
        
        // Setup UI controls
        setupControls();
        
        // Load the 3D model asynchronously
        showLoadingIndicator();
        loadModelAsync();
        
        // Start rotating animation
        setupRotation();
        
        // Setup scene resize listener
        modelScene.widthProperty().addListener((obs, oldVal, newVal) -> {
            resizeScene();
        });
        
        modelScene.heightProperty().addListener((obs, oldVal, newVal) -> {
            resizeScene();
        });
    }
    
    private void setupLoadingIndicator() {
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(100, 100);
        
        Label loadingLabel = new Label("Loading 3D Model...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
        
        loadingPane = new StackPane(loadingBox);
        loadingPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        
        AnchorPane.setTopAnchor(loadingPane, 0.0);
        AnchorPane.setLeftAnchor(loadingPane, 0.0);
        AnchorPane.setRightAnchor(loadingPane, 0.0);
        AnchorPane.setBottomAnchor(loadingPane, 0.0);
    }
    
    private void showLoadingIndicator() {
        if (!modelContainer.getChildren().contains(loadingPane)) {
            modelContainer.getChildren().add(loadingPane);
        }
    }
    
    private void hideLoadingIndicator() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loadingPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            modelContainer.getChildren().remove(loadingPane);
        });
        fadeOut.play();
    }
    
    private void setupControls() {
        // Setup zoom slider if not null
        if (zoomSlider != null) {
            zoomSlider.setMin(0.5);
            zoomSlider.setMax(2.0);
            zoomSlider.setValue(1.0);
            zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                scale.setX(newVal.doubleValue());
                scale.setY(newVal.doubleValue());
                scale.setZ(newVal.doubleValue());
            });
            
            // Add tooltip
            Tooltip.install(zoomSlider, new Tooltip("Adjust zoom level"));
        }
        
        // Setup reset button if not null
        if (resetButton != null) {
            resetButton.setOnAction(e -> resetView());
            
            // Add tooltip
            Tooltip.install(resetButton, new Tooltip("Reset view to default position"));
            
            // Add hover effect
            addButtonEffect(resetButton);
        }
        
        // Create wireframe toggle
        wireframeToggle = new ToggleButton("Wireframe");
        wireframeToggle.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white;");
        wireframeToggle.setOnAction(e -> toggleWireframe());
        Tooltip.install(wireframeToggle, new Tooltip("Toggle wireframe view"));
        addButtonEffect(wireframeToggle);
        
        // Create lighting slider
        lightingSlider = new Slider(0, 1, 0.5);
        lightingSlider.setPrefWidth(150);
        lightingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            adjustLighting(newVal.doubleValue());
        });
        
        Label lightLabel = new Label("Light:");
        lightLabel.setStyle("-fx-text-fill: #E2E8F0;");
        
        HBox lightBox = new HBox(10);
        lightBox.setAlignment(Pos.CENTER);
        lightBox.getChildren().addAll(lightLabel, lightingSlider);
        
        // Add new controls to the bottom HBox
        VBox controlsBox = (VBox) resetButton.getParent().getParent();
        HBox controlsRow = (HBox) resetButton.getParent();
        
        controlsRow.getChildren().add(2, wireframeToggle);
        
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER);
        row2.getChildren().add(lightBox);
        row2.setPadding(new Insets(10, 0, 0, 0));
        
        controlsBox.getChildren().add(row2);
        
        // Setup mouse controls for rotation
        modelScene.setOnMousePressed(this::handleMousePressed);
        modelScene.setOnMouseDragged(this::handleMouseDragged);
        modelScene.setOnScroll(this::handleScroll);
        modelScene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetView();
            }
        });
    }
    
    /**
     * Set up a back button to return to the previous view
     */
    public void setupBackButton(Runnable returnAction) {
        // Create back button if it doesn't exist
        if (backButton == null) {
            backButton = new Button("← Back");
            backButton.getStyleClass().add("back-button");
            backButton.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white;");
            
            // Add hover effect
            addButtonEffect(backButton);
            
            // Find the controls box
            VBox controlsBox = (VBox) resetButton.getParent().getParent();
            
            // Create a container for the back button (top-left)
            HBox backButtonContainer = new HBox(backButton);
            backButtonContainer.setAlignment(Pos.CENTER_LEFT);
            backButtonContainer.setPadding(new Insets(0, 0, 10, 0));
            
            // Add to the top of controls
            controlsBox.getChildren().add(0, backButtonContainer);
        }
        
        // Set the action for the back button
        backButton.setOnAction(e -> {
            if (returnAction != null) {
                returnAction.run();
            }
        });
    }
    
    private void addButtonEffect(ButtonBase button) {
        // Store original style
        String originalStyle = button.getStyle();
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(originalStyle + "-fx-background-color: #718096; -fx-cursor: hand;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(originalStyle);
        });
    }
    
    private void toggleWireframe() {
        wireframeMode = !wireframeMode;
        
        if (wireframeMode) {
            // Save original materials if needed
            if (originalMaterials == null) {
                // Implementation depends on model structure
                // This is a placeholder - actual implementation will depend on model
            }
            
            // Set wireframe material on all parts
            PhongMaterial wireframeMaterial = new PhongMaterial(Color.WHITE);
            wireframeMaterial.setDiffuseColor(Color.LIGHTBLUE);
            wireframeMaterial.setSpecularColor(Color.BLUE);
            
            // Apply wireframe effect to model
            // Actual implementation depends on model structure
        } else {
            // Restore original materials
            if (originalMaterials != null) {
                // Restore materials to model parts
                // Implementation depends on model structure
            }
        }
    }
    
    private void adjustLighting(double value) {
        if (lighting == null) {
            Light.Distant light = new Light.Distant();
            light.setAzimuth(-135.0);
            lighting = new Lighting(light);
        }
        
        lighting.setDiffuseConstant(value * 2);
        lighting.setSpecularConstant(value);
        
        // Apply lighting effect to model
        if (value > 0.1) {
            modelGroup.setEffect(lighting);
        } else {
            modelGroup.setEffect(null);
        }
    }
    
    private void resizeScene() {
        // Resize the scene to match the container
        double width = modelContainer.getWidth();
        double height = modelContainer.getHeight();
        
        if (width > 0 && height > 0) {
            modelScene.setWidth(width);
            modelScene.setHeight(height);
        }
    }
    
    private void handleMousePressed(MouseEvent event) {
        mouseOldX = event.getSceneX();
        mouseOldY = event.getSceneY();
        
        // Stop automatic rotation when user interacts
        if (rotateTransition != null) {
            rotateTransition.pause();
        }
    }
    
    private void handleMouseDragged(MouseEvent event) {
        mousePosX = event.getSceneX();
        mousePosY = event.getSceneY();
        
        double dx = mousePosX - mouseOldX;
        double dy = mousePosY - mouseOldY;
        
        // Adjust rotation based on mouse movement
        rotateY.setAngle(rotateY.getAngle() + dx * 0.2);
        rotateX.setAngle(rotateX.getAngle() - dy * 0.2);
        
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
    }
    
    private void handleScroll(ScrollEvent event) {
        double delta = event.getDeltaY() * 0.002;
        double zoom = scale.getX() + delta;
        
        // Constrain zoom level
        zoom = Math.max(0.5, Math.min(zoom, 2.0));
        
        scale.setX(zoom);
        scale.setY(zoom);
        scale.setZ(zoom);
        
        // Update slider if available
        if (zoomSlider != null) {
            zoomSlider.setValue(zoom);
        }
    }
    
    private void resetView() {
        // Reset rotations
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);
        
        // Reset zoom
        scale.setX(1.0);
        scale.setY(1.0);
        scale.setZ(1.0);
        
        // Reset slider
        if (zoomSlider != null) {
            zoomSlider.setValue(1.0);
        }
        
        // Restart animation
        if (rotateTransition != null) {
            rotateTransition.play();
        }
    }
    
    private void setupRotation() {
        // Create a rotation animation
        rotateTransition = new RotateTransition(Duration.seconds(40), modelGroup);
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.play();
    }
    
    /**
     * Load the model asynchronously
     */
    private void loadModelAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate some loading time for demonstration
                Thread.sleep(1000);
                
                // Load the 3D model
                String modelPath = getClass().getResource("/Views/models/Green_Energy_City_0404194846_texture.glb").getPath();
                Group modelRoot = ModelLoader.loadGlbModel(modelPath);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    // Add the loaded model to the model group
                    modelGroup.getChildren().add(modelRoot);
                    
                    // Hide loading indicator
                    hideLoadingIndicator();
                    
                    System.out.println("3D Model loaded successfully");
                });
            } catch (Exception e) {
                System.err.println("Error loading 3D model: " + e.getMessage());
                e.printStackTrace();
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    // Create fallback model if loading fails
                    Group fallbackModel = ModelLoader.createPlaceholderModel();
                    modelGroup.getChildren().add(fallbackModel);
                    
                    // Hide loading indicator
                    hideLoadingIndicator();
                    
                    // Show error message
                    showErrorMessage("Failed to load 3D model");
                });
            }
        }, executor);
    }
    
    private void showErrorMessage(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-background-color: rgba(255, 0, 0, 0.7); -fx-padding: 10px; " +
                           "-fx-text-fill: white; -fx-background-radius: 5px;");
        
        StackPane errorPane = new StackPane(errorLabel);
        AnchorPane.setTopAnchor(errorPane, 10.0);
        AnchorPane.setRightAnchor(errorPane, 10.0);
        
        modelContainer.getChildren().add(errorPane);
        
        // Auto-hide after 5 seconds
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(5), errorPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setOnFinished(e -> modelContainer.getChildren().remove(errorPane));
        fadeOut.play();
    }
    
    public void onClose() {
        // Clean up resources
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}

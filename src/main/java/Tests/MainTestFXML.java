package Tests;

import Controllers.BasefrontController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class MainTestFXML extends Application {
    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean maximized = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the basefront FXML file
            URL baseFxmlUrl = getClass().getResource("/Views/basefront.fxml");
            if (baseFxmlUrl == null) {
                throw new IOException("Cannot find /Views/basefront.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(baseFxmlUrl);
            Parent root = loader.load();
            
            // Get the controller to navigate to the events page
            BasefrontController controller = loader.getController();
            
            // Create a root container with a custom title bar
            BorderPane mainContainer = new BorderPane();
            mainContainer.getStyleClass().add("transparent-background");
            
            // Create custom title bar
            HBox titleBar = createTitleBar(primaryStage);
            mainContainer.setTop(titleBar);
            
            // Create a container for the application content
            StackPane contentContainer = new StackPane();
            contentContainer.getStyleClass().add("application-content");
            contentContainer.getChildren().add(root);
            mainContainer.setCenter(contentContainer);
            
            // Set up the scene
            Scene scene = new Scene(mainContainer);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            
            // Add the custom CSS
            scene.getStylesheets().add(getClass().getResource("/Styles/title-bar.css").toExternalForm());
            
            // Store the controller in the scene's user data for child controllers to access
            scene.setUserData(controller);
            
            // Configure the stage
            primaryStage.initStyle(StageStyle.TRANSPARENT); // Use TRANSPARENT instead of UNDECORATED for rounded corners
            primaryStage.setTitle("ArtXCape");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            // Navigate to the events page using the correct method
            controller.navigateTo("/Views/home.fxml");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private HBox createTitleBar(Stage stage) {
        HBox titleBar = new HBox();
        titleBar.getStyleClass().add("custom-title-bar");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(8, 15, 8, 15));
        titleBar.setPrefHeight(38);
        
        // Try to set titlebar.png as background
        try {
            Image titleBarImage = new Image(getClass().getResourceAsStream("/Assets/titlebar.png"));
            BackgroundSize backgroundSize = new BackgroundSize(
                    1.0, 
                    1.0, 
                    true, 
                    true, 
                    false, 
                    false);
            BackgroundImage backgroundImage = new BackgroundImage(
                    titleBarImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    backgroundSize);
            titleBar.setBackground(new Background(backgroundImage));
            titleBar.getStyleClass().remove("custom-title-bar");
            titleBar.getStyleClass().add("custom-title-bar-with-image");
        } catch (Exception e) {
            System.err.println("Titlebar image not found: " + e.getMessage());
            // Fallback to gradient background
            titleBar.getStyleClass().remove("custom-title-bar-with-image");
            titleBar.getStyleClass().add("custom-title-bar");
        }
        
        // App icon and title
        try {
            ImageView appIcon = new ImageView(new Image(getClass().getResourceAsStream("/Assets/titlebar.png")));
            appIcon.setFitHeight(24);
            appIcon.setFitWidth(24);
            titleBar.getChildren().add(appIcon);
        } catch (Exception e) {
            // If logo image is not found, continue without it
            System.err.println("Logo image not found: " + e.getMessage());
        }
        
        Label title = new Label("ArtXCape");
        title.getStyleClass().add("window-title");
        HBox.setMargin(title, new Insets(0, 0, 0, 10));
        titleBar.getChildren().add(title);
        
        // Spacer to push window controls to the right
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        titleBar.getChildren().add(spacer);
        
        // Window control buttons
        HBox windowControls = new HBox();
        windowControls.getStyleClass().add("title-bar-buttons");
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.setSpacing(8);
        
        Button minimizeBtn = createCircleButton("#f1c40f", e -> stage.setIconified(true));
        Button maximizeBtn = createCircleButton("#27ae60", e -> {
            if (maximized) {
                stage.setMaximized(false);
                maximized = false;
            } else {
                stage.setMaximized(true);
                maximized = true;
            }
        });
        Button closeBtn = createCircleButton("#e74c3c", e -> Platform.exit());
        
        windowControls.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
        titleBar.getChildren().add(windowControls);
        
        // Make the title bar draggable to move the window
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        titleBar.setOnMouseDragged(event -> {
            if (!maximized) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        
        // Double-click on title bar to maximize/restore
        titleBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (maximized) {
                    stage.setMaximized(false);
                    maximized = false;
                } else {
                    stage.setMaximized(true);
                    maximized = true;
                }
            }
        });
        
        return titleBar;
    }
    
    private Button createCircleButton(String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button();
        
        // Create circle shape for the button
        Circle circle = new Circle(8);
        circle.setFill(Color.web(color));
        circle.setStroke(Color.web("#00000033"));
        circle.setStrokeWidth(0.5);
        
        button.setGraphic(circle);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        button.setPrefSize(16, 16);
        button.setMinSize(16, 16);
        button.setMaxSize(16, 16);
        button.setCursor(javafx.scene.Cursor.HAND);
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            circle.setScaleX(1.1);
            circle.setScaleY(1.1);
            
            if (color.equals("#e74c3c")) { // Close button
                circle.setFill(Color.web("#c0392b"));
            }
        });
        
        button.setOnMouseExited(e -> {
            circle.setScaleX(1);
            circle.setScaleY(1);
            circle.setFill(Color.web(color));
        });
        
        button.setOnAction(handler);
        
        return button;
    }

    /**
     * Main method to run the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}

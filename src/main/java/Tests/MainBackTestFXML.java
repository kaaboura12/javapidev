package Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainBackTestFXML extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the baseback FXML file for admin dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/baseback.fxml"));
            Parent root = loader.load();
            
            // Set up the scene
            Scene scene = new Scene(root);
            
            // Add stylesheet for modern look
            scene.getStylesheets().add(String.valueOf(getClass().getResource("/Styles/modern-theme.css")));
            
            // Set the application icon (using the titlebar.png)
            try {
                Image icon = new Image(getClass().getResourceAsStream("/Assets/titlebar.png"));
                primaryStage.getIcons().add(icon);
                System.out.println("Icon loaded successfully");
            } catch (Exception e) {
                System.err.println("Error loading icon: " + e.getMessage());
            }
            
            // Add CSS class to root for styled title bar
            root.getStyleClass().add("window");
            
            // Configure the stage with enhanced styling
            primaryStage.setTitle("ArtXCope Admin Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);
            
            // For JavaFX 17 with modern style
            primaryStage.initStyle(StageStyle.DECORATED);
            
            // Apply professional touch
            scene.setFill(Color.TRANSPARENT);
            
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 
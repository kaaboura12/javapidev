package Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Test application to run the webhook test form
 */
public class WebhookTestMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the test FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/test.fxml"));
            Parent root = loader.load();
            
            // Create the scene
            Scene scene = new Scene(root);
            
            // Configure the stage
            primaryStage.setTitle("Webhook Test Application");
            primaryStage.setScene(scene);
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method to run the webhook test application
     */
    public static void main(String[] args) {
        launch(args);
    }
} 
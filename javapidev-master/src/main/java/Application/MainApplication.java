package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main layout FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/basefront.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root);

            // Add any global CSS files if needed
            if (getClass().getResource("/style/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            }

            // Set up the stage
            primaryStage.setTitle("ArtXCope");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
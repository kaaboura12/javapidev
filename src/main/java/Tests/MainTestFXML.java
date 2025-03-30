package Tests;

import Controllers.BasefrontController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainTestFXML extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the basefront FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/basefront.fxml"));
            Parent root = loader.load();
            
            // Get the controller to navigate to the events page
            BasefrontController controller = loader.getController();
            
            // Set up the scene
            Scene scene = new Scene(root);
            
            // Store the controller in the scene's user data for child controllers to access
            scene.setUserData(controller);
            
            // Configure the stage
            primaryStage.setTitle("ArtXCope");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            // Try to navigate to the events page
            // Try different possible paths (in case of case sensitivity or different naming conventions)
            String[] possiblePaths = {
                "/Views/event/listevent.fxml",
                "/Views/Events/ListEvent.fxml",
                "/Views/event/ListEvent.fxml",
                "/Views/Events/listevent.fxml"
            };
            
            boolean navigated = false;
            for (String path : possiblePaths) {
                URL url = getClass().getResource(path);
                if (url != null) {
                    controller.navigateTo(path);
                    System.out.println("Successfully navigated to: " + path);
                    navigated = true;
                    break;
                }
            }
            
            if (!navigated) {
                System.err.println("Could not find the event list view. Please check the path.");
            }
            
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

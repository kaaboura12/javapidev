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
            URL baseFxmlUrl = getClass().getResource("/Views/basefront.fxml");
            if (baseFxmlUrl == null) {
                throw new IOException("Cannot find /Views/basefront.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(baseFxmlUrl);
            Parent root = loader.load();
            
            // Get the controller to navigate to the events page
            BasefrontController controller = loader.getController();
            
            // Set up the scene
            Scene scene = new Scene(root);
            
            // Store the controller in the scene's user data for child controllers to access
            scene.setUserData(controller);
            
            // Configure the stage
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

    public static void main(String[] args) {
        launch(args);
    }
}

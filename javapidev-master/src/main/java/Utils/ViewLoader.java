package Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class ViewLoader {
    
    /**
     * Loads a view into the specified content area
     * 
     * @param contentArea The StackPane to load the view into
     * @param fxmlPath The path to the FXML file
     * @return The controller for the loaded view
     */
    public static <T> T loadView(StackPane contentArea, String fxmlPath) {
        try {
            // Clear the existing content
            contentArea.getChildren().clear();
            
            // Load the FXML file
            URL url = ViewLoader.class.getResource(fxmlPath);
            if (url == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return null;
            }
            
            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();
            
            // Add the view to the content area
            contentArea.getChildren().add(view);
            
            // Return the controller
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
            return null;
        }
    }
} 
package Views.event;

import Controllers.event.ModelController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Dmodel {

    private ModelController controller;
    private Stage stage;

    /**
     * Initialize and show the 3D model view in a new window
     */
    public void show() {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/3dmodel.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            controller = loader.getController();
            
            // Create a new stage
            stage = new Stage();
            stage.setTitle("3D Model Viewer");
            
            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Set stage properties
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Show the stage
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading 3D model view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show the 3D model viewer in the same stage
     * 
     * @param currentStage The current stage to reuse
     * @param returnToMainView Runnable to execute when returning to the main view
     */
    public void showInSameStage(Stage currentStage, Runnable returnToMainView) {
        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/3dmodel.fxml"));
            Parent root = loader.load();
            
            // Get the controller
            controller = loader.getController();
            
            // Save reference to the stage
            this.stage = currentStage;
            
            // Create back button
            controller.setupBackButton(returnToMainView);
            
            // Set the scene for the existing stage
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            
        } catch (IOException e) {
            System.err.println("Error loading 3D model view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get the controller for this view
     */
    public ModelController getController() {
        return controller;
    }
    
    /**
     * Get the stage for this view
     */
    public Stage getStage() {
        return stage;
    }
} 
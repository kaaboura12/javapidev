package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Button goToEventsBtn;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (goToEventsBtn != null) {
            goToEventsBtn.setOnAction(e -> navigateToEvents());
        }
    }
    
    private void navigateToEvents() {
        try {
            // Find the parent BasefrontController to use its navigation method
            BasefrontController parentController = findParentController();
            if (parentController != null) {
                parentController.navigateTo("/Views/event/listevent.fxml");
            } else {
                System.err.println("Could not find parent controller for navigation");
            }
        } catch (Exception e) {
            System.err.println("Error navigating to events: " + e.getMessage());
        }
    }
    
    private BasefrontController findParentController() {
        // This is a simplistic implementation that assumes the BasefrontController is registered somewhere
        // In a real application, you'd use a proper navigation service or parent-child controller relationship
        
        // First try to find it in the scene graph
        if (goToEventsBtn != null && goToEventsBtn.getScene() != null) {
            return (BasefrontController) goToEventsBtn.getScene().getUserData();
        }
        
        return null;
    }
} 
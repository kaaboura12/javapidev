package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private Button goToEventsBtn;
    
    @FXML
    private Hyperlink goToForumLink;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (goToEventsBtn != null) {
            goToEventsBtn.setOnAction(e -> navigateToEvents());
        }
        
        if (goToForumLink != null) {
            goToForumLink.setOnAction(e -> navigateToForum());
        }
        
        // Find all buttons by walking the scene graph and add appropriate handlers
        if (goToEventsBtn != null && goToEventsBtn.getScene() != null) {
            goToEventsBtn.getScene().getRoot().lookupAll(".button").forEach(node -> {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    String text = button.getText();
                    
                    if (text != null) {
                        if (text.equals("Join Community") || text.equals("Get Started Now")) {
                            button.setOnAction(e -> handleJoinCommunity());
                        }
                    }
                }
            });
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
    
    private void navigateToForum() {
        try {
            // Find the parent BasefrontController to use its navigation method
            BasefrontController parentController = findParentController();
            if (parentController != null) {
                parentController.navigateTo("/Views/forum/post.fxml");
            } else {
                System.err.println("Could not find parent controller for navigation");
            }
        } catch (Exception e) {
            System.err.println("Error navigating to forum: " + e.getMessage());
        }
    }
    
    private void handleJoinCommunity() {
        try {
            // This could navigate to a sign-up page or show a registration dialog
            System.out.println("Join Community button clicked");
            
            BasefrontController parentController = findParentController();
            if (parentController != null) {
                // For now, navigate to events as well (or you could create a new register.fxml)
                parentController.navigateTo("/Views/event/listevent.fxml");
            }
        } catch (Exception e) {
            System.err.println("Error handling join community: " + e.getMessage());
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
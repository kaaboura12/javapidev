package Views.event;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Controllers.event.eventDetailsController;

public class Eventdetails {
    
    private Stage stage;
    
    public Eventdetails() {
        stage = new Stage();
    }
    
    public void show(int eventId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/event/eventdetails.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the event ID
            eventDetailsController controller = loader.getController();
            controller.setEventId(eventId);
            
            // Set up the stage
            stage.setTitle("Event Details");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading event details view: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Stage getStage() {
        return stage;
    }
} 
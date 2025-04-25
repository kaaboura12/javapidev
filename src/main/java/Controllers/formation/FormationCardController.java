package Controllers.formation;

import Models.Formation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class FormationCardController {
    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Text descriptionText;
    @FXML private Label dateRangeLabel;
    @FXML private Label priceLabel;
    @FXML private Label participantsLabel;
    @FXML private ImageView formationImage;
    @FXML private Label durationLabel;
    @FXML private Button viewDetailsButton;
    
    private Formation formation;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public void setFormation(Formation formation) {
        this.formation = formation;
        
        // Update UI elements with formation data
        titleLabel.setText(formation.getTitre());
        categoryLabel.setText(formation.getCategorie().getNom());
        descriptionText.setText(formation.getDescription());
        
        // Format dates
        String dateRange = formation.getDateDebut().format(DATE_FORMAT) + " - " + 
                          formation.getDateFin().format(DATE_FORMAT);
        dateRangeLabel.setText(dateRange);
        
        // Calculate and display duration
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            formation.getDateDebut(), formation.getDateFin()) + 1;
        durationLabel.setText(days + " Days");
        
        // Display price with dollar sign
        priceLabel.setText("$" + String.format("%.2f", formation.getPrix()));
        
        // Display participants info
        participantsLabel.setText(formation.getNbrpart() + " spots available");

        // Load video thumbnail
        loadThumbnail(formation.getVideo());
    }

    private void loadThumbnail(String videoUrl) {
        if (formationImage == null) return;

        try {
            if (videoUrl != null && !videoUrl.isEmpty() && videoUrl.contains("youtube")) {
                String videoId = extractYoutubeVideoId(videoUrl);
                if (videoId != null) {
                    String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                    Image image = new Image(thumbnailUrl, true);
                    formationImage.setImage(image);
                    return;
                }
            }
            // If no video URL or not a YouTube video, load default image
            Image defaultImage = new Image(getClass().getResourceAsStream("/Assets/video_placeholder.jpg"));
            formationImage.setImage(defaultImage);
            
        } catch (Exception e) {
            System.err.println("Error loading thumbnail: " + e.getMessage());
            e.printStackTrace();
            try {
                // Last resort - try loading default image
                Image defaultImage = new Image(getClass().getResourceAsStream("/Assets/video_placeholder.jpg"));
                formationImage.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Could not load default image: " + ex.getMessage());
            }
        }
    }

    private String extractYoutubeVideoId(String url) {
        if (url == null || url.isEmpty()) return null;
        
        String videoId = null;
        try {
            if (url.contains("youtube.com/watch")) {
                int start = url.indexOf("v=") + 2;
                if (start >= 2) {
                    int end = url.indexOf("&", start);
                    videoId = (end == -1) ? url.substring(start) : url.substring(start, end);
                }
            } else if (url.contains("youtu.be/")) {
                int start = url.indexOf("youtu.be/") + 9;
                if (start >= 9) {
                    int end = url.indexOf("?", start);
                    videoId = (end == -1) ? url.substring(start) : url.substring(start, end);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting YouTube ID: " + e.getMessage());
        }
        return videoId;
    }
    
    @FXML
    private void handleViewDetails() {
        if (formation == null) {
            System.err.println("No formation data available");
            return;
        }
        
        try {
            // Load the formation detail view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/formation/formation_detail.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the formation data
            FormationDetailFrontController controller = loader.getController();
            controller.setFormation(formation);
            
            // Create a new stage and show it
            Stage stage = new Stage();
            stage.setTitle(formation.getTitre());
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading formation details: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

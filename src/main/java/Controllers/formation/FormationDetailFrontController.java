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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.time.format.DateTimeFormatter;

public class FormationDetailFrontController {
    @FXML private Label titleLabel;
    @FXML private Label categorieLabel;
    @FXML private Text descriptionText;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label nbrpartLabel;
    @FXML private Label prixLabel;
    @FXML private Label startDateLabel; // Add this field
    @FXML private StackPane videoContainer; // New container for the thumbnail and play button
    @FXML private ImageView videoThumbnail; // Thumbnail image
    @FXML private Button playVideoButton; // Play button
    
    private Formation formation;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
    
    @FXML
    public void initialize() {
        // Set up play button click handler if it exists
        if (playVideoButton != null) {
            playVideoButton.setOnAction(e -> openVideoPlayer());
        }
    }
    
    public void setFormation(Formation formation) {
        this.formation = formation;
        
        if (formation != null) {
            populateUI();
        } else {
            System.err.println("Formation data is null");
        }
    }
    
    private void populateUI() {
        titleLabel.setText(formation.getTitre());
        categorieLabel.setText(formation.getCategorie().getNom());
        descriptionText.setText(formation.getDescription());
        
        // Add start date display
        startDateLabel.setText("Starting " + formation.getDateDebut().format(
            DateTimeFormatter.ofPattern("d MMM yyyy")
        ));
        
        // Format dates
        dateDebutLabel.setText(formation.getDateDebut().format(DATE_FORMATTER));
        dateFinLabel.setText(formation.getDateFin().format(DATE_FORMATTER));
        
        // Other details
        nbrpartLabel.setText("Maximum participants: " + formation.getNbrpart());
        prixLabel.setText("Price: $" + String.format("%.2f", formation.getPrix()));
        
        // Load video thumbnail if available
        String videoUrl = formation.getVideo();
        
        // Ensure videoContainer and videoThumbnail are visible
        if (videoContainer != null) {
            videoContainer.setVisible(true);
        }
        if (videoThumbnail != null) {
            videoThumbnail.setVisible(true);
        }
        
        if (videoUrl != null && !videoUrl.isEmpty()) {
            setupVideoThumbnail(videoUrl);
            if (playVideoButton != null) {
                playVideoButton.setVisible(true);
            }
        } else {
            loadFallbackContent();
        }
    }
    
    private void setupVideoThumbnail(String videoUrl) {
        try {
            if (videoContainer == null || videoThumbnail == null) {
                System.err.println("Video container or thumbnail is null");
                return;
            }
            
            String thumbnailUrl = "/Assets/video_placeholder.jpg";
            
            if (videoUrl.contains("youtube")) {
                String videoId = extractYoutubeVideoId(videoUrl);
                if (videoId != null) {
                    thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                    try {
                        Image image = new Image(thumbnailUrl, true);
                        videoThumbnail.setImage(image);
                        videoThumbnail.setFitWidth(640); // Larger size for detail view
                        videoThumbnail.setFitHeight(360);
                        videoThumbnail.setPreserveRatio(true);
                        return;
                    } catch (Exception e) {
                        System.err.println("Error loading YouTube thumbnail: " + e.getMessage());
                    }
                }
            }
            
            // Load default image if YouTube thumbnail fails or is not available
            Image defaultImage = new Image(getClass().getResourceAsStream("/Assets/video_placeholder.jpg"));
            videoThumbnail.setImage(defaultImage);
            
        } catch (Exception e) {
            System.err.println("Error setting up video thumbnail: " + e.getMessage());
            e.printStackTrace();
            loadFallbackContent();
        }
    }
    
    private void openVideoInBrowser() {
        if (formation != null && formation.getVideo() != null && !formation.getVideo().isEmpty()) {
            try {
                Desktop.getDesktop().browse(new URI(formation.getVideo()));
            } catch (Exception e) {
                System.err.println("Error opening video in browser: " + e.getMessage());
                showError("Could not open video: " + e.getMessage());
            }
        } else {
            showError("No video URL available");
        }
    }
    
    @FXML
    private void openVideoPlayer() {
        if (formation != null && formation.getVideo() != null && !formation.getVideo().isEmpty()) {
            try {
                // Load the video player view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/formation/video_player.fxml"));
                Parent root = loader.load();
                
                // Get the controller and set up the video
                VideoPlayerController controller = loader.getController();
                controller.loadVideo(formation.getVideo());
                
                // Create and configure the stage
                Stage stage = new Stage();
                stage.setTitle(formation.getTitre() + " - Video");
                stage.setScene(new Scene(root));
                stage.setMinWidth(800);
                stage.setMinHeight(600);
                stage.initModality(Modality.APPLICATION_MODAL);
                
                // Show the window
                stage.show();
                
            } catch (IOException e) {
                System.err.println("Error opening video player: " + e.getMessage());
                e.printStackTrace();
                showError("Could not open video player: " + e.getMessage());
                
                // Fallback to browser if video player fails
                openVideoInBrowser();
            }
        } else {
            showError("No video URL available");
        }
    }
    
    private String extractYoutubeVideoId(String url) {
        String videoId = null;
        try {
            if (url.contains("youtube.com/watch")) {
                // Format: https://www.youtube.com/watch?v=VIDEO_ID
                int start = url.indexOf("v=") + 2;
                if (start >= 2) { // Make sure "v=" was found
                    int end = url.indexOf("&", start);
                    if (end == -1) {
                        videoId = url.substring(start);
                    } else {
                        videoId = url.substring(start, end);
                    }
                }
            } else if (url.contains("youtu.be/")) {
                // Format: https://youtu.be/VIDEO_ID
                int start = url.indexOf("youtu.be/") + 9;
                if (start >= 9) { // Make sure "youtu.be/" was found
                    int end = url.indexOf("?", start);
                    if (end == -1) {
                        videoId = url.substring(start);
                    } else {
                        videoId = url.substring(start, end);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting YouTube ID: " + e.getMessage());
        }
        return videoId;
    }
    
    private void loadFallbackContent() {
        if (videoContainer != null) {
            videoContainer.setVisible(true);
            
            // If we have a thumbnail ImageView, set a placeholder image
            if (videoThumbnail != null) {
                try {
                    videoThumbnail.setImage(new Image(getClass().getResourceAsStream("/Assets/no_video.jpg")));
                } catch (Exception e) {
                    System.err.println("Could not load fallback image: " + e.getMessage());
                }
            }
            
            // Hide the play button if there's no video
            if (playVideoButton != null) {
                playVideoButton.setVisible(false);
            }
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/formation/formation_registration.fxml"));
            Parent root = loader.load();
            
            FormationRegistrationController controller = loader.getController();
            controller.setFormation(formation);
            
            Stage stage = new Stage();
            stage.setTitle("Formation Registration");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String message) {
        System.err.println("Video Error: " + message);
    }
}
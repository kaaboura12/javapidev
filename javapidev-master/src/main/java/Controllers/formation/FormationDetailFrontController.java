package Controllers.formation;

import Models.Formation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import Models.Formation;
import Models.Inscription;
import Services.InscriptionService;




public class FormationDetailFrontController {
    @FXML private Label titleLabel;
    @FXML private Label categorieLabel;
    @FXML private Text descriptionText;
    @FXML private Label dateDebutLabel;
    @FXML private Label dateFinLabel;
    @FXML private Label nbrpartLabel;
    @FXML private Label prixLabel;
    @FXML private StackPane videoContainer; // New container for the thumbnail and play button
    @FXML private ImageView videoThumbnail; // Thumbnail image
    @FXML private Button playVideoButton; // Play button
    
    private Formation formation;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");
    
    @FXML
    public void initialize() {
        // Set up play button click handler if it exists
        if (playVideoButton != null) {
            playVideoButton.setOnAction(e -> openVideoInBrowser());
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
        
        // Format dates
        dateDebutLabel.setText("Start Date: " + formation.getDateDebut().format(DATE_FORMATTER));
        dateFinLabel.setText("End Date: " + formation.getDateFin().format(DATE_FORMATTER));
        
        // Other details
        nbrpartLabel.setText("Maximum participants: " + formation.getNbrpart());
        prixLabel.setText("Price: $" + String.format("%.2f", formation.getPrix()));
        
        // Load video thumbnail if available
        String videoUrl = formation.getVideo();
        System.out.println("Video URL: " + videoUrl);
        
        if (videoUrl != null && !videoUrl.isEmpty()) {
            setupVideoThumbnail(videoUrl);
        } else {
            loadFallbackContent();
        }
    }
    
    private void setupVideoThumbnail(String videoUrl) {
        try {
            // Make sure our container and thumbnail elements exist
            if (videoContainer == null || videoThumbnail == null) {
                System.err.println("Video container or thumbnail is null");
                return;
            }
            
            // Set a default thumbnail or try to get a YouTube thumbnail
            String thumbnailUrl = "/Assets/video_placeholder.jpg"; // Default placeholder
            
            if (videoUrl.contains("youtube")) {
                String videoId = extractYoutubeVideoId(videoUrl);
                if (videoId != null) {
                    // Use high quality YouTube thumbnail
                    thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                }
            }
            
            // Load the thumbnail
            try {
                // Try first as an external URL
                videoThumbnail.setImage(new Image(thumbnailUrl));
            } catch (Exception e) {
                // If that fails, try as a resource
                videoThumbnail.setImage(new Image(getClass().getResourceAsStream(thumbnailUrl)));
            }
            
            // Make everything visible
            videoContainer.setVisible(true);
            videoThumbnail.setVisible(true);
            if (playVideoButton != null) {
                playVideoButton.setVisible(true);
            }
            
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
            int testUserId =4;
            System.out.println("test");
            InscriptionService inscriptionService= new InscriptionService();
           if (inscriptionService.checkIfAlreadyRegistered(testUserId, formation.getId())) {
                showAlert("Already Registered", "You are already registered for this formation.", Alert.AlertType.WARNING);
                return;
            }

            // Check if spots are available
            if (formation.getNbrpart() <= 0) {
                showAlert("Registration Failed", "No more spots available for this formation.", Alert.AlertType.ERROR);
                return;
            }

            Inscription inscription = new Inscription(
                    formation.getId(),
                    testUserId,
                    LocalDate.now(),
                    "pending",
                    LocalDate.now()
            );

            inscriptionService.insert(inscription);

            showAlert("Success", "Registration successful!", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (SQLException e) {
            if (e.getMessage().contains("No more spots available")) {
                showAlert("Registration Failed", "No more spots available for this formation.", Alert.AlertType.ERROR);
            } else {
                showAlert("Error", "Registration failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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
package Controllers.formation;

import Services.TranscriptionService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VideoPlayerController {

    @FXML private MediaView mediaView;
    @FXML private Button playButton;
    @FXML private Slider timeSlider;
    @FXML private Label timeLabel;
    @FXML private Button muteButton;
    @FXML private Slider volumeSlider;
    @FXML private Button fullscreenButton;
    @FXML private Button closeButton;
    
    @FXML private Button transcribeButton;
    @FXML private Button downloadTranscriptButton;
    @FXML private Button copyTranscriptButton;
    @FXML private ProgressBar transcriptionProgress;
    @FXML private Label transcriptionStatusLabel;
    @FXML private TextArea transcriptionTextArea;
    
    private MediaPlayer mediaPlayer;
    private String videoUrl;
    private String localVideoPath;
    private boolean isPlaying = false;
    private boolean isMuted = false;
    private boolean isFullscreen = false;
    private TranscriptionService transcriptionService;
    
    public void initialize() {
        // Initialize transcription service
        transcriptionService = new TranscriptionService();
        
        // Set up volume slider
        volumeSlider.setValue(100);
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });
    }
    
    /**
     * Load and prepare video for playback
     * @param videoUrl URL or local path of the video to play
     */
    public void loadVideo(String videoUrl) {
        this.videoUrl = videoUrl;
        
        try {
            // Determine if local file or URL
            Media media;
            if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
                // For web URLs - properly format for Media player
                if (videoUrl.contains("youtube")) {
                    // YouTube videos can't be directly played in JavaFX MediaPlayer
                    showAlert(Alert.AlertType.WARNING, "YouTube Videos", 
                          "YouTube Playback Limited", 
                          "YouTube videos cannot be played directly in this player. " +
                          "However, you can still transcribe the video by clicking the 'Transcribe Video' button.");
                    
                    // Load YouTube thumbnail if possible
                    setupYouTubeThumbnail(videoUrl);
                    return;
                } else {
                    // For direct media URLs
                    media = new Media(videoUrl);
                }
            } else {
                // For local files
                this.localVideoPath = videoUrl;
                File videoFile = new File(videoUrl);
                media = new Media(videoFile.toURI().toString());
            }
            
            // Create media player
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }
            
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            // Setup responsive sizing
            DoubleProperty mvw = mediaView.fitWidthProperty();
            DoubleProperty mvh = mediaView.fitHeightProperty();
            mvw.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width").multiply(0.9));
            mvh.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").multiply(0.7));
            
            // Handle media errors
            mediaPlayer.setOnError(() -> {
                MediaException error = mediaPlayer.getError();
                System.err.println("Media error: " + error.getMessage());
                showAlert(Alert.AlertType.ERROR, "Media Error", 
                        "Failed to play the video", 
                        "Error: " + error.getMessage());
            });
            
            // Setup time tracking
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!timeSlider.isValueChanging() && newTime != null && mediaPlayer.getTotalDuration() != null) {
                    double total = mediaPlayer.getTotalDuration().toSeconds();
                    double current = newTime.toSeconds();
                    if (total > 0) {
                        timeSlider.setValue((current / total) * 100.0);
                        
                        // Update time label
                        updateTimeLabel(current, total);
                    }
                }
            });
            
            timeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (timeSlider.isValueChanging() && mediaPlayer.getTotalDuration() != null) {
                    double total = mediaPlayer.getTotalDuration().toSeconds();
                    double seekTime = total * (newVal.doubleValue() / 100.0);
                    mediaPlayer.seek(Duration.seconds(seekTime));
                }
            });
            
            // Setup player ready actions
            mediaPlayer.setOnReady(() -> {
                // Update duration display
                double duration = mediaPlayer.getTotalDuration().toSeconds();
                updateTimeLabel(0, duration);
                
                // Enable controls
                playButton.setDisable(false);
                timeSlider.setDisable(false);
                muteButton.setDisable(false);
                volumeSlider.setDisable(false);
                fullscreenButton.setDisable(false);
            });
            
            // Set initial button states
            playButton.setText("Play");
            isPlaying = false;
            
            // Set volume
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            
        } catch (Exception e) {
            System.err.println("Error loading video: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load video", 
                    "Could not load the video: " + e.getMessage());
        }
    }
    
    private void setupYouTubeThumbnail(String youtubeUrl) {
        try {
            String videoId = extractYoutubeVideoId(youtubeUrl);
            if (videoId != null) {
                // Create a YouTube thumbnail URL
                String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
                
                // Create an ImageView to display in place of the mediaView
                ImageView thumbnailView = new ImageView(new Image(thumbnailUrl, true));
                thumbnailView.setPreserveRatio(true);
                thumbnailView.setFitWidth(mediaView.getFitWidth());
                thumbnailView.setFitHeight(mediaView.getFitHeight());
                
                // Get the parent of the mediaView - correctly handle parent type
                if (mediaView.getParent() != null) {
                    // Replace mediaView with thumbnailView in the parent layout
                    if (mediaView.getParent() instanceof StackPane) {
                        StackPane parent = (StackPane) mediaView.getParent();
                        parent.getChildren().clear();
                        parent.getChildren().add(thumbnailView);
                        
                        // Add play button that opens the video in browser
                        Button ytPlayButton = new Button("Open in Browser");
                        ytPlayButton.getStyleClass().add("youtube-play-button");
                        ytPlayButton.setOnAction(e -> {
                            try {
                                Desktop.getDesktop().browse(new URI(youtubeUrl));
                            } catch (Exception ex) {
                                showAlert(Alert.AlertType.ERROR, "Error", 
                                        "Could not open browser", ex.getMessage());
                            }
                        });
                        parent.getChildren().add(ytPlayButton);
                    } else if (mediaView.getParent() instanceof VBox) {
                        // For VBox parent
                        VBox parent = (VBox) mediaView.getParent();
                        int index = parent.getChildren().indexOf(mediaView);
                        if (index >= 0) {
                            // Replace mediaView with thumbnailView at the same position
                            parent.getChildren().remove(mediaView);
                            parent.getChildren().add(index, thumbnailView);
                            
                            // Create a container for the thumbnail and button
                            StackPane thumbnailContainer = new StackPane();
                            thumbnailContainer.getChildren().add(thumbnailView);
                            
                            // Add play button that opens the video in browser
                            Button ytPlayButton = new Button("Open in Browser");
                            ytPlayButton.getStyleClass().add("youtube-play-button");
                            ytPlayButton.setOnAction(e -> {
                                try {
                                    Desktop.getDesktop().browse(new URI(youtubeUrl));
                                } catch (Exception ex) {
                                    showAlert(Alert.AlertType.ERROR, "Error", 
                                            "Could not open browser", ex.getMessage());
                                }
                            });
                            thumbnailContainer.getChildren().add(ytPlayButton);
                            
                            // Replace the thumbnailView with our container
                            parent.getChildren().remove(thumbnailView);
                            parent.getChildren().add(index, thumbnailContainer);
                        }
                    } else {
                        System.err.println("Unsupported parent container type: " + mediaView.getParent().getClass().getName());
                    }
                }
                
                // Disable playback controls since we can't play YouTube
                playButton.setDisable(true);
                timeSlider.setDisable(true);
                muteButton.setDisable(true);
                volumeSlider.setDisable(true);
                
                // But keep the transcribe button enabled
                transcribeButton.setDisable(false);
            }
        } catch (Exception e) {
            System.err.println("Error setting up YouTube thumbnail: " + e.getMessage());
            e.printStackTrace();
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
    
    @FXML
    private void togglePlayback() {
        if (mediaPlayer == null) return;
        
        if (isPlaying) {
            mediaPlayer.pause();
            playButton.setText("Play");
            isPlaying = false;
        } else {
            mediaPlayer.play();
            playButton.setText("Pause");
            isPlaying = true;
        }
    }
    
    @FXML
    private void toggleMute() {
        if (mediaPlayer == null) return;
        
        isMuted = !isMuted;
        mediaPlayer.setMute(isMuted);
        muteButton.setText(isMuted ? "Unmute" : "Mute");
    }
    
    @FXML
    private void toggleFullscreen() {
        if (mediaPlayer == null) return;
        
        Stage stage = (Stage) mediaView.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
        isFullscreen = stage.isFullScreen();
        fullscreenButton.setText(isFullscreen ? "Exit Fullscreen" : "Fullscreen");
    }
    
    @FXML
    private void closePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        
        Stage stage = (Stage) mediaView.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void transcribeVideo() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Video Loaded", 
                    "Please load a video before attempting to transcribe.");
            return;
        }
        
        // Reset UI
        transcriptionTextArea.clear();
        transcriptionProgress.setProgress(0);
        transcriptionProgress.setVisible(true);
        transcriptionStatusLabel.setText("Starting transcription...");
        transcriptionStatusLabel.setVisible(true);
        transcribeButton.setDisable(true);
        downloadTranscriptButton.setDisable(true);
        copyTranscriptButton.setDisable(true);
        
        // Determine video type and use appropriate transcription method
        if (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be")) {
            // For YouTube videos - use the specialized YouTube transcription method
            transcriptionService.transcribeYouTubeVideo(
                    videoUrl,
                    this::updateTranscriptionProgress,
                    this::handleTranscriptionResult,
                    this::handleTranscriptionError
            );
        } else if (localVideoPath != null) {
            // For local files
            transcriptionService.transcribeLocalFile(
                    localVideoPath,
                    this::updateTranscriptionProgress,
                    this::handleTranscriptionResult,
                    this::handleTranscriptionError
            );
        } else if (videoUrl.startsWith("http")) {
            // For other remote URLs
            transcriptionService.transcribeVideoUrl(
                    videoUrl,
                    this::updateTranscriptionProgress,
                    this::handleTranscriptionResult,
                    this::handleTranscriptionError
            );
        } else {
            handleTranscriptionError("Unsupported video source");
        }
    }
    
    @FXML
    private void downloadTranscript() {
        String transcription = transcriptionTextArea.getText();
        if (transcription == null || transcription.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Transcription Available", 
                    "There is no transcription available to download.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Transcription");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialFileName("video_transcription.txt");
        
        // Show save dialog
        File file = fileChooser.showSaveDialog(mediaView.getScene().getWindow());
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(transcription.getBytes());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Transcription Saved", 
                        "The transcription has been saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to Save Transcription", 
                        "An error occurred while saving the transcription: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void copyTranscript() {
        String transcription = transcriptionTextArea.getText();
        if (transcription == null || transcription.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No Transcription Available", 
                    "There is no transcription available to copy.");
            return;
        }
        
        // Copy to clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(transcription);
        clipboard.setContent(content);
        
        // Show confirmation
        transcriptionStatusLabel.setText("Transcription copied to clipboard!");
        transcriptionStatusLabel.setVisible(true);
        
        // Hide confirmation after a few seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> transcriptionStatusLabel.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void updateTimeLabel(double currentTime, double totalTime) {
        String current = formatTime(currentTime);
        String total = formatTime(totalTime);
        timeLabel.setText(current + " / " + total);
    }
    
    private String formatTime(double seconds) {
        int minutes = (int) (seconds / 60);
        int remainingSeconds = (int) (seconds % 60);
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
    
    private void updateTranscriptionProgress(int progress) {
        Platform.runLater(() -> {
            transcriptionProgress.setProgress(progress / 100.0);
            if (progress < 100) {
                transcriptionStatusLabel.setText("Transcribing: " + progress + "%");
            } else {
                transcriptionStatusLabel.setText("Transcription complete!");
            }
        });
    }
    
    private void handleTranscriptionResult(String transcription) {
        Platform.runLater(() -> {
            transcriptionTextArea.setText(transcription);
            transcriptionProgress.setProgress(1.0);
            transcriptionStatusLabel.setText("Transcription complete!");
            transcribeButton.setDisable(false);
            downloadTranscriptButton.setDisable(false);
            copyTranscriptButton.setDisable(false);
        });
    }
    
    private void handleTranscriptionError(String error) {
        Platform.runLater(() -> {
            transcriptionStatusLabel.setText("Error: " + error);
            transcriptionProgress.setVisible(false);
            transcribeButton.setDisable(false);
            
            showAlert(Alert.AlertType.ERROR, "Transcription Error", "Failed to Transcribe Video", 
                    error);
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
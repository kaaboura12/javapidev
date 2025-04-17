package Views.chatbot;

import Controllers.chatbot.ChatbotController;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Chatbot implements Initializable {
    
    @FXML
    private ScrollPane chatScrollPane;
    
    @FXML
    private VBox chatMessagesContainer;
    
    @FXML
    private TextField messageInput;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private HBox typingIndicator;
    
    // Suggestion chips
    @FXML
    private Button eventIdeasBtn;
    
    @FXML
    private Button venueRecommendationsBtn;
    
    @FXML
    private Button planningTipsBtn;
    
    private ChatbotController controller;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        controller = new ChatbotController(this);
        
        // Set up event handlers
        sendButton.setOnAction(event -> sendMessage());
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
        
        // Setup window appearance and make it draggable
        setupWindowAppearance();
        
        // Setup scrolling behavior to auto-scroll to bottom when new messages arrive
        chatScrollPane.vvalueProperty().bind(chatMessagesContainer.heightProperty());
        
        // Hide typing indicator initially
        typingIndicator.setVisible(false);
        typingIndicator.setOpacity(0);
        
        // Set up suggestion chips directly
        setupSuggestionChipsDirectly();
    }
    
    /**
     * Setup window appearance and make the window draggable by the header
     */
    private void setupWindowAppearance() {
        Platform.runLater(() -> {
            Scene scene = messageInput.getScene();
            if (scene != null) {
                Stage stage = (Stage) scene.getWindow();
                // Make window draggable from anywhere in the header
                scene.lookup(".header-container").setOnMousePressed(event -> {
                    scene.getRoot().setOnMouseDragged(e -> {
                        stage.setX(e.getScreenX() - event.getSceneX());
                        stage.setY(e.getScreenY() - event.getSceneY());
                    });
                });
                scene.lookup(".header-container").setOnMouseReleased(event -> {
                    scene.getRoot().setOnMouseDragged(null);
                });
            }
        });
    }
    
    /**
     * Set up suggestion chips directly using the injected references
     */
    private void setupSuggestionChipsDirectly() {
        if (eventIdeasBtn != null) {
            eventIdeasBtn.setOnAction(e -> {
                messageInput.setText(eventIdeasBtn.getText());
                sendMessage();
            });
        }
        
        if (venueRecommendationsBtn != null) {
            venueRecommendationsBtn.setOnAction(e -> {
                messageInput.setText(venueRecommendationsBtn.getText());
                sendMessage();
            });
        }
        
        if (planningTipsBtn != null) {
            planningTipsBtn.setOnAction(e -> {
                messageInput.setText(planningTipsBtn.getText());
                sendMessage();
            });
        }
    }
    
    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            // Show typing indicator with delay before actual response
            showTypingIndicator();
            
            // Send message to controller
            controller.handleUserMessage(message);
            
            // Clear input
            messageInput.clear();
        }
    }
    
    public VBox getChatMessagesContainer() {
        return chatMessagesContainer;
    }
    
    public void showTypingIndicator() {
        typingIndicator.setVisible(true);
        
        // Create fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), typingIndicator);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Create blinking animation for the dots
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        // Add keyframes to animate each dot
        typingIndicator.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox indicatorBox = (HBox) node;
                for (int i = 0; i < indicatorBox.getChildren().size(); i++) {
                    int index = i;
                    KeyFrame kf1 = new KeyFrame(Duration.millis(300 * index), e -> 
                        indicatorBox.getChildren().get(index).setOpacity(0.4));
                    KeyFrame kf2 = new KeyFrame(Duration.millis(300 * index + 150), e -> 
                        indicatorBox.getChildren().get(index).setOpacity(1.0));
                    timeline.getKeyFrames().addAll(kf1, kf2);
                }
            }
        });
        
        timeline.play();
        
        // Hide and stop animation after a delay (this will be overridden when the actual response arrives)
        PauseTransition delay = new PauseTransition(Duration.seconds(8));
        delay.setOnFinished(event -> hideTypingIndicator(timeline));
        delay.play();
    }
    
    public void hideTypingIndicator(Timeline timeline) {
        if (timeline != null) {
            timeline.stop();
        }
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), typingIndicator);
        fadeOut.setFromValue(typingIndicator.getOpacity());
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> typingIndicator.setVisible(false));
        fadeOut.play();
    }
} 
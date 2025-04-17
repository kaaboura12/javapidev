package Controllers.chatbot;

import Views.chatbot.Chatbot;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ChatbotController {
    
    private final Chatbot view;
    private static final String API_KEY = "AIzaSyBsHnbOmSNveC-KQVGc6WOMvbpSGFfyqgI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private Timeline typingTimeline;
    
    public ChatbotController(Chatbot view) {
        this.view = view;
        
        // Add a welcome message after a short delay
        Platform.runLater(() -> {
            // Short delay to ensure UI is ready
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Add welcome message
            addBotMessage("Hello! I'm your AI assistant for events. I can help you with event planning, venue suggestions, and more. How can I assist you today?");
        });
    }
    
    public void handleUserMessage(String message) {
        // Add user message to the chat
        addUserMessage(message);
        
        // Call Gemini API asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate network delay for better UX (at least 1 second of typing indicator)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                String response = callGeminiApi(message);
                
                Platform.runLater(() -> {
                    // Hide typing indicator
                    view.hideTypingIndicator(typingTimeline);
                    
                    // Add response
                    addBotMessage(response);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    // Hide typing indicator
                    view.hideTypingIndicator(typingTimeline);
                    
                    // Show error
                    addBotMessage("I apologize, but I encountered an error: " + e.getMessage());
                });
            }
        });
    }
    
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("user-message");
        textFlow.setPadding(new Insets(10));
        
        Text text = new Text(message);
        textFlow.getChildren().add(text);
        
        messageBox.getChildren().add(textFlow);
        view.getChatMessagesContainer().getChildren().add(messageBox);
    }
    
    private void addBotMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("bot-message");
        textFlow.setPadding(new Insets(10));
        
        // Process message for markdown-like formatting
        String processedMessage = processMarkdown(message);
        
        // Split by lines to handle newlines properly
        String[] lines = processedMessage.split("\n");
        for (int i = 0; i < lines.length; i++) {
            Text text = new Text(lines[i]);
            textFlow.getChildren().add(text);
            
            // Add line break for all but the last line
            if (i < lines.length - 1) {
                textFlow.getChildren().add(new Text("\n"));
            }
        }
        
        messageBox.getChildren().add(textFlow);
        view.getChatMessagesContainer().getChildren().add(messageBox);
    }
    
    private String processMarkdown(String message) {
        // Simple markdown processing - could be extended for more complex formatting
        return message.replace("\\n", "\n");
    }
    
    private String callGeminiApi(String userMessage) throws Exception {
        URL url = new URL(API_URL + "?key=" + API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        // Create a context relevant to events
        String requestBody = String.format("""
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "You are an event planning assistant. Focus your responses on helping with event organization, venue suggestions, invitations, and other event-related questions. The user wants help with events when they ask: %s"
                    }
                  ]
                }
              ],
              "generationConfig": {
                "temperature": 0.7,
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 1024
              }
            }
            """, userMessage.replace("\"", "\\\""));
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // Parse the response to extract text content
                // Simple parsing, could be improved with JSON parsing library
                String responseText = response.toString();
                if (responseText.contains("\"text\":")) {
                    int startIndex = responseText.indexOf("\"text\":") + 7;
                    int endIndex = responseText.indexOf("\"}", startIndex);
                    if (endIndex == -1) {
                        endIndex = responseText.indexOf("\",", startIndex);
                    }
                    if (endIndex != -1) {
                        return responseText.substring(startIndex, endIndex)
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"");
                    }
                }
                return "I received a response but couldn't parse it properly.";
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return "Error: " + responseCode + " - " + response;
            }
        }
    }
}

package Controllers.forum;

import Services.TenorAPIService;
import Services.TenorAPIService.GifResult;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GifPickerDialog extends Stage {
    
    private TenorAPIService tenorService;
    private TextField searchField;
    private FlowPane gifContainer;
    private ProgressIndicator loadingIndicator;
    private Button searchButton;
    private GifResult selectedGif;
    private Consumer<GifResult> onGifSelectedCallback;
    
    public GifPickerDialog(Consumer<GifResult> onGifSelectedCallback) {
        this.tenorService = new TenorAPIService();
        this.onGifSelectedCallback = onGifSelectedCallback;
        
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Select a GIF");
        setWidth(600);
        setHeight(500);
        
        createContent();
        loadTrendingGifs();
    }
    
    private void createContent() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("gif-picker-dialog");
        root.setStyle("-fx-background-color: #1e1e30;");
        
        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(15));
        searchBox.setAlignment(Pos.CENTER);
        
        searchField = new TextField();
        searchField.setPromptText("Search GIFs...");
        searchField.setPrefWidth(400);
        searchField.getStyleClass().add("search-field");
        searchField.setStyle("-fx-background-color: rgba(30, 30, 48, 0.6); -fx-border-color: rgba(255, 255, 255, 0.1); " +
                "-fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: rgba(255, 255, 255, 0.5); -fx-padding: 8px;");
        
        searchButton = new Button("Search");
        searchButton.getStyleClass().add("search-button");
        searchButton.setStyle("-fx-background-color: linear-gradient(to right, #ff7e5f, #feb47b); -fx-text-fill: white; " +
                "-fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 12px; " +
                "-fx-font-weight: bold; -fx-border-width: 0;");
        
        searchButton.setOnAction(e -> searchGifs(searchField.getText()));
        
        // Add enter key handler for search field
        searchField.setOnAction(e -> searchGifs(searchField.getText()));
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // GIF container
        gifContainer = new FlowPane();
        gifContainer.setPadding(new Insets(10));
        gifContainer.setHgap(10);
        gifContainer.setVgap(10);
        gifContainer.setAlignment(Pos.CENTER);
        
        ScrollPane scrollPane = new ScrollPane(gifContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(80, 80);
        
        StackPane centerPane = new StackPane();
        centerPane.getChildren().addAll(scrollPane, loadingIndicator);
        
        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.7); " +
                "-fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand; -fx-font-size: 13px; " +
                "-fx-min-height: 32px;");
        
        cancelButton.setOnAction(e -> close());
        
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(15));
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.getChildren().add(cancelButton);
        
        // Main layout
        root.setTop(searchBox);
        root.setCenter(centerPane);
        root.setBottom(bottomBox);
        
        Scene scene = new Scene(root);
        setScene(scene);
    }
    
    private void searchGifs(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadTrendingGifs();
            return;
        }
        
        showLoading(true);
        gifContainer.getChildren().clear();
        
        CompletableFuture.supplyAsync(() -> tenorService.searchGifs(query))
            .thenAccept(this::displayGifs)
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Error loading GIFs");
                });
                e.printStackTrace();
                return null;
            });
    }
    
    private void loadTrendingGifs() {
        showLoading(true);
        gifContainer.getChildren().clear();
        
        CompletableFuture.supplyAsync(() -> tenorService.getTrendingGifs())
            .thenAccept(this::displayGifs)
            .exceptionally(e -> {
                Platform.runLater(() -> {
                    showLoading(false);
                    showError("Error loading trending GIFs");
                });
                e.printStackTrace();
                return null;
            });
    }
    
    private void displayGifs(List<GifResult> gifs) {
        Platform.runLater(() -> {
            showLoading(false);
            gifContainer.getChildren().clear();
            
            if (gifs.isEmpty()) {
                showNoResults();
                return;
            }
            
            for (GifResult gif : gifs) {
                VBox gifBox = createGifThumbnail(gif);
                gifContainer.getChildren().add(gifBox);
            }
        });
    }
    
    private VBox createGifThumbnail(GifResult gif) {
        VBox gifBox = new VBox(5);
        gifBox.setPadding(new Insets(5));
        gifBox.setAlignment(Pos.CENTER);
        gifBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2); -fx-background-radius: 4px; -fx-cursor: hand;");
        
        // Create image view with thumbnail
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(90);
        imageView.setPreserveRatio(true);
        
        // Add loading animation while thumbnail loads
        ProgressIndicator thumbnailLoading = new ProgressIndicator();
        thumbnailLoading.setMaxSize(30, 30);
        
        StackPane imageContainer = new StackPane();
        imageContainer.getChildren().addAll(imageView, thumbnailLoading);
        
        // Load image asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                Image image = new Image(gif.getPreviewUrl(), true);
                Platform.runLater(() -> {
                    imageView.setImage(image);
                    thumbnailLoading.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    thumbnailLoading.setVisible(false);
                    imageView.setImage(null);
                });
                e.printStackTrace();
            }
        });
        
        // Hover effect
        gifBox.setOnMouseEntered(e -> 
            gifBox.setStyle("-fx-background-color: rgba(255, 126, 95, 0.3); -fx-background-radius: 4px; -fx-cursor: hand;"));
        gifBox.setOnMouseExited(e -> 
            gifBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2); -fx-background-radius: 4px; -fx-cursor: hand;"));
        
        // Click handler
        gifBox.setOnMouseClicked(e -> {
            this.selectedGif = gif;
            if (onGifSelectedCallback != null) {
                onGifSelectedCallback.accept(gif);
            }
            close();
        });
        
        gifBox.getChildren().add(imageContainer);
        return gifBox;
    }
    
    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisible(isLoading);
        searchButton.setDisable(isLoading);
        searchField.setDisable(isLoading);
    }
    
    private void showNoResults() {
        Label noResultsLabel = new Label("No GIFs found");
        noResultsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        gifContainer.getChildren().add(noResultsLabel);
    }
    
    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");
        gifContainer.getChildren().add(errorLabel);
    }
} 
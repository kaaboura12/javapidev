package Controllers.forum;

import Services.ServicePost;
import Services.ServiceComment;
import Services.ServiceLike;
import Services.ServiceBadword;
import Services.ServiceReaction;
import Services.TenorAPIService;
import Models.Post;
import Models.Comment;
import Models.Reaction;
import Services.TenorAPIService.GifResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javafx.application.Platform;
import Utils.PexelsAPI;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Toggle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;

public class PostController implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextArea contentField;
    @FXML private TextField imageField;
    @FXML private StackPane imagePreviewContainer;
    @FXML private ImageView imagePreview;
    @FXML private Button clearImageButton;
    @FXML private TableView<Post> postsTable;
    @FXML private TableColumn<Post, Integer> idColumn;
    @FXML private TableColumn<Post, String> titleColumn;
    @FXML private TableColumn<Post, String> contentColumn;
    @FXML private TableColumn<Post, String> imageColumn;
    @FXML private TableColumn<Post, LocalDateTime> dateColumn;
    @FXML private VBox postsContainer;
    @FXML private TextArea newCommentField;
    @FXML private Button addCommentButton;
    @FXML private Button gifButton;
    @FXML private VBox postCardTemplate;
    @FXML private Label postAuthor;
    @FXML private Label postDate;
    @FXML private Label postTitle;
    @FXML private Label postContent;
    @FXML private ImageView postImage;
    @FXML private StackPane postImageContainer;
    @FXML private VBox commentsBox;
    @FXML private Button addButton;
    @FXML private VBox titleErrorContainer;
    @FXML private VBox contentErrorContainer;
    @FXML private Label titleErrorLabel;
    @FXML private Label contentErrorLabel;
    @FXML private VBox formErrorContainer;
    @FXML private FlowPane reactionButtons;
    @FXML private Button generateImageButton;
    
    // Add HBox to hold form buttons
    private HBox formButtonsContainer;
    private Button updateButton;

    private ServicePost postService;
    private ServiceComment commentService;
    private ServiceLike likeService;
    private ServiceBadword badwordService;
    private ServiceReaction reactionService;
    private ObservableList<Post> posts;
    private Post selectedPost;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Current user (hardcoded for now)
    private final int currentUserId = 4; // TODO: Get from authentication system

    // Update reaction arrays to use image filenames and short emoji codes
    private final String[] AVAILABLE_REACTIONS = {
        "Thumbs-Up--Streamline-Kawaii-Emoji.png", 
        "Grinning-Face-With-Smiling-Eyes--Streamline-Kawaii-Emoji.png", 
        "Face-With-Tears-Of-Joy--Streamline-Kawaii-Emoji.png", 
        "Star-Struck--Streamline-Kawaii-Emoji.png", 
        "Pensive-Face--Streamline-Kawaii-Emoji.png", 
        "Pouting-Face--Streamline-Kawaii-Emoji.png"
    };
    
    private final String[] REACTION_TYPES = {"like", "happy", "laugh", "wow", "sad", "angry"};
    private final String[] EMOJI_CODES = {"👍", "😊", "😂", "😮", "😢", "😡"}; // Short codes for database storage
    private final String EMOJI_PATH = "/Images/emojis/";
    
    // GIF selection variables
    private GifResult selectedGif = null;
    private ImageView selectedGifPreview = null;
    private HBox gifPreviewContainer = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        postService = new ServicePost();
        commentService = new ServiceComment();
        likeService = new ServiceLike();
        badwordService = new ServiceBadword();
        reactionService = new ServiceReaction();
        posts = FXCollections.observableArrayList();

        // Initialize error containers and labels if they exist in the FXML
        setupValidationElements();
        
        // Setup the form buttons container
        setupFormButtons();
        
        // Setup GIF button if it doesn't exist
        setupGifButton();

        // Setup image field listener for URL changes
        if (imageField != null) {
            imageField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.trim().isEmpty()) {
                    updateImagePreview(newValue.trim());
                } else {
                    hideImagePreview();
                }
            });
        }

        // Load initial data
        refreshPosts();
    }

    private void setupValidationElements() {
        // Create title error label directly added to the parent container
        if (titleErrorContainer == null) {
            titleErrorContainer = new VBox();
            titleErrorContainer.setVisible(false);
            titleErrorContainer.setManaged(false);
            
            titleErrorLabel = new Label("Title is required");
            titleErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-background-color: rgba(231, 76, 60, 0.1); -fx-padding: 5 10; -fx-border-color: #e74c3c; -fx-border-width: 0 0 0 2;");
            
            titleErrorContainer.getChildren().add(titleErrorLabel);
            
            // Find the parent container of the post form
            if (titleField != null) {
                // Get the VBox containing the form elements (should be 2 levels up)
                if (titleField.getParent() != null && titleField.getParent().getParent() instanceof VBox) {
                    VBox formContainer = (VBox) titleField.getParent().getParent();
                    // Insert after the title field's parent HBox
                    for (int i = 0; i < formContainer.getChildren().size(); i++) {
                        Node node = formContainer.getChildren().get(i);
                        if (node instanceof HBox && ((HBox) node).getChildren().contains(titleField)) {
                            formContainer.getChildren().add(i + 1, titleErrorContainer);
                            break;
                        }
                    }
                }
            }
        }
        
        if (contentErrorContainer == null) {
            contentErrorContainer = new VBox();
            contentErrorContainer.setVisible(false);
            contentErrorContainer.setManaged(false);
            contentErrorContainer.getStyleClass().add("error-container");
            
            contentErrorLabel = new Label("Content is required");
            contentErrorLabel.getStyleClass().add("field-error-label");
            contentErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-padding: 3 0 0 5;");
            
            contentErrorContainer.getChildren().add(contentErrorLabel);
            
            // Insert after contentField
            if (contentField != null && contentField.getParent() instanceof VBox) {
                VBox parent = (VBox) contentField.getParent();
                int index = parent.getChildren().indexOf(contentField);
                if (index >= 0) {
                    parent.getChildren().add(index + 1, contentErrorContainer);
                }
            }
        }
        
        if (formErrorContainer == null) {
            formErrorContainer = new VBox();
            formErrorContainer.getStyleClass().add("validation-error-box");
            formErrorContainer.setVisible(false);
            formErrorContainer.setManaged(false);
            
            // If there's a parent container for the form fields, add the error container at the top
            if (titleField != null && titleField.getParent() != null && titleField.getParent().getParent() instanceof VBox) {
                VBox formContainer = (VBox) titleField.getParent().getParent();
                formContainer.getChildren().add(0, formErrorContainer);
            }
        }
    }

    private void setupFormButtons() {
        // Create the form buttons container
        formButtonsContainer = new HBox(10); // 10 is the spacing between buttons
        formButtonsContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Create Update button if it doesn't exist
        updateButton = new Button("Update Post");
        updateButton.getStyleClass().add("update-button");
        updateButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        updateButton.setOnAction(e -> handleUpdatePost());
        updateButton.setVisible(false);
        updateButton.setManaged(false);
        
        // Style the Add button if it exists
        if (addButton != null) {
            addButton.getStyleClass().add("add-button");
            addButton.setStyle("-fx-padding: 10 20;");
            
            // Get the parent of the Add button
            Node addButtonParent = addButton.getParent();
            if (addButtonParent != null) {
                // Remove the Add button from its current parent
                ((Pane) addButtonParent).getChildren().remove(addButton);
                
                // Add both buttons to the new container
                formButtonsContainer.getChildren().addAll(addButton, updateButton);
                
                // Add the new container where the Add button was
                ((Pane) addButtonParent).getChildren().add(formButtonsContainer);
            }
        }
    }

    private void setupGifButton() {
        // If we don't have a GIF button in FXML, create it programmatically
        if (gifButton == null && newCommentField != null) {
            gifButton = new Button("GIF");
            gifButton.getStyleClass().add("gif-button");
            gifButton.setStyle("-fx-background-color: linear-gradient(to right, #7e5fff, #7b9dfe); " +
                    "-fx-text-fill: white; -fx-padding: 6px 12px; -fx-background-radius: 4px; -fx-cursor: hand; " +
                    "-fx-font-size: 12px; -fx-font-weight: bold; -fx-border-width: 0;");
            
            // Find the HBox containing the comment field and button
            if (addCommentButton != null && addCommentButton.getParent() instanceof HBox) {
                HBox commentBox = (HBox) addCommentButton.getParent();
                
                // Add the GIF button before the comment button
                int commentBtnIndex = commentBox.getChildren().indexOf(addCommentButton);
                if (commentBtnIndex >= 0) {
                    commentBox.getChildren().add(commentBtnIndex, gifButton);
                } else {
                    commentBox.getChildren().add(gifButton);
                }
            }
        }
        
        // Create GIF preview container
        if (gifPreviewContainer == null) {
            gifPreviewContainer = new HBox();
            gifPreviewContainer.setAlignment(Pos.CENTER_LEFT);
            gifPreviewContainer.setSpacing(10);
            gifPreviewContainer.setVisible(false);
            gifPreviewContainer.setManaged(false);
            gifPreviewContainer.setPadding(new Insets(5, 0, 5, 0));
            
            selectedGifPreview = new ImageView();
            selectedGifPreview.setFitHeight(80);
            selectedGifPreview.setPreserveRatio(true);
            
            Button removeGifButton = new Button("×");
            removeGifButton.setStyle("-fx-background-color: rgba(231, 76, 60, 0.7); -fx-text-fill: white; " +
                    "-fx-background-radius: 50%; -fx-padding: 2px 6px; -fx-font-weight: bold; -fx-font-size: 12px;");
            removeGifButton.setOnAction(e -> clearSelectedGif());
            
            gifPreviewContainer.getChildren().addAll(selectedGifPreview, removeGifButton);
            
            // Add the preview container after the comment box
            if (addCommentButton != null && addCommentButton.getParent() != null && 
                addCommentButton.getParent().getParent() instanceof VBox) {
                VBox parentContainer = (VBox) addCommentButton.getParent().getParent();
                int commentBoxIndex = -1;
                for (int i = 0; i < parentContainer.getChildren().size(); i++) {
                    Node node = parentContainer.getChildren().get(i);
                    if (node instanceof HBox && ((HBox) node).getChildren().contains(addCommentButton)) {
                        commentBoxIndex = i;
                        break;
                    }
                }
                
                if (commentBoxIndex >= 0) {
                    parentContainer.getChildren().add(commentBoxIndex + 1, gifPreviewContainer);
                }
            }
        }
        
        // Setup GIF button action
        if (gifButton != null) {
            gifButton.setOnAction(e -> handleShowGifPicker());
        }
    }
    
    /**
     * Handle showing the GIF picker dialog when the GIF button is clicked
     */
    @FXML
    private void handleShowGifPicker() {
        GifPickerDialog gifPicker = new GifPickerDialog(this::handleGifSelected);
        gifPicker.showAndWait();
    }
    
    private void handleGifSelected(GifResult gif) {
        if (gif != null) {
            this.selectedGif = gif;
            showSelectedGif(gif);
        }
    }
    
    private void showSelectedGif(GifResult gif) {
        if (selectedGifPreview != null && gifPreviewContainer != null) {
            try {
                Image image = new Image(gif.getPreviewUrl());
                selectedGifPreview.setImage(image);
                gifPreviewContainer.setVisible(true);
                gifPreviewContainer.setManaged(true);
            } catch (Exception e) {
                e.printStackTrace();
                clearSelectedGif();
            }
        }
    }
    
    private void clearSelectedGif() {
        selectedGif = null;
        if (selectedGifPreview != null) {
            selectedGifPreview.setImage(null);
        }
        if (gifPreviewContainer != null) {
            gifPreviewContainer.setVisible(false);
            gifPreviewContainer.setManaged(false);
        }
    }

    private void showEditMode(Post post) {
        // Set the post data in the form fields for editing
        selectedPost = post;
        titleField.setText(post.getTitle());
        contentField.setText(post.getContent());
        imageField.setText(post.getImage());
        
        // Show update button and hide add button
        if (formButtonsContainer != null) {
            updateButton.setVisible(true);
            updateButton.setManaged(true);
            addButton.setVisible(false);
            addButton.setManaged(false);
        }
        
        // Show edit mode indicator
        showFormError("Edit Mode", "You are editing post: " + post.getTitle());

        // Scroll to form area smoothly
        Platform.runLater(() -> {
            // Find the ScrollPane containing the form
            Node current = titleField;
            while (current != null && !(current instanceof ScrollPane)) {
                current = current.getParent();
            }
            
            if (current instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) current;
                
                // Create a smooth scrolling animation
                double startValue = scrollPane.getVvalue();
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(500), // 500ms duration for smooth scroll
                        new KeyValue(scrollPane.vvalueProperty(), 0) // Scroll to top
                    )
                );
                timeline.play();
                
                // Request focus on the title field after scrolling
                timeline.setOnFinished(e -> titleField.requestFocus());
            }
        });
    }

    private void exitEditMode() {
        // Clear form and selection
        clearFields();
        selectedPost = null;
        
        // Reset button visibility
        if (formButtonsContainer != null) {
            updateButton.setVisible(false);
            updateButton.setManaged(false);
            addButton.setVisible(true);
            addButton.setManaged(true);
        }
        
        // Clear edit mode indicator
        if (formErrorContainer != null) {
            formErrorContainer.setVisible(false);
            formErrorContainer.setManaged(false);
        }
    }
    
    // Update the edit button click handler
    private Button createEditButton(Post post, VBox postBox) {
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("edit-button");
        Tooltip editTooltip = new Tooltip("Edit this post");
        Tooltip.install(editBtn, editTooltip);
        
        editBtn.setOnAction(e -> {
            showEditMode(post);
            
            // Highlight the selected post
            postsContainer.getChildren().forEach(node -> {
                if (node instanceof VBox) {
                    node.getStyleClass().remove("selected-post");
                }
            });
            postBox.getStyleClass().add("selected-post");
        });
        
        return editBtn;
    }

    private void refreshPosts() {
        postsContainer.getChildren().clear();
        posts.setAll(postService.getAllPosts());

        for (Post post : posts) {
            VBox postBox = createPostBox(post);
            postsContainer.getChildren().add(postBox);
        }
    }

    private VBox createPostBox(Post post) {
        VBox postBox = new VBox(10);
        postBox.getStyleClass().add("post-card-modern");
        postBox.setSpacing(15);
        
        // Post header
        HBox header = new HBox(15);
        header.getStyleClass().add("post-header");
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // User avatar (placeholder)
        Circle avatar = new Circle(25);
        avatar.getStyleClass().add("user-avatar");
        
        // Author info
        VBox authorInfo = new VBox(3);
        Label authorName = new Label("Utilisateur " + post.getAuthorId()); // TODO: Get actual username
        authorName.getStyleClass().add("post-author");
        Label dateLabel = new Label(post.getCreatedAt().format(dateFormatter));
        dateLabel.getStyleClass().add("post-date");
        authorInfo.getChildren().addAll(authorName, dateLabel);
        
        // Actions
        HBox actions = new HBox(10);
        actions.getStyleClass().add("post-actions");
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        // Check if the post belongs to the current user
        boolean isCurrentUserPost = post.getAuthorId() == currentUserId;
        System.out.println("Post ID: " + post.getId() + ", Author ID: " + post.getAuthorId() + ", Current User ID: " + currentUserId + ", Is Own Post: " + isCurrentUserPost);
        
        // Only add edit/delete buttons if the post belongs to current user
        if (isCurrentUserPost) {
            System.out.println("Adding edit/delete buttons for post ID: " + post.getId());
            Button editBtn = createEditButton(post, postBox);
            
            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().addAll("delete-button");
            Tooltip deleteTooltip = new Tooltip("Delete this post");
            Tooltip.install(deleteBtn, deleteTooltip);
            
            deleteBtn.setOnAction(e -> {
                // Confirm deletion
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Confirmation");
                confirmDialog.setHeaderText("Delete Post");
                confirmDialog.setContentText("Are you sure you want to delete this post?");

                if (confirmDialog.showAndWait().get() == ButtonType.OK) {
                    if (postService.deletePost(post.getId())) {
                        refreshPosts();
                        clearFields();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Post deleted successfully!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Could not delete post");
                    }
                }
            });
            
            // Make actions more visible
            actions.setSpacing(10);
            actions.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));
            
            actions.getChildren().addAll(editBtn, deleteBtn);
        }
        
        // Add spacer between author and actions
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(avatar, authorInfo, spacer);
        
        // Only add the actions box if it has children (edit/delete buttons)
        if (!actions.getChildren().isEmpty()) {
            header.getChildren().add(actions);
        }
        
        // Post content
        VBox contentBox = new VBox(10);
        contentBox.getStyleClass().add("post-content");
        
        Label titleLabel = new Label(post.getTitle());
        titleLabel.getStyleClass().add("post-title-modern");
        titleLabel.setWrapText(true);
        
        Label contentLabel = new Label(post.getContent());
        contentLabel.getStyleClass().add("post-content-text");
        contentLabel.setWrapText(true);
        
        contentBox.getChildren().addAll(titleLabel, contentLabel);
        
        // Post image if exists
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            try {
                String img = post.getImage();
                Image image = null;
                // More robust base64 detection: check for data URI or long string with only base64 chars
                if ((img.startsWith("data:image") && img.contains(",")) || (img.length() > 100 && img.matches("[A-Za-z0-9+/=]+"))) {
                    // Remove data URI prefix if present
                    String base64Data = img;
                    if (img.startsWith("data:image") && img.contains(",")) {
                        base64Data = img.substring(img.indexOf(",") + 1);
                    }
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                    image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                } else {
                    File file = new File(img);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else {
                        image = new Image(img);
                    }
                }
                if (image != null && !image.isError()) {
                    ImageView imageView = new ImageView(image);
                    // Reduce image width from 600 to 400 pixels
                    imageView.setFitWidth(400);
                    imageView.setPreserveRatio(true);
                    imageView.getStyleClass().add("post-image-modern");
                    
                    // Add a container to center the image
                    StackPane imageContainer = new StackPane(imageView);
                    imageContainer.getStyleClass().add("post-image-container");
                    imageContainer.setMaxWidth(400); // Limit container width to match image
                    imageContainer.setAlignment(Pos.CENTER); // Center the image
                    
                    contentBox.getChildren().add(imageContainer);
                } else {
                    System.err.println("Image could not be displayed for post ID: " + post.getId());
                }
            } catch (Exception e) {
                System.err.println("Error loading image for post ID " + post.getId() + ": " + e.getMessage());
            }
        }
        
        // Post stats
        HBox statsBox = new HBox(20);
        statsBox.getStyleClass().add("post-stats");
        
        // Likes section (keep original)
        HBox likesBox = new HBox(5);
        likesBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Create like button with heart icon
        Button likeButton = new Button();
        likeButton.getStyleClass().addAll("like-button", "icon-button");
        
        // Check if user has liked this post
        boolean hasLiked = likeService.hasUserLikedPost(currentUserId, post.getId());
        if (hasLiked) {
            likeButton.getStyleClass().add("liked");
        } else {
            likeButton.getStyleClass().remove("liked");
        }
        
        // Get likes count
        int likesCount = likeService.countLikesForPost(post.getId());
        Label likesCountLabel = new Label(likesCount + (likesCount == 1 ? " Like" : " Likes"));
        likesCountLabel.getStyleClass().add("stat-value");
        
        // Add like button click handler
        likeButton.setOnAction(e -> {
            boolean success = likeService.toggleLike(currentUserId, post.getId());
            if (success) {
                // Update like count and button style
                int newLikesCount = likeService.countLikesForPost(post.getId());
                likesCountLabel.setText(newLikesCount + (newLikesCount == 1 ? " Like" : " Likes"));
                
                boolean isNowLiked = likeService.hasUserLikedPost(currentUserId, post.getId());
                if (isNowLiked) {
                    if (!likeButton.getStyleClass().contains("liked")) {
                    likeButton.getStyleClass().add("liked");
                    }
                    
                    // Add a little animation
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), likeButton);
                    scaleTransition.setFromX(1.0);
                    scaleTransition.setFromY(1.0);
                    scaleTransition.setToX(1.2);
                    scaleTransition.setToY(1.2);
                    
                    ScaleTransition scaleTransition2 = new ScaleTransition(Duration.millis(200), likeButton);
                    scaleTransition2.setFromX(1.2);
                    scaleTransition2.setFromY(1.2);
                    scaleTransition2.setToX(1.0);
                    scaleTransition2.setToY(1.0);
                    
                    SequentialTransition sequentialTransition = new SequentialTransition(scaleTransition, scaleTransition2);
                    sequentialTransition.play();
                } else {
                    likeButton.getStyleClass().remove("liked");
                }
            }
        });
        
        likesBox.getChildren().addAll(likeButton, likesCountLabel);
        
        // Reactions section
        HBox reactionsBox = new HBox(5);
        reactionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Create a StackPane to properly position the panel above the button
        StackPane reactionStackPane = new StackPane();
        
        // Create reaction button with default emoji or user's reaction
        Button reactionButton = new Button();
        reactionButton.getStyleClass().addAll("reaction-trigger-button");
        
        // Create emoji reaction panel (hidden by default)
        FlowPane reactionPanel = new FlowPane(5, 5);
        reactionPanel.getStyleClass().add("reaction-panel");
        reactionPanel.setPrefWrapLength(180);
        reactionPanel.setPadding(new Insets(8));
        reactionPanel.setVisible(false);
        reactionPanel.setManaged(false);
        
        // Apply drop shadow effect to the panel
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.7));
        dropShadow.setRadius(10);
        dropShadow.setOffsetY(3);
        reactionPanel.setEffect(dropShadow);
        
        // Get reaction counts for this post
        Map<String, Integer> reactionCounts = reactionService.getReactionCountsByPostId(post.getId());
        
        // Check if user has reacted to this post
        Reaction userReaction = reactionService.getUserReaction(currentUserId, post.getId());
        
        // Set default emoji icon or user's reaction
        if (userReaction != null) {
            // Convert emoji code to filename
            String emojiFileName = getEmojiFilenameFromCode(userReaction.getEmoji());
            
            if (emojiFileName != null) {
                ImageView userEmojiView = createEmojiImageView(emojiFileName, 28);
                if (userEmojiView != null) {
                    reactionButton.setGraphic(userEmojiView);
                    reactionButton.setText("");
                } else {
                    reactionButton.setText(userReaction.getEmoji()); // Use stored emoji code as fallback
                }
                reactionButton.getStyleClass().add("reacted");
                reactionButton.getStyleClass().add("reaction-" + userReaction.getType());
            }
        } else {
            // Default "add reaction" icon - using a simple text label for now
            reactionButton.setText("😀");
        }
        
        // Create reaction buttons in the panel
        for (int i = 0; i < AVAILABLE_REACTIONS.length; i++) {
            final int index = i; // Create a final copy of the index for use in lambdas
            final String emojiFileName = AVAILABLE_REACTIONS[i];
            final String type = REACTION_TYPES[i];
            
            Button emojiBtn = new Button();
            emojiBtn.getStyleClass().add("reaction-emoji-button");
            emojiBtn.getStyleClass().add("reaction-" + type);
            
            // Load emoji image
            ImageView emojiView = createEmojiImageView(emojiFileName, 30);
            if (emojiView != null) {
                emojiBtn.setGraphic(emojiView);
                emojiBtn.setText("");
            } else {
                emojiBtn.setText(getFallbackEmoji(emojiFileName));
            }
            
            // Add reaction count if exists
            int count = reactionCounts.getOrDefault(type, 0);
            
            // Add tooltip with count
            Tooltip tooltip = new Tooltip(type + " (" + count + ")");
            tooltip.setShowDelay(Duration.millis(300));
            Tooltip.install(emojiBtn, tooltip);
            
            // Add reaction handler
            emojiBtn.setOnAction(e -> {
                // Use index instead of i, which is effectively final
                String emojiCode = EMOJI_CODES[index]; 
                boolean success = reactionService.toggleReaction(currentUserId, post.getId(), type, emojiCode);
                if (success) {
                    // Update reaction button immediately
                    ImageView updatedEmojiView = createEmojiImageView(emojiFileName, 28);
                    Reaction updatedReaction = reactionService.getUserReaction(currentUserId, post.getId());
                    
                    if (updatedReaction != null) {
                        // Set the emoji image on the button
                        if (updatedEmojiView != null) {
                            reactionButton.setGraphic(updatedEmojiView);
                            reactionButton.setText("");
                        } else {
                            reactionButton.setText(emojiCode); // Use emoji code as fallback
                        }
                        
                        reactionButton.getStyleClass().removeAll("reaction-like", "reaction-happy", 
                            "reaction-laugh", "reaction-wow", "reaction-sad", "reaction-angry");
                        reactionButton.getStyleClass().add("reacted");
                        reactionButton.getStyleClass().add("reaction-" + type);
                    } else {
                        // Reset to default
                        reactionButton.setGraphic(null);
                        reactionButton.setText("😀");
                        reactionButton.getStyleClass().removeAll("reacted", "reaction-like", "reaction-happy", 
                            "reaction-laugh", "reaction-wow", "reaction-sad", "reaction-angry");
                    }
                    
                    reactionPanel.setVisible(false);
                    reactionPanel.setManaged(false);
                    
                    // Refresh to update counts
                    refreshPosts();
                }
            });
            
            reactionPanel.getChildren().add(emojiBtn);
        }
        
        // Add panel and button to the StackPane in order (panel on top)
        reactionStackPane.getChildren().addAll(reactionButton);
        
        // Position the panel above the button using a container
        VBox reactionContainer = new VBox(5);
        reactionContainer.setAlignment(Pos.BOTTOM_CENTER);
        reactionContainer.getChildren().addAll(reactionPanel, reactionStackPane);
        
        // Setup reaction panel toggle
        reactionButton.setOnMouseEntered(e -> {
            reactionPanel.setVisible(true);
            reactionPanel.setManaged(true);
        });
        
        // Hide panel when mouse leaves the container
        reactionContainer.setOnMouseExited(e -> {
            // Add a small delay before hiding to prevent accidental hiding
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), evt -> {
                reactionPanel.setVisible(false);
                reactionPanel.setManaged(false);
            }));
            timeline.play();
        });
        
        // Add mouse entered handler to keep panel visible when mouse is over it
        reactionPanel.setOnMouseEntered(e -> {
            reactionPanel.setVisible(true);
            reactionPanel.setManaged(true);
        });
        
        // Get reaction count for label
        int reactionCount = 0;
        for (Integer count : reactionCounts.values()) {
            reactionCount += count;
        }
        
        // Update the count display to specifically show the active reaction
        Label reactionsCountLabel = new Label();
        if (userReaction != null) {
            // Find emoji filename for display
            String userEmojiFile = null;
            for (int i = 0; i < REACTION_TYPES.length; i++) {
                if (REACTION_TYPES[i].equals(userReaction.getType())) {
                    userEmojiFile = AVAILABLE_REACTIONS[i];
                    break;
                }
            }
            
            reactionsCountLabel.setText("You reacted: " + userReaction.getType());
        } else {
            reactionsCountLabel.setText(reactionCount + " Reactions");
        }
        reactionsCountLabel.getStyleClass().add("stat-value");
        
        reactionsBox.getChildren().addAll(reactionContainer, reactionsCountLabel);
        
        // Add comments count
        HBox commentsBox = new HBox(5);
        commentsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.shape.Circle commentsIcon = new javafx.scene.shape.Circle(10);
        commentsIcon.getStyleClass().addAll("stat-icon", "comments-icon");
        
        int commentCount = commentService.getCommentsByPostId(post.getId()).size();
        Label commentsCount = new Label(commentCount + " Comments");
        commentsCount.getStyleClass().add("stat-value");
        commentsBox.getChildren().addAll(commentsIcon, commentsCount);
        
        // Add all stats to stats box
        statsBox.getChildren().addAll(likesBox, reactionsBox, commentsBox);
        
        // Comments section
        VBox commentsSection = new VBox(10);
        commentsSection.getStyleClass().add("comments-section");
        
        HBox commentsTitleBox = new HBox();
        commentsTitleBox.getStyleClass().add("section-subtitle-container");
        commentsTitleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label commentsTitle = new Label("Comments");
        commentsTitle.getStyleClass().add("section-subtitle");
        commentsTitleBox.getChildren().add(commentsTitle);
        
        VBox commentsContainer = new VBox(10);
        commentsContainer.getStyleClass().add("comments-box");
        
        // Load comments for this post
        for (Comment comment : commentService.getCommentsByPostId(post.getId())) {
            VBox commentBox = createCommentBox(comment);
            commentsContainer.getChildren().add(commentBox);
        }
        
        // Add comment form
        HBox commentForm = new HBox(10);
        commentForm.getStyleClass().add("new-comment-container");
        
        TextArea commentField = new TextArea();
        commentField.setPromptText("Add a comment...");
        commentField.setPrefRowCount(2);
        commentField.getStyleClass().add("comment-text-area");
        HBox.setHgrow(commentField, javafx.scene.layout.Priority.ALWAYS);
        
        // Create GIF button for this comment form
        Button gifBtn = new Button("GIF");
        gifBtn.getStyleClass().add("gif-button");
        gifBtn.setOnAction(e -> {
            // Create a new GifPickerDialog for this specific comment field and post
            GifPickerDialog gifPicker = new GifPickerDialog(gif -> {
                if (gif != null) {
                    this.selectedGif = gif;
                    showSelectedGif(gif);
                }
            });
            gifPicker.showAndWait();
        });
        
        Button commentBtn = new Button("Comment");
        commentBtn.getStyleClass().add("comment-button");
        commentBtn.setOnAction(e -> handleCommentSubmission(commentField, post));
        
        commentForm.getChildren().addAll(commentField, gifBtn, commentBtn);
        
        commentsSection.getChildren().addAll(commentsTitleBox, commentsContainer, commentForm);
        
        // Add all elements to post box
        postBox.getChildren().addAll(header, contentBox, statsBox, commentsSection);
        
        // If this is the selected post, highlight it
        if (selectedPost != null && selectedPost.getId() == post.getId()) {
            postBox.getStyleClass().add("selected-post");
        }

        return postBox;
    }

    private VBox createCommentBox(Comment comment) {
        VBox commentBox = new VBox(5);
        commentBox.getStyleClass().add("comment-box");
        
        // Comment header with author and date
        HBox commentHeader = new HBox(8);
        commentHeader.setAlignment(Pos.CENTER_LEFT);
        
        // User avatar image
        Circle avatarCircle = new Circle(15);
        avatarCircle.setFill(Color.web("#2a2a40"));
        avatarCircle.setStroke(Color.web("#ff7e5f"));
        avatarCircle.setStrokeWidth(1.5);
        
        // Add a label with first letter of user ID as avatar
        Label avatarLabel = new Label(String.valueOf(comment.getUserId()).substring(0, 1));
        avatarLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        StackPane avatar = new StackPane();
        avatar.getChildren().addAll(avatarCircle, avatarLabel);
        
        Label authorLabel = new Label("User " + comment.getUserId());
        authorLabel.getStyleClass().add("comment-author");
        
        Label dateLabel = new Label(comment.getCreatedAt().format(dateFormatter));
        dateLabel.getStyleClass().add("comment-date");
        dateLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6); -fx-font-size: 11px;");
        
        // Spacer for pushing delete button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button deleteButton = null;
        // Only show delete button for the current user's comments
        if (comment.getUserId() == currentUserId) {
            deleteButton = new Button("×");
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 14px;");
            deleteButton.setOnAction(e -> {
                if (commentService.deleteComment(comment.getId())) {
                    refreshPosts(); // Refresh to update comment list
                }
            });
        }
        
        commentHeader.getChildren().addAll(avatar, authorLabel, new Label("•"), dateLabel, spacer);
        
        if (deleteButton != null) {
            commentHeader.getChildren().add(deleteButton);
        }
        
        // Comment content (only add if not empty)
        if (comment.getContent() != null && !comment.getContent().trim().isEmpty()) {
            Label contentLabel = new Label(comment.getContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("comment-content");
            contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            commentBox.getChildren().addAll(commentHeader, contentLabel);
        } else {
            commentBox.getChildren().add(commentHeader);
        }
        
        // Add GIF if present
        if (comment.getGifUrl() != null && !comment.getGifUrl().isEmpty()) {
            try {
                ImageView gifView = new ImageView(new Image(comment.getGifUrl()));
                gifView.setFitWidth(200);
                gifView.setPreserveRatio(true);
                gifView.getStyleClass().add("comment-gif");
                
                StackPane gifContainer = new StackPane(gifView);
                gifContainer.getStyleClass().add("comment-gif-container");
                gifContainer.setStyle("-fx-padding: 5 0 0 0;");
                
                commentBox.getChildren().add(gifContainer);
            } catch (Exception e) {
                System.err.println("Error loading GIF for comment ID " + comment.getId() + ": " + e.getMessage());
            }
        }
        
        return commentBox;
    }

    @FXML
    private void handleAddComment() {
        if (selectedPost != null && (!newCommentField.getText().trim().isEmpty() || selectedGif != null)) {
            handleCommentSubmission(newCommentField, selectedPost);
        }
    }

    // Renamed from handleAddComment to handleCommentSubmission to avoid confusion
    private void handleCommentSubmission(TextArea commentField, Post post) {
        String commentText = commentField.getText().trim();
        // Allow empty comment text if a GIF is selected
        if (!commentText.isEmpty() || selectedGif != null) {
            // Filter bad words without showing warning
            if (!commentText.isEmpty()) {
                commentText = badwordService.filterText(commentText);
            }

            Comment newComment = new Comment();
            newComment.setPostId(post.getId());
            newComment.setUserId(currentUserId);
            newComment.setContent(commentText); // Use filtered text
            
            // Add GIF URL if one is selected
            if (selectedGif != null) {
                newComment.setGifUrl(selectedGif.getUrl());
            }
            
            newComment.setCreatedAt(LocalDateTime.now());
            newComment.setUpdatedAt(LocalDateTime.now());

            if (commentService.createComment(newComment)) {
                commentField.clear();
                clearSelectedGif();
                refreshPosts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not add comment");
            }
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(imageField.getScene().getWindow());
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            imageField.setText(imagePath);
            updateImagePreview(imagePath);
        }
    }

    @FXML
    private void handleClearImage() {
        imageField.clear();
        hideImagePreview();
    }

    private void updateImagePreview(String imagePath) {
        try {
            Image image = null;
            
            // Check if it's a base64 image
            if ((imagePath.startsWith("data:image") && imagePath.contains(",")) || 
                (imagePath.length() > 100 && imagePath.matches("[A-Za-z0-9+/=]+"))) {
                // Remove data URI prefix if present
                String base64Data = imagePath;
                if (imagePath.startsWith("data:image") && imagePath.contains(",")) {
                    base64Data = imagePath.substring(imagePath.indexOf(",") + 1);
                }
                // Decode base64 to image
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                image = new Image(new ByteArrayInputStream(imageBytes));
            } else {
                // Try as local file first
                File file = new File(imagePath);
                if (file.exists()) {
                    // Local file
                    image = new Image(file.toURI().toString());
                } else {
                    // Try as URL
                    image = new Image(imagePath);
                }
            }
            
            if (!image.isError()) {
                imagePreview.setImage(image);
                imagePreviewContainer.setVisible(true);
                imagePreviewContainer.setManaged(true);
            } else {
                hideImagePreview();
            }
        } catch (Exception e) {
            System.err.println("Error loading image preview: " + e.getMessage());
            hideImagePreview();
        }
    }

    private void hideImagePreview() {
        imagePreview.setImage(null);
        imagePreviewContainer.setVisible(false);
        imagePreviewContainer.setManaged(false);
    }

    @FXML
    private void handleAddPost() {
        clearValidationErrors(); // Clear any previous error indicators
        
        // If we're in edit mode (selectedPost exists), don't create a new post
        if (selectedPost != null) {
            showFormError("Edit Mode", "You are currently editing a post. Please use the Update button to save changes.");
            return;
        }
        
        if (validateFields()) {
            Post post = new Post();
            post.setTitle(titleField.getText());
            post.setContent(contentField.getText());
            post.setImage(imageField.getText());
            post.setAuthorId(currentUserId); // Use the current user ID
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());

            if (postService.createPost(post)) {
                refreshPosts();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post created successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not create post");
            }
        }
    }

    @FXML
    private void handleUpdatePost() {
        clearValidationErrors();
        
        if (selectedPost == null) {
            showFormError("Update Error", "No post selected for update");
            return;
        }

        if (validateFields()) {
            // Update the selected post with new values
            selectedPost.setTitle(titleField.getText().trim());
            selectedPost.setContent(contentField.getText().trim());
            selectedPost.setImage(imageField.getText().trim());
            selectedPost.setUpdatedAt(LocalDateTime.now());

            boolean success = postService.updatePost(selectedPost);
            
            if (success) {
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post updated successfully!");
                
                // Exit edit mode (this will clear fields and reset buttons)
                exitEditMode();
                
                // Refresh the posts display
                refreshPosts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update post");
            }
        }
    }

    @FXML
    private void handleDeletePost() {
        if (selectedPost == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a post to delete");
            return;
        }

        // Check if this post belongs to the current user
        if (selectedPost.getAuthorId() != currentUserId) {
            showAlert(Alert.AlertType.WARNING, "Warning", "You can only delete your own posts");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText("Delete Post");
        confirmDialog.setContentText("Are you sure you want to delete this post?");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            if (postService.deletePost(selectedPost.getId())) {
                refreshPosts();
                clearFields();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete post");
            }
        }
    }

    @FXML
    private void clearFields() {
        titleField.clear();
        contentField.clear();
        imageField.clear();
        hideImagePreview();
        selectedPost = null;
        
        // Clear validation styling
        clearValidationErrors();

        // Reset button visibility - show add button, hide update button
        if (updateButton != null && addButton != null) {
            updateButton.setVisible(false);
            updateButton.setManaged(false);
            addButton.setVisible(true);
            addButton.setManaged(true);
        }

        // Clear selection highlight
        postsContainer.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                node.getStyleClass().remove("selected-post");
            }
        });
    }
    
    private void clearValidationErrors() {
        // Remove error styling from fields - using consistent direct style approach
        titleField.setStyle("");
        contentField.setStyle(""); // Changed to match title field approach
        
        // Hide error messages
        if (titleErrorContainer != null) {
            titleErrorContainer.setVisible(false);
            titleErrorContainer.setManaged(false);
        }
        
        if (contentErrorContainer != null) {
            contentErrorContainer.setVisible(false);
            contentErrorContainer.setManaged(false);
        }
        
        if (formErrorContainer != null) {
            formErrorContainer.setVisible(false);
            formErrorContainer.setManaged(false);
        }
    }
    
    private void showFieldError(TextField field, VBox errorContainer, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: #e74c3c;");
            
            if (errorContainer != null && errorLabel != null) {
                errorLabel.setText(message);
                errorContainer.setVisible(true);
                errorContainer.setManaged(true);
                
                // Debug message to verify the error is being displayed
                System.out.println("Showing error for field: " + message);
            }
        }
    }
    
    private void showFieldError(TextArea field, VBox errorContainer, Label errorLabel, String message) {
        if (field != null) {
            field.setStyle("-fx-border-color: #e74c3c;");
            
            if (errorContainer != null && errorLabel != null) {
                errorLabel.setText(message);
                errorContainer.setVisible(true);
                errorContainer.setManaged(true);
                
                // Debug message to verify the error is being displayed
                System.out.println("Showing error for field: " + message);
            }
        }
    }
    
    private void showFormError(String title, String message) {
        if (formErrorContainer != null) {
            formErrorContainer.getChildren().clear();
            
            Label errorTitle = new Label(title);
            errorTitle.getStyleClass().add("validation-error-title");
            
            Label errorMessage = new Label(message);
            errorMessage.getStyleClass().add("validation-error-message");
            errorMessage.setWrapText(true);
            
            formErrorContainer.getChildren().addAll(errorTitle, errorMessage);
            formErrorContainer.setVisible(true);
            formErrorContainer.setManaged(true);
            
            // Auto-hide after 5 seconds
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> {
                formErrorContainer.setVisible(false);
                formErrorContainer.setManaged(false);
            });
            pause.play();
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        
        // Check title field
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            // Make title field red
            titleField.setStyle("-fx-border-color: #e74c3c;");
            // Show error directly under title field's parent HBox
            titleErrorLabel.setText("Title is required");
            titleErrorContainer.setVisible(true);
            titleErrorContainer.setManaged(true);
            System.out.println("TITLE ERROR: Title is required");
            isValid = false;
        } else if (title.length() < 5) {
            // Make title field red
            titleField.setStyle("-fx-border-color: #e74c3c;");
            // Show error directly under title field's parent HBox
            titleErrorLabel.setText("Title must be at least 5 characters");
            titleErrorContainer.setVisible(true);
            titleErrorContainer.setManaged(true);
            System.out.println("TITLE ERROR: Title too short");
            isValid = false;
        } else {
            // Title is valid, remove error styling
            titleField.setStyle("");
            titleErrorContainer.setVisible(false);
            titleErrorContainer.setManaged(false);
        }
        
        // Check content field
        String content = contentField.getText().trim();
        if (content.isEmpty()) {
            showFieldError(contentField, contentErrorContainer, contentErrorLabel, "Content is required");
            isValid = false;
        } else if (content.length() < 10) {
            showFieldError(contentField, contentErrorContainer, contentErrorLabel, "Content must be at least 10 characters");
            isValid = false;
        } else {
            // Content is valid, remove error styling
            contentField.getStyleClass().remove("input-error");
            contentErrorContainer.setVisible(false);
            contentErrorContainer.setManaged(false);
        }
        
        return isValid;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showBadWordAlert(List<String> badWords) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Inappropriate Content");
        alert.setHeaderText("Your comment contains inappropriate words");
        
        String badWordsList = String.join(", ", badWords);
        String content = "The following inappropriate words were found:\n" + badWordsList + 
                        "\n\nYour comment will be filtered before posting.";
        
        alert.setContentText(content);
        
        // Add custom styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/post.css").toExternalForm());
        dialogPane.getStyleClass().add("error-popup");
        
        alert.showAndWait();
    }

    // Helper method to create an ImageView for an emoji
    private ImageView createEmojiImageView(String emojiFileName, double size) {
        try {
            Image emojiImage = new Image(getClass().getResourceAsStream(EMOJI_PATH + emojiFileName));
            ImageView imageView = new ImageView(emojiImage);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            System.err.println("Error loading emoji image: " + emojiFileName + " - " + e.getMessage());
            // Fallback to text emoji
            Label fallbackLabel = new Label(getFallbackEmoji(emojiFileName));
            fallbackLabel.getStyleClass().add("emoji-fallback");
            StackPane container = new StackPane(fallbackLabel);
            container.setMinSize(size, size);
            container.setMaxSize(size, size);
            return null;
        }
    }
    
    // Get fallback text emoji
    private String getFallbackEmoji(String emojiFileName) {
        if (emojiFileName.contains("Thumbs-Up")) return "👍";
        if (emojiFileName.contains("Grinning")) return "😊";
        if (emojiFileName.contains("Tears-Of-Joy")) return "😂";
        if (emojiFileName.contains("Star-Struck")) return "😮";
        if (emojiFileName.contains("Pensive")) return "😢";
        if (emojiFileName.contains("Pouting")) return "😡";
        return "😀";
    }

    // Helper method to get image filename from emoji code
    private String getEmojiFilenameFromCode(String emojiCode) {
        for (int i = 0; i < EMOJI_CODES.length; i++) {
            if (EMOJI_CODES[i].equals(emojiCode)) {
                return AVAILABLE_REACTIONS[i];
            }
        }
        return null; // Not found
    }

    // Add this method to show a temporary success message
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Auto-close after 1.5 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> alert.close());
        
        alert.show();
        delay.play();
    }

    @FXML
    private void handleGenerateImage() {
        if (!PexelsAPI.isApiKeySet()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Pexels API key is not set. Please configure it in PexelsAPI.java");
            return;
        }

        if (titleField.getText().trim().isEmpty() && contentField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a title or content to search for an image");
            return;
        }

        // Show loading indicator
        showLoadingIndicator("Searching for images...");

        // Prepare the search query from title and content
        String searchQuery = titleField.getText().trim() + " " + contentField.getText().trim();
        
        // Create a new thread for the API call
        new Thread(() -> {
            try {
                // Search for images using Pexels API (get 15 images)
                List<PexelsAPI.PexelsImage> images = PexelsAPI.searchImages(searchQuery, 15);
                
                if (!images.isEmpty()) {
                    // Create image selection dialog on JavaFX thread
                    Platform.runLater(() -> {
                        Dialog<PexelsAPI.PexelsImage> dialog = new Dialog<>();
                        dialog.setTitle("Select Image");
                        dialog.setHeaderText("Choose an image to use");
                        
                        // Add buttons
                        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
                        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

                        // Create grid for images
                        GridPane grid = new GridPane();
                        grid.setHgap(15);
                        grid.setVgap(15);
                        grid.setPadding(new Insets(20, 20, 20, 20));

                        // Create a toggle group for radio buttons
                        ToggleGroup group = new ToggleGroup();
                        
                        // Add images to grid
                        int col = 0;
                        int row = 0;
                        for (PexelsAPI.PexelsImage img : images) {
                            try {
                                // Create image container
                                StackPane imageStack = new StackPane();
                                imageStack.setMinSize(150, 150);
                                imageStack.setMaxSize(150, 150);
                                
                                // Add initial loading indicator
                                ProgressIndicator loading = new ProgressIndicator();
                                loading.setMaxSize(50, 50);
                                imageStack.getChildren().add(loading);

                                // Create image view
                                ImageView imageView = new ImageView();
                                imageView.setFitWidth(150);
                                imageView.setFitHeight(150);
                                imageView.setPreserveRatio(true);

                                // Use the preloaded image instead of loading from URL
                                if (img.isPreloaded()) {
                                    try {
                                        Image image = img.getImage();
                                        imageView.setImage(image);
                                        imageStack.getChildren().remove(loading);
                                        imageStack.getChildren().add(0, imageView);
                                    } catch (Exception e) {
                                        System.err.println("Error using preloaded image: " + e.getMessage());
                                        Platform.runLater(() -> {
                                            imageStack.getChildren().clear();
                                            Label errorLabel = new Label("Failed to load");
                                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                                            imageStack.getChildren().add(errorLabel);
                                        });
                                    }
                                } else {
                                    // Fallback to URL loading if image is not preloaded
                                    Image image = new Image(img.getUrl(), 
                                        150, 150, true, true, true); // background loading

                                    // Add proper error handlers and timeout management
                                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                                        if (newVal) {
                                            System.err.println("Error loading image: " + img.getUrl());
                                            imageStack.getChildren().clear();
                                            Label errorLabel = new Label("Failed to load");
                                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                                            imageStack.getChildren().add(errorLabel);
                                        }
                                    });

                                    // Use a timeout for loading images
                                    PauseTransition timeout = new PauseTransition(Duration.seconds(10));
                                    timeout.setOnFinished(evt -> {
                                        if (image.getProgress() < 1.0 && !image.isError()) {
                                            System.err.println("Image loading timeout: " + img.getUrl());
                                            Platform.runLater(() -> {
                                                imageStack.getChildren().clear();
                                                Label timeoutLabel = new Label("Loading timeout");
                                                timeoutLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 12px;");
                                                imageStack.getChildren().add(timeoutLabel);
                                            });
                                        }
                                    });
                                    timeout.play();

                                    // Add progress listener to remove loading indicator when complete
                                    image.progressProperty().addListener((obs, oldVal, newVal) -> {
                                        if (newVal.doubleValue() == 1.0) {
                                            Platform.runLater(() -> {
                                                imageStack.getChildren().remove(loading);
                                                if (imageView.getImage() == null) {
                                                    imageView.setImage(image);
                                                    imageStack.getChildren().add(0, imageView);
                                                }
                                            });
                                        }
                                    });

                                    // Pre-set the image on the ImageView
                                    imageView.setImage(image);
                                    imageStack.getChildren().add(0, imageView);
                                }

                                // Create radio button
                                RadioButton rb = new RadioButton();
                                rb.setToggleGroup(group);
                                rb.setUserData(img);
                                
                                // Create container for image and radio button
                                VBox imageBox = new VBox(5);
                                imageBox.setAlignment(Pos.CENTER);
                                imageBox.getChildren().addAll(imageStack, rb);
                                
                                // Add photographer credit
                                Label creditLabel = new Label("Photo by: " + img.getPhotographer());
                                creditLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
                                creditLabel.setWrapText(true);
                                imageBox.getChildren().add(creditLabel);

                                // Style the container
                                imageBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: white;");
                                
                                // Add hover effect
                                imageBox.setOnMouseEntered(e -> 
                                    imageBox.setStyle("-fx-padding: 10; -fx-border-color: #0088cc; -fx-border-width: 1; -fx-background-color: #f8f9fa;"));
                                imageBox.setOnMouseExited(e -> 
                                    imageBox.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 1; -fx-background-color: white;"));
                                
                                // Make the whole box clickable
                                imageBox.setOnMouseClicked(e -> rb.setSelected(true));

                                grid.add(imageBox, col, row);
                                
                                col++;
                                if (col > 2) { // 3 images per row
                                    col = 0;
                                    row++;
                                }
                            } catch (Exception e) {
                                System.err.println("Error loading preview for image: " + e.getMessage());
                            }
                        }

                        // Add grid to scrollpane
                        ScrollPane scrollPane = new ScrollPane(grid);
                        scrollPane.setFitToWidth(true);
                        scrollPane.setPrefHeight(500);
                        scrollPane.setPrefWidth(600);
                        scrollPane.setStyle("-fx-background: white;");
                        
                        // Style the dialog
                        dialog.getDialogPane().setStyle("-fx-background-color: white;");
                        dialog.getDialogPane().setPrefWidth(650);
                        dialog.getDialogPane().setPrefHeight(600);
                        dialog.getDialogPane().setContent(scrollPane);

                        // Hide loading indicator
                        hideLoadingIndicator();

                        // Enable/disable select button based on selection
                        Node selectButton = dialog.getDialogPane().lookupButton(selectButtonType);
                        selectButton.setDisable(true);
                        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> 
                            selectButton.setDisable(newVal == null));

                        // Convert the result
                        dialog.setResultConverter(dialogButton -> {
                            if (dialogButton == selectButtonType) {
                                Toggle selected = group.getSelectedToggle();
                                return selected != null ? (PexelsAPI.PexelsImage)selected.getUserData() : null;
                            }
                            return null;
                        });

                        // Show dialog and handle result
                        Optional<PexelsAPI.PexelsImage> result = dialog.showAndWait();
                        result.ifPresent(selectedImage -> {
                            showLoadingIndicator("Downloading selected image...");
                            
                            // Check if the image is already preloaded
                            if (selectedImage.isPreloaded()) {
                                Platform.runLater(() -> {
                                    try {
                                        // Use the already loaded base64 data
                                        String base64Image = selectedImage.getBase64Data();
                                        
                                        // Save the base64 image data
                                        imageField.setText(base64Image);
                                        
                                        // Use the already loaded image for preview
                                        Image image = selectedImage.getImage();
                                        if (image != null) {
                                            imagePreview.setImage(image);
                                            imagePreviewContainer.setVisible(true);
                                            imagePreviewContainer.setManaged(true);
                                        } else {
                                            // Fallback - decode base64 and create a new image
                                            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                            image = new Image(new ByteArrayInputStream(imageBytes));
                                            imagePreview.setImage(image);
                                            imagePreviewContainer.setVisible(true);
                                            imagePreviewContainer.setManaged(true);
                                        }
                                        
                                        hideLoadingIndicator();
                                    } catch (Exception e) {
                                        hideLoadingIndicator();
                                        showAlert(Alert.AlertType.ERROR, "Error", 
                                            "Failed to process image: " + e.getMessage());
                                    }
                                });
                            } else {
                                // Download the selected image if not already preloaded
                                new Thread(() -> {
                                    try {
                                        String base64Image = PexelsAPI.downloadImageAsBase64(selectedImage.getUrl());
                                        
                                        Platform.runLater(() -> {
                                            try {
                                                // Save the base64 image data
                                                imageField.setText(base64Image);
                                                
                                                // Convert base64 to image for preview
                                                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                                Image image = new Image(new ByteArrayInputStream(imageBytes));
                                                imagePreview.setImage(image);
                                                imagePreviewContainer.setVisible(true);
                                                imagePreviewContainer.setManaged(true);
                                                
                                                hideLoadingIndicator();
                                            } catch (Exception e) {
                                                hideLoadingIndicator();
                                                showAlert(Alert.AlertType.ERROR, "Error", 
                                                    "Failed to process downloaded image: " + e.getMessage());
                                            }
                                        });
                                    } catch (Exception e) {
                                        Platform.runLater(() -> {
                                            hideLoadingIndicator();
                                            showAlert(Alert.AlertType.ERROR, "Error", 
                                                "Failed to download image: " + e.getMessage());
                                        });
                                    }
                                }).start();
                            }
                        });
                    });
                } else {
                    Platform.runLater(() -> {
                        hideLoadingIndicator();
                        showAlert(Alert.AlertType.WARNING, "No Results", 
                            "No images found for your search query. Try different keywords.");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoadingIndicator();
                    showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to search for images: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showLoadingIndicator(String message) {
        Platform.runLater(() -> {
            VBox loadingBox = new VBox(10);
            loadingBox.getStyleClass().add("loading-indicator");
            loadingBox.setAlignment(Pos.CENTER);

            ProgressIndicator spinner = new ProgressIndicator();
            spinner.getStyleClass().add("loading-spinner");

            Label loadingText = new Label(message);
            loadingText.getStyleClass().add("loading-text");

            loadingBox.getChildren().addAll(spinner, loadingText);

            // Add to the image preview container
            imagePreviewContainer.getChildren().add(loadingBox);
            imagePreviewContainer.setVisible(true);
            imagePreviewContainer.setManaged(true);

            // Disable generate button
            generateImageButton.setDisable(true);
        });
    }

    private void hideLoadingIndicator() {
        Platform.runLater(() -> {
            // Remove loading indicator
            imagePreviewContainer.getChildren().removeIf(node -> 
                node instanceof VBox && node.getStyleClass().contains("loading-indicator"));
            
            // Re-enable generate button
            generateImageButton.setDisable(false);
        });
    }
}
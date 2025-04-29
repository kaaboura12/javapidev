package Controllers.backforum;

import Controllers.Basebackcontroller;
import Models.Post;
import Models.Comment;
import Models.Reaction;
import Services.ServicePost;
import Services.ServiceLike;
import Services.ServiceComment;
import Services.ServiceReaction;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Comparator;

public class BackPostcontroller implements Initializable, Basebackcontroller.BaseController {

    @FXML
    private TableView<PostWrapper> postsTable;

    @FXML
    private TableColumn<PostWrapper, Integer> idColumn;

    @FXML
    private TableColumn<PostWrapper, String> titleColumn;

    @FXML
    private TableColumn<PostWrapper, String> authorColumn;

    @FXML
    private TableColumn<PostWrapper, String> dateColumn;

    @FXML
    private TableColumn<PostWrapper, Integer> likesColumn;
    
    @FXML
    private TableColumn<PostWrapper, Integer> reactionsColumn;

    @FXML
    private TableColumn<PostWrapper, Void> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox topPostsContainer;

    @FXML private HBox emojiFilterContainer;
    @FXML private Button clearFilterBtn;
    
    private String currentEmojiFilter = null;

    private ObservableList<PostWrapper> postsList = FXCollections.observableArrayList();
    private FilteredList<PostWrapper> filteredPosts;
    private SortedList<PostWrapper> sortedPosts;
    private String currentUser;

    // Services for handling post operations
    private ServicePost postService;
    private ServiceLike likeService;
    private ServiceComment commentService;
    private ServiceReaction reactionService;

    // Emoji mapping for reactions
    private final Map<String, String> emojiMap = new HashMap<>();
    
    // Path to emoji images
    private final String EMOJI_PATH = "/Images/emojis/";
    
    // Map to store emoji images by type
    private final Map<String, ImageView> emojiImageMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the services
        postService = new ServicePost();
        likeService = new ServiceLike();
        commentService = new ServiceComment();
        reactionService = new ServiceReaction();
        
        // Initialize emoji mapping
        initializeEmojiMap();
        
        // Load emoji images
        loadEmojiImages();

        // Initialize the table columns and load data
        initializeTableColumns();
        setupSearchAndSortFunctionality();
        setupActionsColumn();
        
        // Setup emoji filter buttons
        setupEmojiFilterButtons();
        
        loadPosts();
        displayTopPosts();

        // Apply explicit styling to the table to ensure theme consistency
        applyTableStyling();
    }
    
    private void initializeEmojiMap() {
        emojiMap.put("like", "Thumbs-Up--Streamline-Kawaii-Emoji.png");
        emojiMap.put("happy", "Grinning-Face-With-Smiling-Eyes--Streamline-Kawaii-Emoji.png");
        emojiMap.put("laugh", "Face-With-Tears-Of-Joy--Streamline-Kawaii-Emoji.png");
        emojiMap.put("wow", "Star-Struck--Streamline-Kawaii-Emoji.png");
        emojiMap.put("sad", "Pensive-Face--Streamline-Kawaii-Emoji.png");
        emojiMap.put("angry", "Pouting-Face--Streamline-Kawaii-Emoji.png");
    }
    
    private void loadEmojiImages() {
        try {
            for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
                String type = entry.getKey();
                String fileName = entry.getValue();
                
                // Load image from resources
                Image image = new Image(getClass().getResourceAsStream(EMOJI_PATH + fileName));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                imageView.setPreserveRatio(true);
                
                emojiImageMap.put(type, imageView);
            }
            System.out.println("Emoji images loaded successfully");
        } catch (Exception e) {
            System.err.println("Error loading emoji images: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to get an ImageView clone for a specific reaction type
    private ImageView getEmojiImageView(String reactionType) {
        ImageView original = emojiImageMap.get(reactionType);
        if (original == null) {
            // Fallback to the first available emoji if the type isn't found
            original = emojiImageMap.values().iterator().next();
        }
        
        // Create a new ImageView with the same image to avoid issues with reusing nodes
        ImageView clone = new ImageView(original.getImage());
        clone.setFitWidth(original.getFitWidth());
        clone.setFitHeight(original.getFitHeight());
        clone.setPreserveRatio(true);
        return clone;
    }

    private void applyTableStyling() {
        // Add specific styling to ensure table appearance matches the theme
        postsTable.getStyleClass().add("posts-table");

        // Apply striped row styling
        postsTable.setRowFactory(tv -> {
            TableRow<PostWrapper> row = new TableRow<>();
            row.styleProperty().bind(javafx.beans.binding.Bindings.createStringBinding(() -> {
                if (!row.isEmpty()) {
                    int rowIndex = row.getIndex();
                    if (rowIndex % 2 == 0) {
                        return "-fx-background-color: #252525;";
                    } else {
                        return "-fx-background-color: #1e1e1e;";
                    }
                }
                return null;
            }, row.indexProperty(), row.emptyProperty()));
            return row;
        });

        // Override the default blue selection highlight
        postsTable.setStyle("-fx-selection-bar: linear-gradient(to right, rgba(255,106,0,0.6), rgba(238,9,121,0.6)); " +
                "-fx-selection-bar-non-focused: rgba(255,106,0,0.2); " +
                "-fx-control-inner-background: #252525; " +
                "-fx-control-inner-background-alt: #1e1e1e;");

        // Apply styling to all columns
        for (TableColumn<PostWrapper, ?> column : postsTable.getColumns()) {
            column.setStyle("-fx-alignment: CENTER-LEFT;");
        }
    }

    private void initializeTableColumns() {
        // Remove ID column from the table
        postsTable.getColumns().remove(idColumn);
        
        // Setup remaining cell value factories
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        likesColumn.setCellValueFactory(new PropertyValueFactory<>("likesCount"));
        reactionsColumn.setCellValueFactory(new PropertyValueFactory<>("reactionsCount"));

        // Set comparators for each column
        titleColumn.setComparator(String::compareToIgnoreCase);
        authorColumn.setComparator(String::compareToIgnoreCase);
        dateColumn.setComparator((date1, date2) -> {
            try {
                LocalDateTime dt1 = LocalDateTime.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime dt2 = LocalDateTime.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return dt1.compareTo(dt2);
            } catch (Exception e) {
                return 0;
            }
        });
        likesColumn.setComparator(Integer::compare);
        reactionsColumn.setComparator(Integer::compare);

        // Style headers to indicate sortable columns
        String sortableStyle = "; -fx-font-weight: bold;";
        titleColumn.setStyle(titleColumn.getStyle() + sortableStyle);
        authorColumn.setStyle(authorColumn.getStyle() + sortableStyle);
        dateColumn.setStyle(dateColumn.getStyle() + sortableStyle);
        likesColumn.setStyle(likesColumn.getStyle() + sortableStyle);
        reactionsColumn.setStyle(reactionsColumn.getStyle() + sortableStyle);

        // Style the likes and reactions columns to be centered
        likesColumn.setStyle(likesColumn.getStyle() + "; -fx-alignment: CENTER;");
        reactionsColumn.setStyle(reactionsColumn.getStyle() + "; -fx-alignment: CENTER;");

        // Add custom cell factory for likes column to show heart icon
        likesColumn.setCellFactory(column -> {
            return new TableCell<PostWrapper, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox container = new HBox(5);
                        container.setAlignment(Pos.CENTER);

                        Region heartIcon = new Region();
                        heartIcon.setStyle(
                            "-fx-background-color: #ff7e5f;" +
                            "-fx-shape: \"M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z\";" +
                            "-fx-min-width: 16px;" +
                            "-fx-min-height: 16px;" +
                            "-fx-pref-width: 16px;" +
                            "-fx-pref-height: 16px;"
                        );

                        Label countLabel = new Label(item.toString());
                        countLabel.setStyle("-fx-text-fill: white;");

                        container.getChildren().addAll(heartIcon, countLabel);
                        setGraphic(container);
                    }
                }
            };
        });
        
        // Add custom cell factory for reactions column
        reactionsColumn.setCellFactory(column -> {
            return new TableCell<PostWrapper, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox container = new HBox(5);
                        container.setAlignment(Pos.CENTER);
                        
                        PostWrapper post = getTableView().getItems().get(getIndex());
                        Map<String, Integer> reactionsMap = post.getReactionDetails();
                        
                        if (reactionsMap.isEmpty()) {
                            ImageView placeholderEmoji = getEmojiImageView("happy");
                            Label countLabel = new Label(item.toString());
                            countLabel.setStyle("-fx-text-fill: white;");
                            container.getChildren().addAll(placeholderEmoji, countLabel);
                        } else {
                            String topReactionType = "";
                            int maxCount = 0;
                            
                            for (Map.Entry<String, Integer> entry : reactionsMap.entrySet()) {
                                if (entry.getValue() > maxCount) {
                                    maxCount = entry.getValue();
                                    topReactionType = entry.getKey();
                                }
                            }
                            
                            ImageView emojiImageView = getEmojiImageView(topReactionType);
                            Label countLabel = new Label(item.toString());
                            countLabel.setStyle("-fx-text-fill: white;");
                            container.getChildren().addAll(emojiImageView, countLabel);
                        }
                        
                        setGraphic(container);
                    }
                }
            };
        });

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        // Setup actions column with View and Delete buttons
        actionsColumn.setCellFactory(column -> {
            return new TableCell<PostWrapper, Void>() {
                private final Button viewBtn = new Button("View");
                private final Button deleteBtn = new Button("Delete");
                private final HBox buttonsBox = new HBox(5, viewBtn, deleteBtn);

                {
                    // Style view button
                    viewBtn.getStyleClass().add("view-button");

                    // Style delete button
                    deleteBtn.getStyleClass().add("delete-button");

                    // Style button container
                    buttonsBox.setAlignment(Pos.CENTER);

                    viewBtn.setOnAction(event -> {
                        PostWrapper post = getTableView().getItems().get(getIndex());
                        viewPost(post);
                    });

                    deleteBtn.setOnAction(event -> {
                        PostWrapper post = getTableView().getItems().get(getIndex());
                        deletePost(post);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonsBox);
                    }
                }
            };
        });
    }

    private void setupEmojiFilterButtons() {
        emojiFilterContainer.getChildren().clear();
        
        // Create a button for each reaction type
        for (Map.Entry<String, String> entry : emojiMap.entrySet()) {
            String reactionType = entry.getKey();
            
            // Create button with emoji image only
            Button emojiButton = new Button();
            emojiButton.getStyleClass().add("emoji-filter-button");
            
            // Add only the emoji image, no count
            ImageView emojiView = getEmojiImageView(reactionType);
            emojiButton.setGraphic(emojiView);
            
            // Add tooltip
            Tooltip.install(emojiButton, new Tooltip("Filter by " + reactionType + " reactions"));
            
            // Add click handler
            emojiButton.setOnAction(e -> handleEmojiFilter(reactionType, emojiButton));
            
            emojiFilterContainer.getChildren().add(emojiButton);
        }
    }
    
    private int getTotalReactionCount(String reactionType) {
        int total = 0;
        for (PostWrapper post : postsList) {
            Map<String, Integer> reactions = post.getReactionDetails();
            if (reactions.containsKey(reactionType)) {
                total += reactions.get(reactionType);
            }
        }
        return total;
    }
    
    private void handleEmojiFilter(String reactionType, Button clickedButton) {
        // Clear previous selection
        emojiFilterContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("selected");
            }
        });
        
        // Apply new filter
        if (currentEmojiFilter != null && currentEmojiFilter.equals(reactionType)) {
            // If clicking the same filter, clear it
            currentEmojiFilter = null;
        } else {
            // Apply new filter
            currentEmojiFilter = reactionType;
            clickedButton.getStyleClass().add("selected");
        }
        
        // Update the filtered list
        updateFilters();
    }
    
    @FXML
    private void clearEmojiFilter() {
        currentEmojiFilter = null;
        emojiFilterContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("selected");
            }
        });
        updateFilters();
    }
    
    private void updateFilters() {
        filteredPosts.setPredicate(post -> {
            boolean matchesSearch = true;
            boolean matchesEmoji = true;
            
            // Apply search filter
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                searchText = searchText.toLowerCase();
                matchesSearch = String.valueOf(post.getId()).contains(searchText)
                    || post.getTitle().toLowerCase().contains(searchText)
                    || post.getAuthor().toLowerCase().contains(searchText)
                    || post.getDate().toLowerCase().contains(searchText)
                    || String.valueOf(post.getLikesCount()).contains(searchText)
                    || String.valueOf(post.getReactionsCount()).contains(searchText)
                    || post.getContent().toLowerCase().contains(searchText);
            }
            
            // Apply emoji filter
            if (currentEmojiFilter != null) {
                Map<String, Integer> reactions = post.getReactionDetails();
                matchesEmoji = reactions.containsKey(currentEmojiFilter) 
                    && reactions.get(currentEmojiFilter) > 0;
            }
            
            return matchesSearch && matchesEmoji;
        });
        
        updateStatusLabel();
    }

    private void setupSearchAndSortFunctionality() {
        // Initialize FilteredList
        filteredPosts = new FilteredList<>(postsList, p -> true);

        // Initialize SortedList
        sortedPosts = new SortedList<>(filteredPosts);
        sortedPosts.comparatorProperty().bind(postsTable.comparatorProperty());

        // Set the sorted/filtered data to the table
        postsTable.setItems(sortedPosts);

        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilters();
        });
    }

    private void displayTopPosts() {
        // Clear existing content
        topPostsContainer.getChildren().clear();
        
        // Get all posts and sort them by likes
        List<PostWrapper> allPosts = new ArrayList<>(postsList);
        allPosts.sort((p1, p2) -> Integer.compare(p2.getLikesCount(), p1.getLikesCount()));
        
        // Take top 3 posts
        List<PostWrapper> topPosts = allPosts.stream().limit(3).collect(java.util.stream.Collectors.toList());
        
        // Create header
        Label headerLabel = new Label("Top Posts");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 10 0;");
        topPostsContainer.getChildren().add(headerLabel);
        
        // Create cards for top posts
        HBox cardsContainer = new HBox(20);
        cardsContainer.setStyle("-fx-padding: 10 0 20 0;");
        
        for (int i = 0; i < topPosts.size(); i++) {
            PostWrapper post = topPosts.get(i);
            
            // Create card container
            VBox card = new VBox(10);
            card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.05);" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 15;" +
                "-fx-pref-width: 250;" +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 10, 0, 0, 0);"
            );
            
            // Medal icon based on position
            HBox medalBox = new HBox(5);
            medalBox.setAlignment(Pos.CENTER_LEFT);
            
            Region medalIcon = new Region();
            String medalColor = switch (i) {
                case 0 -> "#FFD700"; // Gold
                case 1 -> "#C0C0C0"; // Silver
                case 2 -> "#CD7F32"; // Bronze
                default -> "#808080";
            };
            
            medalIcon.setStyle(
                "-fx-background-color: " + medalColor + ";" +
                "-fx-shape: \"M9 .375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125h1.5c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125h-1.5zM3.375 7.5c0-1.036.84-1.875 1.875-1.875h.375v-1.5h-.375a3.375 3.375 0 0 0-3.375 3.375v1.5c0 .621.504 1.125 1.125 1.125h1.5c.621 0 1.125-.504 1.125-1.125v-1.5h-1.5v1.5h-.75v-1.5zm15-1.5v1.5h-.75v-1.5h-1.5v1.5c0 .621.504 1.125 1.125 1.125h1.5c.621 0 1.125-.504 1.125-1.125v-1.5a3.375 3.375 0 0 0-3.375-3.375h-.375v1.5h.375c1.035 0 1.875.84 1.875 1.875z\";" +
                "-fx-min-width: 24px;" +
                "-fx-min-height: 24px;" +
                "-fx-max-width: 24px;" +
                "-fx-max-height: 24px;"
            );
            
            Label positionLabel = new Label("#" + (i + 1));
            positionLabel.setStyle("-fx-text-fill: " + medalColor + "; -fx-font-weight: bold;");
            
            medalBox.getChildren().addAll(medalIcon, positionLabel);
            
            // Title
            Label titleLabel = new Label(post.getTitle());
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            titleLabel.setWrapText(true);
            
            // Author
            Label authorLabel = new Label(post.getAuthor());
            authorLabel.setStyle("-fx-text-fill: #a0a0a0;");
            
            // Likes count with heart icon
            HBox likesBox = new HBox(5);
            likesBox.setAlignment(Pos.CENTER_LEFT);
            
            Region heartIcon = new Region();
            heartIcon.setStyle(
                "-fx-background-color: #ff7e5f;" +
                "-fx-shape: \"M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z\";" +
                "-fx-min-width: 16px;" +
                "-fx-min-height: 16px;" +
                "-fx-max-width: 16px;" +
                "-fx-max-height: 16px;"
            );
            
            Label likesLabel = new Label(String.valueOf(post.getLikesCount()));
            likesLabel.setStyle("-fx-text-fill: white;");
            
            likesBox.getChildren().addAll(heartIcon, likesLabel);
            
            // View button
            Button viewButton = new Button("View Post");
            viewButton.getStyleClass().add("view-button");
            viewButton.setMaxWidth(Double.MAX_VALUE);
            viewButton.setOnAction(e -> viewPost(post));
            
            // Add all elements to card
            card.getChildren().addAll(medalBox, titleLabel, authorLabel, likesBox, viewButton);
            cardsContainer.getChildren().add(card);
        }
        
        topPostsContainer.getChildren().add(cardsContainer);
    }

    private void loadPosts() {
        postsList.clear();

        // Use the service to get all posts
        List<Post> posts = postService.getAllPosts();

        // Convert Post objects to PostWrapper objects for display
        for (Post post : posts) {
            int likesCount = likeService.countLikesForPost(post.getId());
            
            // Get reactions count
            Map<String, Integer> reactionCounts = reactionService.getReactionCountsByPostId(post.getId());
            int reactionsCount = 0;
            for (Integer count : reactionCounts.values()) {
                reactionsCount += count;
            }
            
            PostWrapper wrapper = new PostWrapper(
                    post.getId(),
                    post.getTitle(),
                    "User " + post.getAuthorId(),
                    post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    post.getContent(),
                    post.getImage(),
                    likesCount,
                    reactionsCount,
                    reactionCounts
            );
            postsList.add(wrapper);
        }

        updateStatusLabel();
        
        // Refresh top posts display
        displayTopPosts();
    }

    private void updateStatusLabel() {
        int totalPosts = postsList.size();
        int filteredPostsCount = sortedPosts.size();

        if (filteredPostsCount < totalPosts) {
            statusLabel.setText(String.format("Showing %d of %d posts", filteredPostsCount, totalPosts));
        } else {
            statusLabel.setText(String.format("Total Posts: %d", totalPosts));
        }
    }

    @FXML
    private void refreshPosts() {
        loadPosts();
        setupEmojiFilterButtons(); // Refresh emoji counts
        updateFilters();
    }

    private void viewPost(PostWrapper postWrapper) {
        try {
            Post post = postService.getPostById(postWrapper.getId());
            if (post == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Post Not Found",
                        "The post you're trying to view could not be found.");
                return;
            }

            Stage dialog = new Stage();
            dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(postsTable.getScene().getWindow());
            dialog.setWidth(600);
            dialog.setHeight(600);

            // Main container
            VBox mainLayout = new VBox(15);
            mainLayout.setPadding(new Insets(20));
            mainLayout.setStyle("-fx-background-color: #1e1e30;");

            // Post header (title and author)
            VBox headerBox = new VBox(10);
            headerBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-padding: 15;");

            Label titleLabel = new Label(post.getTitle());
            titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
            titleLabel.setWrapText(true);

            HBox authorDateBox = new HBox(15);
            Label authorLabel = new Label("User " + post.getAuthorId());
            authorLabel.setStyle("-fx-text-fill: #a0a0a0;");
            Label dateLabel = new Label("•");
            dateLabel.setStyle("-fx-text-fill: #a0a0a0;");
            Label postDate = new Label(post.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            postDate.setStyle("-fx-text-fill: #a0a0a0;");
            authorDateBox.getChildren().addAll(authorLabel, dateLabel, postDate);

            headerBox.getChildren().addAll(titleLabel, authorDateBox);

            // Content section
            VBox contentBox = new VBox(15);
            contentBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-padding: 15;");

            Label contentLabel = new Label(post.getContent());
            contentLabel.setWrapText(true);
            contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            contentBox.getChildren().add(contentLabel);

            // Image section (if exists)
            if (post.getImage() != null && !post.getImage().isEmpty()) {
                try {
                    String img = post.getImage();
                    Image image = null;
                    
                    // Check if it's a base64 image
                    if ((img.startsWith("data:image") && img.contains(",")) || 
                        (img.length() > 100 && img.matches("[A-Za-z0-9+/=]+"))) {
                        // Remove data URI prefix if present
                        String base64Data = img;
                        if (img.startsWith("data:image") && img.contains(",")) {
                            base64Data = img.substring(img.indexOf(",") + 1);
                        }
                        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                        image = new Image(new java.io.ByteArrayInputStream(imageBytes));
                    } else {
                        // Try as file path first, then as URL
                        File file = new File(img);
                        if (file.exists()) {
                            image = new Image(file.toURI().toString());
                        } else {
                            // Try as URL
                            image = new Image(img);
                        }
                    }
                    
                    if (image != null && !image.isError()) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(400);
                        imageView.setPreserveRatio(true);
                        // Add a container to center the image
                        StackPane imageContainer = new StackPane(imageView);
                        imageContainer.setMaxWidth(400);
                        imageContainer.setAlignment(Pos.CENTER);
                        contentBox.getChildren().add(imageContainer);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                }
            }

            // Engagement section (likes and reactions)
            HBox engagementBox = new HBox(20);
            engagementBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-padding: 10;");
            engagementBox.setAlignment(Pos.CENTER_LEFT);

            // Likes
            HBox likesBox = new HBox(5);
            likesBox.setAlignment(Pos.CENTER_LEFT);
            Region heartIcon = new Region();
            heartIcon.setStyle(
                "-fx-background-color: #ff7e5f;" +
                "-fx-shape: \"M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z\";" +
                "-fx-min-width: 16px; -fx-min-height: 16px;"
            );
            Label likesCount = new Label(String.valueOf(likeService.countLikesForPost(post.getId())));
            likesCount.setStyle("-fx-text-fill: white;");
            likesBox.getChildren().addAll(heartIcon, likesCount);

            // Reactions
            FlowPane reactionsBox = new FlowPane(10, 10);
            reactionsBox.setAlignment(Pos.CENTER_LEFT);
            
            Map<String, Integer> reactionCounts = reactionService.getReactionCountsByPostId(post.getId());
            for (Map.Entry<String, Integer> entry : reactionCounts.entrySet()) {
                if (entry.getValue() > 0) {
                    HBox reactionItem = new HBox(5);
                    reactionItem.setAlignment(Pos.CENTER);
                    
                    ImageView emojiView = getEmojiImageView(entry.getKey());
                    Label count = new Label(entry.getValue().toString());
                    count.setStyle("-fx-text-fill: white;");
                    
                    reactionItem.getChildren().addAll(emojiView, count);
                    reactionsBox.getChildren().add(reactionItem);
                }
            }

            engagementBox.getChildren().addAll(likesBox, reactionsBox);

            // Comments section
            VBox commentsBox = new VBox(10);
            commentsBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-padding: 15;");

            // Comments header with count
            HBox commentsHeader = new HBox(10);
            commentsHeader.setAlignment(Pos.CENTER_LEFT);
            Label commentsLabel = new Label("Comments");
            commentsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            commentsHeader.getChildren().add(commentsLabel);

            // Comments list container
            VBox commentsList = new VBox(10);
            commentsList.setStyle("-fx-background-color: transparent;");
            List<Comment> comments = commentService.getCommentsByPostId(post.getId());
            
            if (comments.isEmpty()) {
                HBox noCommentsBox = new HBox();
                noCommentsBox.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.03);" +
                    "-fx-background-radius: 5;" +
                    "-fx-padding: 10;" +
                    "-fx-alignment: center-left;"
                );
                
                Label noCommentsLabel = new Label("No comments yet");
                noCommentsLabel.setStyle("-fx-text-fill: #a0a0a0; -fx-font-style: italic;");
                
                noCommentsBox.getChildren().add(noCommentsLabel);
                commentsList.getChildren().add(noCommentsBox);
            } else {
                for (Comment comment : comments) {
                    VBox commentBox = new VBox(5);
                    commentBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.03); -fx-background-radius: 5; -fx-padding: 10;");

                    HBox commentHeader = new HBox(10);
                    Label commentAuthor = new Label("User " + comment.getUserId());
                    commentAuthor.setStyle("-fx-text-fill: #a0a0a0; -fx-font-weight: bold;");
                    Label commentDate = new Label(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                    commentDate.setStyle("-fx-text-fill: #a0a0a0;");
                    commentHeader.getChildren().addAll(commentAuthor, new Label("•"), commentDate);

                    // Create content container for text and possible GIF
                    VBox commentContentContainer = new VBox(8);
                    
                    // Always show text content
                    Label commentContent = new Label(comment.getContent());
                    commentContent.setStyle("-fx-text-fill: white;");
                    commentContent.setWrapText(true);
                    commentContentContainer.getChildren().add(commentContent);
                    
                    // Check for GIF in comment
                    String gifUrl = comment.getGifUrl();
                    if (gifUrl != null && !gifUrl.isEmpty()) {
                        try {
                            // Create image view for GIF
                            Image gifImage = new Image(gifUrl, true); // true enables background loading
                            ImageView gifView = new ImageView(gifImage);
                            gifView.setFitWidth(200); // Set appropriate width for comment GIFs
                            gifView.setPreserveRatio(true);
                            
                            // Add to a container to handle layout
                            StackPane gifContainer = new StackPane(gifView);
                            gifContainer.setMaxWidth(200);
                            gifContainer.setAlignment(Pos.CENTER_LEFT);
                            gifContainer.setStyle("-fx-padding: 5 0 0 0;");
                            
                            // Add GIF container to comment content
                            commentContentContainer.getChildren().add(gifContainer);
                        } catch (Exception e) {
                            System.err.println("Error loading GIF in comment: " + e.getMessage());
                        }
                    }

                    commentBox.getChildren().addAll(commentHeader, commentContentContainer);
                    commentsList.getChildren().add(commentBox);
                }
            }

            commentsBox.getChildren().addAll(commentsHeader, commentsList);

            // Add all sections to main layout
            mainLayout.getChildren().addAll(headerBox, contentBox, engagementBox, commentsBox);

            // Wrap in ScrollPane with dark theme
            ScrollPane scrollPane = new ScrollPane(mainLayout);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle(
                "-fx-background: #1e1e30;" +
                "-fx-background-color: #1e1e30;" +
                "-fx-control-inner-background: #1e1e30;" +
                "-fx-faint-focus-color: transparent;" +
                "-fx-focus-color: transparent;" +
                "-fx-border-color: transparent;"
            );
            // Remove scroll pane borders
            scrollPane.getStyleClass().add("no-border");

            // Create scene with dark background
            Scene scene = new Scene(scrollPane);
            scene.setFill(javafx.scene.paint.Color.valueOf("#1e1e30"));
            scene.getStylesheets().add(getClass().getResource("/Styles/backposts.css").toExternalForm());

            // Close on ESC
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    dialog.close();
                }
            });

            dialog.setScene(scene);
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "View Error",
                    "An error occurred while trying to view the post.");
        }
    }

    private void deletePost(PostWrapper postWrapper) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Delete Post");
        confirmation.setContentText("Are you sure you want to delete this post?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Use the service to delete the post
            boolean success = postService.deletePost(postWrapper.getId());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Post Deleted",
                        "The post has been successfully deleted.");
                refreshPosts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed",
                        "Failed to delete the post. Please try again.");
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    // PostWrapper class for table display
    public static class PostWrapper {
        private final Integer id;
        private final String title;
        private final String author;
        private final String date;
        private final String content;
        private final String imagePath;
        private final int likesCount;
        private final int reactionsCount;
        private final Map<String, Integer> reactionDetails;

        public PostWrapper(Integer id, String title, String author, String date, String content, String imagePath, int likesCount, int reactionsCount, Map<String, Integer> reactionDetails) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.date = date;
            this.content = content;
            this.imagePath = imagePath;
            this.likesCount = likesCount;
            this.reactionsCount = reactionsCount;
            this.reactionDetails = reactionDetails;
        }

        public Integer getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getDate() {
            return date;
        }

        public String getContent() {
            return content;
        }

        public String getImagePath() {
            return imagePath;
        }

        public int getLikesCount() {
            return likesCount;
        }
        
        public int getReactionsCount() {
            return reactionsCount;
        }
        
        public Map<String, Integer> getReactionDetails() {
            return reactionDetails;
        }
    }
}

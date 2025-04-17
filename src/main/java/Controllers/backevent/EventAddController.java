package Controllers.backevent;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class EventAddController {
    @FXML private TextField eventNameField;
    @FXML private DatePicker eventDatePicker;
    @FXML private TextField eventTimeField;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField capacityField;
    @FXML private TextField priceField;
    @FXML private TextField missionField;
    @FXML private TextField donationObjectiveField;
    @FXML private TextField imageField;
    @FXML private Label selectedImageLabel;
    @FXML private VBox imageContainer;
    
    // Validation labels
    @FXML private Label eventNameValidation;
    @FXML private Label eventDateValidation;
    @FXML private Label eventTimeValidation;
    @FXML private Label locationValidation;
    @FXML private Label descriptionValidation;
    @FXML private Label capacityValidation;
    @FXML private Label priceValidation;
    @FXML private Label missionValidation;
    @FXML private Label donationValidation;
    @FXML private Label imageValidation;
    
    private ImageView imagePreview;
    private EventService eventService;
    private File selectedImageFile;
    private final String IMAGE_UPLOAD_DIR = "src/main/resources/Uploads/Events/";

    @FXML
    public void initialize() {
        eventService = new EventService();
        
        // Create the upload directory if it doesn't exist
        createUploadDirectory();
        
        // Initialize image preview
        imagePreview = new ImageView();
        imagePreview.setFitHeight(150);
        imagePreview.setFitWidth(200);
        imagePreview.setPreserveRatio(true);
        imagePreview.getStyleClass().add("selected-image-preview");
        
        // Set default date to today
        eventDatePicker.setValue(LocalDate.now());
        
        // Clear all validation labels initially
        clearValidationMessages();
        
        // Add input validation listeners
        setupValidationListeners();
        
        // Add responsive behavior
        setupResponsiveBehavior();
    }
    
    private void clearValidationMessages() {
        eventNameValidation.setText("");
        eventDateValidation.setText("");
        eventTimeValidation.setText("");
        locationValidation.setText("");
        descriptionValidation.setText("");
        capacityValidation.setText("");
        priceValidation.setText("");
        missionValidation.setText("");
        donationValidation.setText("");
        imageValidation.setText("");
    }
    
    private void setupValidationListeners() {
        // Event Name validation
        eventNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                eventNameField.getStyleClass().remove("valid-field");
                eventNameField.getStyleClass().add("error-field");
                eventNameValidation.setText("Event name is required");
            } else {
                eventNameField.getStyleClass().remove("error-field");
                eventNameField.getStyleClass().add("valid-field");
                eventNameValidation.setText("");
            }
        });
        
        // Event Date validation
        eventDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                eventDatePicker.getStyleClass().remove("valid-field");
                eventDatePicker.getStyleClass().add("error-field");
                eventDateValidation.setText("Event date is required");
            } else if (newValue.isBefore(LocalDate.now())) {
                eventDatePicker.getStyleClass().remove("valid-field");
                eventDatePicker.getStyleClass().add("error-field");
                eventDateValidation.setText("Event date cannot be in the past");
            } else {
                eventDatePicker.getStyleClass().remove("error-field");
                eventDatePicker.getStyleClass().add("valid-field");
                eventDateValidation.setText("");
            }
        });
        
        // Event Time validation
        eventTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                eventTimeField.getStyleClass().remove("valid-field");
                eventTimeField.getStyleClass().add("error-field");
                eventTimeValidation.setText("Event time is required");
            } else {
                try {
                    LocalTime.parse(newValue, DateTimeFormatter.ofPattern("HH:mm"));
                    eventTimeField.getStyleClass().remove("error-field");
                    eventTimeField.getStyleClass().add("valid-field");
                    eventTimeValidation.setText("");
                } catch (DateTimeParseException e) {
                    eventTimeField.getStyleClass().remove("valid-field");
                    eventTimeField.getStyleClass().add("error-field");
                    eventTimeValidation.setText("Invalid time format. Use HH:mm");
                }
            }
        });
        
        // Location validation
        locationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                locationField.getStyleClass().remove("valid-field");
                locationField.getStyleClass().add("error-field");
                locationValidation.setText("Location is required");
            } else {
                locationField.getStyleClass().remove("error-field");
                locationField.getStyleClass().add("valid-field");
                locationValidation.setText("");
            }
        });
        
        // Description validation
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                descriptionArea.getStyleClass().remove("valid-field");
                descriptionArea.getStyleClass().add("error-field");
                descriptionValidation.setText("Description is required");
            } else if (newValue.length() < 10) {
                descriptionArea.getStyleClass().remove("valid-field");
                descriptionArea.getStyleClass().add("error-field");
                descriptionValidation.setText("Description must be at least 10 characters");
            } else {
                descriptionArea.getStyleClass().remove("error-field");
                descriptionArea.getStyleClass().add("valid-field");
                descriptionValidation.setText("");
            }
        });
        
        // Capacity validation
        capacityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                capacityField.getStyleClass().remove("valid-field");
                capacityField.getStyleClass().add("error-field");
                capacityValidation.setText("Capacity is required");
            } else if (!newValue.matches("\\d*")) {
                capacityField.setText(oldValue);
                capacityField.getStyleClass().remove("valid-field");
                capacityField.getStyleClass().add("error-field");
                capacityValidation.setText("Only numbers allowed");
            } else {
                try {
                    int capacity = Integer.parseInt(newValue);
                    if (capacity <= 0) {
                        capacityField.getStyleClass().remove("valid-field");
                        capacityField.getStyleClass().add("error-field");
                        capacityValidation.setText("Capacity must be greater than 0");
                    } else {
                        capacityField.getStyleClass().remove("error-field");
                        capacityField.getStyleClass().add("valid-field");
                        capacityValidation.setText("");
                    }
                } catch (NumberFormatException e) {
                    capacityField.getStyleClass().remove("valid-field");
                    capacityField.getStyleClass().add("error-field");
                    capacityValidation.setText("Invalid capacity value");
                }
            }
        });
        
        // Price validation
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                priceField.getStyleClass().remove("valid-field");
                priceField.getStyleClass().add("error-field");
                priceValidation.setText("Price is required");
            } else if (!newValue.matches("\\d*\\.?\\d*")) {
                priceField.setText(oldValue);
                priceField.getStyleClass().remove("valid-field");
                priceField.getStyleClass().add("error-field");
                priceValidation.setText("Invalid price format");
            } else {
                try {
                    double price = Double.parseDouble(newValue);
                    if (price < 0) {
                        priceField.getStyleClass().remove("valid-field");
                        priceField.getStyleClass().add("error-field");
                        priceValidation.setText("Price cannot be negative");
                    } else {
                        priceField.getStyleClass().remove("error-field");
                        priceField.getStyleClass().add("valid-field");
                        priceValidation.setText("");
                    }
                } catch (NumberFormatException e) {
                    priceField.getStyleClass().remove("valid-field");
                    priceField.getStyleClass().add("error-field");
                    priceValidation.setText("Invalid price value");
                }
            }
        });
        
        // Mission validation
        missionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                missionField.getStyleClass().remove("valid-field");
                missionField.getStyleClass().add("error-field");
                missionValidation.setText("Mission is required");
            } else {
                missionField.getStyleClass().remove("error-field");
                missionField.getStyleClass().add("valid-field");
                missionValidation.setText("");
            }
        });
        
        // Donation Objective validation
        donationObjectiveField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                donationObjectiveField.getStyleClass().remove("valid-field");
                donationObjectiveField.getStyleClass().add("error-field");
                donationValidation.setText("Donation objective is required");
            } else if (!newValue.matches("\\d*\\.?\\d*")) {
                donationObjectiveField.setText(oldValue);
                donationObjectiveField.getStyleClass().remove("valid-field");
                donationObjectiveField.getStyleClass().add("error-field");
                donationValidation.setText("Invalid format");
            } else {
                try {
                    double donation = Double.parseDouble(newValue);
                    if (donation < 0) {
                        donationObjectiveField.getStyleClass().remove("valid-field");
                        donationObjectiveField.getStyleClass().add("error-field");
                        donationValidation.setText("Donation cannot be negative");
                    } else {
                        donationObjectiveField.getStyleClass().remove("error-field");
                        donationObjectiveField.getStyleClass().add("valid-field");
                        donationValidation.setText("");
                    }
                } catch (NumberFormatException e) {
                    donationObjectiveField.getStyleClass().remove("valid-field");
                    donationObjectiveField.getStyleClass().add("error-field");
                    donationValidation.setText("Invalid donation value");
                }
            }
        });
    }
    
    private void setupResponsiveBehavior() {
        // Make the image preview responsive to container size
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            if (imagePreview.getParent() != null) {
                double containerWidth = imageContainer.getWidth();
                if (containerWidth > 0) {
                    // Adjust preview size based on container width
                    double maxPreviewWidth = Math.min(containerWidth * 0.8, 300);
                    imagePreview.setFitWidth(maxPreviewWidth);
                }
            }
        };
        
        // Add the listener once the scene is available
        imageContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Scene scene = imageContainer.getScene();
                scene.widthProperty().addListener(stageSizeListener);
                scene.heightProperty().addListener(stageSizeListener);
            }
        });
    }
    
    @FXML
    private void handleImageUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        
        // Set image file extension filters
        FileChooser.ExtensionFilter imageFilter = 
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);
        
        // Show open file dialog
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);
        
        if (selectedImageFile != null) {
            // Display the selected file name in the field
            imageField.setText(selectedImageFile.getName());
            selectedImageLabel.setText("Selected: " + selectedImageFile.getName());
            
            // Update validation status for image
            imageField.getStyleClass().remove("error-field");
            imageField.getStyleClass().add("valid-field");
            imageValidation.setText("");
            
            // Display image preview
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                
                // Add the image preview to the container if it's not already there
                if (!imageContainer.getChildren().contains(imagePreview)) {
                    imageContainer.getChildren().add(0, imagePreview);
                }
                
                // Adjust preview size based on container size
                double containerWidth = imageContainer.getWidth();
                if (containerWidth > 0) {
                    double maxPreviewWidth = Math.min(containerWidth * 0.8, 300);
                    imagePreview.setFitWidth(maxPreviewWidth);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load image preview: " + e.getMessage());
            }
        } else {
            // User cancelled image selection
            imageField.getStyleClass().remove("valid-field");
            imageField.getStyleClass().add("error-field");
            imageValidation.setText("Image is required");
        }
    }
    
    private void createUploadDirectory() {
        File uploadDir = new File(IMAGE_UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create upload directory: " + IMAGE_UPLOAD_DIR);
            }
        }
    }
    
    private String saveImageToUploadDirectory() throws IOException {
        if (selectedImageFile == null) {
            return "";
        }
        
        // Create a unique filename using UUID
        String originalFileName = selectedImageFile.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Build the path to save the file
        Path targetPath = Paths.get(IMAGE_UPLOAD_DIR, newFileName);
        
        // Copy the file to our upload directory
        Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return the relative path to be stored in the database
        return "Uploads/Events/" + newFileName;
    }

    @FXML
    private void handleAddEvent(ActionEvent event) {
        if (validateInputs()) {
            try {
                // Save uploaded image and get its path
                String imagePath = saveImageToUploadDirectory();
                
                // Create event object with the form data
                String eventName = eventNameField.getText();
                LocalDate eventDate = eventDatePicker.getValue();
                LocalTime eventTime = LocalTime.parse(eventTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                String location = locationField.getText();
                String description = descriptionArea.getText();
                int capacity = Integer.parseInt(capacityField.getText());
                double price = Double.parseDouble(priceField.getText());
                String mission = missionField.getText();
                double donationObjective = Double.parseDouble(donationObjectiveField.getText());

                // Create and save the event
                Event newEvent = new Event();
                newEvent.setTitre(eventName);
                newEvent.setDescription(description);
                newEvent.setLieu(location);
                newEvent.setNombreBillets(capacity);
                newEvent.setImage(imagePath);
                newEvent.setTimestart(eventTime);
                newEvent.setEvent_mission(mission);
                newEvent.setDonation_objective(donationObjective);
                newEvent.setSeatprice(price);
                newEvent.setDateEvenement(LocalDateTime.of(eventDate, eventTime));

                eventService.insert(newEvent);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Event added successfully!");
                closeWindow(event);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save image: " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add event: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow(event);
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        // Vérifiez chaque champ et modifiez le style et les messages de validation en conséquence
        // Event name
        if (eventNameField.getText().trim().isEmpty()) {
            eventNameField.getStyleClass().add("error-field");
            eventNameValidation.setText("Event name is required");
            isValid = false;
        }

        // Event date
        if (eventDatePicker.getValue() == null) {
            eventDatePicker.getStyleClass().add("error-field");
            eventDateValidation.setText("Event date is required");
            isValid = false;
        } else if (eventDatePicker.getValue().isBefore(LocalDate.now())) {
            eventDatePicker.getStyleClass().add("error-field");
            eventDateValidation.setText("Event date cannot be in the past");
            isValid = false;
        }

        // Event time
        if (eventTimeField.getText().trim().isEmpty()) {
            eventTimeField.getStyleClass().add("error-field");
            eventTimeValidation.setText("Event time is required");
            isValid = false;
        } else {
            try {
                LocalTime.parse(eventTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                eventTimeField.getStyleClass().add("error-field");
                eventTimeValidation.setText("Invalid time format. Use HH:mm");
                isValid = false;
            }
        }

        // Location
        if (locationField.getText().trim().isEmpty()) {
            locationField.getStyleClass().add("error-field");
            locationValidation.setText("Location is required");
            isValid = false;
        }

        // Description
        if (descriptionArea.getText().trim().isEmpty()) {
            descriptionArea.getStyleClass().add("error-field");
            descriptionValidation.setText("Description is required");
            isValid = false;
        } else if (descriptionArea.getText().length() < 10) {
            descriptionArea.getStyleClass().add("error-field");
            descriptionValidation.setText("Description must be at least 10 characters");
            isValid = false;
        }

        // Capacity
        if (capacityField.getText().trim().isEmpty()) {
            capacityField.getStyleClass().add("error-field");
            capacityValidation.setText("Capacity is required");
            isValid = false;
        } else {
            try {
                int capacity = Integer.parseInt(capacityField.getText());
                if (capacity <= 0) {
                    capacityField.getStyleClass().add("error-field");
                    capacityValidation.setText("Capacity must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                capacityField.getStyleClass().add("error-field");
                capacityValidation.setText("Invalid capacity value");
                isValid = false;
            }
        }

        // Price
        if (priceField.getText().trim().isEmpty()) {
            priceField.getStyleClass().add("error-field");
            priceValidation.setText("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    priceField.getStyleClass().add("error-field");
                    priceValidation.setText("Price cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                priceField.getStyleClass().add("error-field");
                priceValidation.setText("Invalid price value");
                isValid = false;
            }
        }

        // Mission
        if (missionField.getText().trim().isEmpty()) {
            missionField.getStyleClass().add("error-field");
            missionValidation.setText("Mission is required");
            isValid = false;
        }

        // Donation objective
        if (donationObjectiveField.getText().trim().isEmpty()) {
            donationObjectiveField.getStyleClass().add("error-field");
            donationValidation.setText("Donation objective is required");
            isValid = false;
        } else {
            try {
                double donationObjective = Double.parseDouble(donationObjectiveField.getText());
                if (donationObjective < 0) {
                    donationObjectiveField.getStyleClass().add("error-field");
                    donationValidation.setText("Donation cannot be negative");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                donationObjectiveField.getStyleClass().add("error-field");
                donationValidation.setText("Invalid donation value");
                isValid = false;
            }
        }
        
        // Image
        if (selectedImageFile == null) {
            imageField.getStyleClass().add("error-field");
            imageValidation.setText("Event image is required");
            isValid = false;
        }

        return isValid;
    }
    
    private void resetErrorStyles() {
        eventNameField.getStyleClass().remove("error-field");
        eventDatePicker.getStyleClass().remove("error-field");
        eventTimeField.getStyleClass().remove("error-field");
        locationField.getStyleClass().remove("error-field");
        descriptionArea.getStyleClass().remove("error-field");
        capacityField.getStyleClass().remove("error-field");
        priceField.getStyleClass().remove("error-field");
        missionField.getStyleClass().remove("error-field");
        donationObjectiveField.getStyleClass().remove("error-field");
        imageField.getStyleClass().remove("error-field");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Apply custom styling to the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/eventdashboard.css").toExternalForm());
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/addevent.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.showAndWait();
    }

    private void closeWindow(ActionEvent event) {
        try {
            // Try to find the main content area (AnchorPane with ID "contentArea")
            AnchorPane contentArea = findContentArea((Node) event.getSource());
            
            if (contentArea != null) {
                // Load event list view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backevent/eventbacklist.fxml"));
                Parent root = loader.load();
                
                // Set anchors for the new view
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                
                // Clear content area and add event list view
                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
                
                // Update the title if possible
                updatePageTitle("Event List");
            } else {
                // Fallback: Close the current stage if in a separate window
                Node source = (Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to original behavior if there's an error
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }
    }
    
    /**
     * Find the main content area in parent hierarchy
     * @param node The starting node to search from
     * @return The content area AnchorPane or null if not found
     */
    private AnchorPane findContentArea(Node node) {
        if (node == null) return null;
        
        // Walk up the parent hierarchy
        Parent parent = node.getParent();
        while (parent != null) {
            // First, check if any children or the parent itself is the content area
            if (parent instanceof AnchorPane && "contentArea".equals(parent.getId())) {
                return (AnchorPane) parent;
            }
            
            // Look for contentArea in all scenes
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (child instanceof AnchorPane && "contentArea".equals(child.getId())) {
                    return (AnchorPane) child;
                }
                
                // Check if this child has children (recursive)
                if (child instanceof Parent) {
                    AnchorPane result = searchChildren((Parent) child);
                    if (result != null) {
                        return result;
                    }
                }
            }
            
            // Try parent's parent
            if (parent.getParent() != null) {
                parent = parent.getParent();
            } else {
                // Reached the root, try one more approach
                if (parent.getScene() != null && parent.getScene().getRoot() != null) {
                    // Try searching the scene root
                    AnchorPane result = searchChildren(parent.getScene().getRoot());
                    if (result != null) {
                        return result;
                    }
                    break;
                } else {
                    break;
                }
            }
        }
        return null;
    }
    
    /**
     * Search children of a parent node for the content area
     * @param parent The parent to search within
     * @return The content area AnchorPane or null if not found
     */
    private AnchorPane searchChildren(Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof AnchorPane && "contentArea".equals(child.getId())) {
                return (AnchorPane) child;
            }
            
            if (child instanceof Parent) {
                AnchorPane result = searchChildren((Parent) child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     * Try to update the page title in the parent controller
     * @param title The title to set
     */
    private void updatePageTitle(String title) {
        try {
            Scene scene = eventNameField.getScene();
            if (scene == null) return;
            
            Parent root = scene.getRoot();
            if (root == null) return;
            
            // Look for the page title label
            Label pageTitleLabel = (Label) root.lookup("#pageTitle");
            if (pageTitleLabel != null) {
                pageTitleLabel.setText(title);
            }
        } catch (Exception e) {
            // Silently ignore - title update is not critical
            System.err.println("Could not update page title: " + e.getMessage());
        }
    }
}


package Controllers.backevent;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
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
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import java.util.Locale;

public class EventEditController implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private Spinner<Integer> ticketsSpinner;
    @FXML private TextField imageField;
    @FXML private TextField timeField;
    @FXML private TextField missionField;
    @FXML private TextField donationField;
    @FXML private TextField priceField;
    @FXML private DatePicker datePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private VBox imageContainer;
    @FXML private Label selectedImageLabel;

    // Validation message containers
    @FXML private VBox titleValidation;
    @FXML private VBox locationValidation;
    @FXML private VBox timeValidation;
    @FXML private VBox donationValidation;
    @FXML private VBox priceValidation;
    @FXML private VBox dateValidation;

    // Validation message labels
    @FXML private Label titleValidationLabel;
    @FXML private Label locationValidationLabel;
    @FXML private Label timeValidationLabel;
    @FXML private Label donationValidationLabel;
    @FXML private Label priceValidationLabel;
    @FXML private Label dateValidationLabel;

    private Event event;
    private EventService eventService;
    private boolean isSaved = false;
    private ImageView imagePreview;
    private File selectedImageFile;
    private final String IMAGE_UPLOAD_DIR = "src/main/resources/Uploads/Events/";
    private String currentImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        eventService = new EventService();
        
        // Create the upload directory if it doesn't exist
        createUploadDirectory();
        
        // Initialize image preview
        imagePreview = new ImageView();
        imagePreview.setFitHeight(150);
        imagePreview.setFitWidth(200);
        imagePreview.setPreserveRatio(true);
        imagePreview.getStyleClass().add("selected-image-preview");
        
        // Initialize validation labels
        initializeValidationLabels();
        
        // Add responsive behavior
        setupResponsiveBehavior();
        
        // Setup validation listeners - moved after field population
    }
    
    private void initializeValidationLabels() {
        // Hide all validation labels initially
        titleValidationLabel.setVisible(false);
        locationValidationLabel.setVisible(false);
        timeValidationLabel.setVisible(false);
        donationValidationLabel.setVisible(false);
        priceValidationLabel.setVisible(false);
        dateValidationLabel.setVisible(false);
        
        // Apply validation message style
        titleValidationLabel.getStyleClass().add("validation-message");
        locationValidationLabel.getStyleClass().add("validation-message");
        timeValidationLabel.getStyleClass().add("validation-message");
        donationValidationLabel.getStyleClass().add("validation-message");
        priceValidationLabel.getStyleClass().add("validation-message");
        dateValidationLabel.getStyleClass().add("validation-message");
    }

    private void initializeValidationContainers() {
        // This method is no longer needed but kept for backwards compatibility
    }

    private void setupValidationListeners() {
        // Title validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(titleValidationLabel, "Title is required");
            } else if (newValue.length() < 3) {
                showValidationError(titleValidationLabel, "Title must be at least 3 characters");
            } else {
                clearValidationError(titleValidationLabel);
            }
        });

        // Location validation
        locationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(locationValidationLabel, "Location is required");
            } else {
                clearValidationError(locationValidationLabel);
            }
        });

        // Time validation
        timeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    LocalTime.parse(newValue, DateTimeFormatter.ofPattern("HH:mm"));
                    clearValidationError(timeValidationLabel);
                } catch (DateTimeParseException e) {
                    showValidationError(timeValidationLabel, "Invalid time format (HH:mm)");
                }
            } else {
                clearValidationError(timeValidationLabel);
            }
        });

        // Donation validation - modified for better decimal handling
        donationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(donationValidationLabel, "Donation goal is required");
            } else {
                try {
                    double amount = Double.parseDouble(newValue.replace(",", "."));
                    if (amount < 0) {
                        showValidationError(donationValidationLabel, "Amount cannot be negative");
                    } else {
                        clearValidationError(donationValidationLabel);
                    }
                } catch (NumberFormatException e) {
                    showValidationError(donationValidationLabel, "Invalid amount");
                }
            }
        });

        // Price validation - modified for better decimal handling
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                showValidationError(priceValidationLabel, "Price is required");
            } else {
                try {
                    double price = Double.parseDouble(newValue.replace(",", "."));
                    if (price < 0) {
                        showValidationError(priceValidationLabel, "Price cannot be negative");
                    } else {
                        clearValidationError(priceValidationLabel);
                    }
                } catch (NumberFormatException e) {
                    showValidationError(priceValidationLabel, "Invalid price");
                }
            }
        });

        // Date validation
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                showValidationError(dateValidationLabel, "Date is required");
            } else if (newValue.isBefore(LocalDate.now())) {
                showValidationError(dateValidationLabel, "Date cannot be in the past");
            } else {
                clearValidationError(dateValidationLabel);
            }
        });
    }

    private void showValidationError(Label validationLabel, String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
    }

    private void clearValidationError(Label validationLabel) {
        validationLabel.setText("");
        validationLabel.setVisible(false);
    }

    private boolean isFormValid() {
        boolean isValid = true;
        
        // Reset validation styles
        resetValidationStyles();
        
        // Check title
        if (titleField.getText().trim().isEmpty() || titleField.getText().length() < 3) {
            showValidationError(titleValidationLabel, "Title is required and must be at least 3 characters");
            titleField.getStyleClass().add("error-field");
            isValid = false;
        }
        
        // Check location
        if (locationField.getText().trim().isEmpty()) {
            showValidationError(locationValidationLabel, "Location is required");
            locationField.getStyleClass().add("error-field");
            isValid = false;
        }
        
        // Check time format if provided
        if (!timeField.getText().trim().isEmpty()) {
            try {
                LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e) {
                showValidationError(timeValidationLabel, "Invalid time format (HH:mm)");
                timeField.getStyleClass().add("error-field");
                isValid = false;
            }
        }
        
        // Check donation - modified for better decimal handling
        try {
            double donation = parseDecimalField(donationField.getText());
            if (donation < 0) {
                showValidationError(donationValidationLabel, "Donation amount cannot be negative");
                donationField.getStyleClass().add("error-field");
                isValid = false;
            }
        } catch (Exception e) {
            showValidationError(donationValidationLabel, "Invalid donation amount");
            donationField.getStyleClass().add("error-field");
            isValid = false;
        }
        
        // Check price - modified for better decimal handling
        try {
            double price = parseDecimalField(priceField.getText());
            if (price < 0) {
                showValidationError(priceValidationLabel, "Price cannot be negative");
                priceField.getStyleClass().add("error-field");
                isValid = false;
            }
        } catch (Exception e) {
            showValidationError(priceValidationLabel, "Invalid price");
            priceField.getStyleClass().add("error-field");
            isValid = false;
        }
        
        // Check date
        if (datePicker.getValue() == null) {
            showValidationError(dateValidationLabel, "Date is required");
            datePicker.getStyleClass().add("error-field");
            isValid = false;
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            showValidationError(dateValidationLabel, "Date cannot be in the past");
            datePicker.getStyleClass().add("error-field");
            isValid = false;
        }
        
        return isValid;
    }
    
    private void resetValidationStyles() {
        // Remove error styles from all fields
        titleField.getStyleClass().remove("error-field");
        locationField.getStyleClass().remove("error-field");
        timeField.getStyleClass().remove("error-field");
        donationField.getStyleClass().remove("error-field");
        priceField.getStyleClass().remove("error-field");
        datePicker.getStyleClass().remove("error-field");
        
        // Clear all validation messages
        clearValidationError(titleValidationLabel);
        clearValidationError(locationValidationLabel);
        clearValidationError(timeValidationLabel);
        clearValidationError(donationValidationLabel);
        clearValidationError(priceValidationLabel);
        clearValidationError(dateValidationLabel);
    }

    public void setEvent(Event event) {
        this.event = event;
        populateFields();
        
        // Setup validation listeners AFTER populating fields
        // This prevents validation errors when the form is first loaded
        setupValidationListeners();
    }

    private void populateFields() {
        titleField.setText(event.getTitre());
        descriptionField.setText(event.getDescription());
        locationField.setText(event.getLieu());
        ticketsSpinner.getValueFactory().setValue(event.getNombreBillets());
        
        // Store current image path
        currentImagePath = event.getImage();
        
        // Display current image or placeholder
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            try {
                // Try to load the image from resources
                String imagePath = "/resources/" + currentImagePath;
                URL imageUrl = getClass().getResource(imagePath);
                
                // If resource not found, try as file path
                if (imageUrl == null) {
                    File imageFile = new File("src/main/" + currentImagePath);
                    if (imageFile.exists()) {
                        imageUrl = imageFile.toURI().toURL();
                    }
                }
                
                if (imageUrl != null) {
                    Image image = new Image(imageUrl.toString());
                    imagePreview.setImage(image);
                    imageContainer.getChildren().add(0, imagePreview);
                    selectedImageLabel.setText("Current image: " + currentImagePath.substring(currentImagePath.lastIndexOf('/') + 1));
                } else {
                    selectedImageLabel.setText("Current image path: " + currentImagePath);
                }
            } catch (Exception e) {
                System.err.println("Could not load current image: " + e.getMessage());
                selectedImageLabel.setText("Current image path: " + currentImagePath);
            }
        } else {
            selectedImageLabel.setText("No image set");
        }
        
        timeField.setText(event.getTimestart() != null ? event.getTimestart().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
        missionField.setText(event.getEvent_mission());
        
        // Format donation and price with proper locale to avoid decimal format issues
        donationField.setText(String.format(Locale.US, "%.2f", event.getDonation_objective()));
        priceField.setText(String.format(Locale.US, "%.2f", event.getSeatprice()));
        
        datePicker.setValue(event.getDateEvenement() != null ? event.getDateEvenement().toLocalDate() : null);
    }

    @FXML
    private void handleSave() {
        if (isFormValid()) {
            try {
                // Save any new image that was uploaded
                String imagePath = saveImageToUploadDirectory();
                
                // Update event with form data
                event.setTitre(titleField.getText().trim());
                event.setDescription(descriptionField.getText().trim());
                event.setLieu(locationField.getText().trim());
                event.setNombreBillets(ticketsSpinner.getValue());
                
                if (!imagePath.isEmpty()) {
                    event.setImage(imagePath);
                }
                
                // Parse time from the field
                LocalTime time = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                event.setTimestart(time);
                
                // Set event mission
                event.setEvent_mission(missionField.getText().trim());
                
                // Parse donation goal and seat price
                event.setDonation_objective(parseDecimalField(donationField.getText().trim()));
                event.setSeatprice(parseDecimalField(priceField.getText().trim()));
                
                // Set event date
                LocalDate date = datePicker.getValue();
                event.setDateEvenement(LocalDateTime.of(date, time));
                
                // Save the updated event
                eventService.update(event);
                
                isSaved = true;
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully");
                
                // Navigate back to event list
                navigateToEventList();
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save changes: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateToEventList();
    }
    
    /**
     * Navigate back to the event list
     */
    private void navigateToEventList() {
        try {
            // Try to find the main content area (AnchorPane with ID "contentArea")
            AnchorPane contentArea = findContentArea(cancelButton);
            
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
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();
            }
        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to original behavior if there's an error
            Stage stage = (Stage) cancelButton.getScene().getWindow();
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
        javafx.scene.Parent parent = node.getParent();
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
                if (child instanceof javafx.scene.Parent) {
                    AnchorPane result = searchChildren((javafx.scene.Parent) child);
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
    private AnchorPane searchChildren(javafx.scene.Parent parent) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof AnchorPane && "contentArea".equals(child.getId())) {
                return (AnchorPane) child;
            }
            
            if (child instanceof javafx.scene.Parent) {
                AnchorPane result = searchChildren((javafx.scene.Parent) child);
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
            Scene scene = cancelButton.getScene();
            if (scene == null) return;
            
            javafx.scene.Parent root = scene.getRoot();
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

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
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

    private void createUploadDirectory() {
        File uploadDir = new File(IMAGE_UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create upload directory: " + IMAGE_UPLOAD_DIR);
            }
        }
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
        }
    }
    
    private String saveImageToUploadDirectory() throws IOException {
        if (selectedImageFile == null) {
            // If no new image is selected, return the current path
            return currentImagePath;
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

    public boolean isSaved() {
        return isSaved;
    }

    // Helper method to safely parse decimal fields
    private double parseDecimalField(String value) {
        try {
            // Replace comma with period to handle different locale formats
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 
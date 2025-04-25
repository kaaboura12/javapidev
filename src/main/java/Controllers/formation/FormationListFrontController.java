package Controllers.formation;

import Models.Formation;
import Models.Categorie;
import Services.FormationService;
import Services.CategorieService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FormationListFrontController {
    @FXML private ComboBox<Categorie> categoryCombo;
    @FXML private TextField searchField;
    @FXML private FlowPane formationsContainer;
    @FXML private Pagination formationsPagination;
    
    private final FormationService formationService = new FormationService();
    private final CategorieService categorieService = new CategorieService();
    private List<Formation> formationsList;
    private ExecutorService executorService;
    private final int FORMATIONS_PER_PAGE = 6;

    @FXML
    public void initialize() {
        // Create thread pool for loading
        executorService = Executors.newFixedThreadPool(3);
        
        setupCategoryFilter();
        setupSearch();
        
        // Load formations asynchronously
        CompletableFuture.runAsync(this::loadAllFormations)
            .thenRun(() -> Platform.runLater(this::setupPagination));
    }
    
    private void setupCategoryFilter() {
        try {
            Categorie allCategories = new Categorie();
            allCategories.setId(-1);
            allCategories.setNom("All Categories");
            
            categoryCombo.getItems().add(allCategories);
            categoryCombo.getItems().addAll(categorieService.getAll());
            categoryCombo.setValue(allCategories);
            
            categoryCombo.setOnAction(e -> loadFormations());
        } catch (SQLException e) {
            showError("Failed to load categories", e);
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadFormations());
    }
    
    private void setupPagination() {
        if (formationsPagination != null) {
            int pageCount = (int) Math.ceil((double) formationsList.size() / FORMATIONS_PER_PAGE);
            formationsPagination.setPageCount(pageCount > 0 ? pageCount : 1);
            
            formationsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                displayFormationsForPage(newIndex.intValue());
            });
            
            // Display first page
            displayFormationsForPage(0);
        }
    }
    
    private void loadAllFormations() {
        try {
            formationsList = formationService.getAll();
        } catch (SQLException e) {
            Platform.runLater(() -> showError("Failed to load formations", e));
        }
    }

    private void loadFormations() {
        String searchText = searchField.getText().toLowerCase();
        Categorie selectedCategory = categoryCombo.getValue();
        
        // Filter formations based on search and category
        List<Formation> filteredFormations = formationsList.stream()
            .filter(f -> matchesSearch(f, searchText))
            .filter(f -> matchesCategory(f, selectedCategory))
            .collect(Collectors.toList());
        
        // Update pagination with filtered list
        int pageCount = (int) Math.ceil((double) filteredFormations.size() / FORMATIONS_PER_PAGE);
        Platform.runLater(() -> {
            formationsPagination.setPageCount(pageCount > 0 ? pageCount : 1);
            formationsPagination.setCurrentPageIndex(0);
            displayFormationsForPage(0, filteredFormations);
        });
    }
    
    private void displayFormationsForPage(int pageIndex) {
        displayFormationsForPage(pageIndex, formationsList);
    }
    
    private void displayFormationsForPage(int pageIndex, List<Formation> formations) {
        formationsContainer.getChildren().clear();
        
        int startIndex = pageIndex * FORMATIONS_PER_PAGE;
        int endIndex = Math.min(startIndex + FORMATIONS_PER_PAGE, formations.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            final int index = i;
            final Formation formation = formations.get(index);
            
            CompletableFuture.supplyAsync(() -> createFormationCard(formation), executorService)
                .thenAccept(card -> {
                    if (card != null) {
                        Platform.runLater(() -> {
                            // Add card with animation delay based on position
                            addCardWithAnimation(card, (index - startIndex) * 100);
                        });
                    }
                });
        }
    }
    
    private void addCardWithAnimation(Node card, int delay) {
        // Add the card to container
        formationsContainer.getChildren().add(card);
        
        // Initial state: transparent and slightly shifted down
        card.setOpacity(0);
        card.setTranslateY(20);
        
        // Create fade-in animation
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(600), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Create slide-up animation
        javafx.animation.TranslateTransition slideUp = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(500), card);
        slideUp.setFromY(20);
        slideUp.setToY(0);
        
        // Create scale animation
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(500), card);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);
        
        // Combine animations
        javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition(fadeIn, slideUp, scaleIn);
        parallelTransition.setDelay(javafx.util.Duration.millis(delay));
        
        // Play animation
        parallelTransition.play();
        
        // Add hover effect
        addHoverEffect(card);
    }
    
    private void addHoverEffect(Node card) {
        // Add hover effect with glow and scale
        card.setOnMouseEntered(e -> {
            card.getStyleClass().add("formation-card-hover");
            card.setCursor(javafx.scene.Cursor.HAND);
            
            // Add glow effect
            DropShadow glow = new DropShadow();
            glow.setColor(Color.valueOf("#3498db50"));
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(10);
            card.setEffect(glow);
            
            // Scale up
            javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.play();
        });
        
        card.setOnMouseExited(e -> {
            card.getStyleClass().remove("formation-card-hover");
            card.setCursor(javafx.scene.Cursor.DEFAULT);
            
            // Remove glow effect
            card.setEffect(null);
            
            // Scale back down
            javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), card);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    private boolean matchesSearch(Formation formation, String searchText) {
        if (searchText == null || searchText.isEmpty()) return true;
        return formation.getTitre().toLowerCase().contains(searchText) ||
               formation.getDescription().toLowerCase().contains(searchText);
    }

    private boolean matchesCategory(Formation formation, Categorie category) {
        return category == null || category.getId() == -1 || 
               formation.getCategorie().getId() == category.getId();
    }

    private Node createFormationCard(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/formation/formation_card.fxml"));
            Parent card = loader.load();
            FormationCardController controller = loader.getController();
            controller.setFormation(formation);
            return card;
        } catch (IOException e) {
            Platform.runLater(() -> showError("Failed to create formation card", e));
            return null;
        }
    }

    private void showError(String message, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message + "\n" + e.getMessage());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        });
    }
    
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
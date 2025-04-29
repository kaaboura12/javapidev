package Controllers.Article;

import Models.Article;
import Services.ServiceArticle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ArticleController implements ArticleRefreshListener {
    @FXML
    private TableView<Article> articlesTableView;
    @FXML
    private TableColumn<Article, Integer> idColumn;
    @FXML
    private TableColumn<Article, String> titreColumn;
    @FXML
    private TableColumn<Article, String> descriptionColumn;
    @FXML
    private TableColumn<Article, Double> prixColumn;
    @FXML
    private TableColumn<Article, LocalDate> datePubColumn;
    @FXML
    private TableColumn<Article, Boolean> disponibleColumn;
    @FXML
    private TableColumn<Article, Integer> nbrArticleColumn;
    @FXML
    private TableColumn<Article, Integer> nbrLikesColumn;
    @FXML
    private TableColumn<Article, String> categorieColumn;
    @FXML
    private TableColumn<Article, String> contenuColumn;
    
    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField prixField;
    @FXML
    private TextField categorieField;
    @FXML
    private ImageView contenuImageView;
    
    @FXML
    private Button newArticleBtn;
    
    @FXML
    private Button editBtn;
    
    @FXML
    private Button deleteBtn;
    
    @FXML
    private Button backToGaleriesBtn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button exportExcelBtn;
    
    private ServiceArticle serviceArticle;
    private Article articleSelectionne;
    private static final NumberFormat PRIX_FORMAT = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ObservableList<Article> allArticles;

    @FXML
    public void initialize() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidatabase", "root", "");
            serviceArticle = new ServiceArticle(con);
            
            // Configuration des colonnes
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
            datePubColumn.setCellValueFactory(new PropertyValueFactory<>("datePub"));
            datePubColumn.setCellFactory(column -> new TableCell<Article, LocalDate>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DATE_FORMATTER));
                    }
                }
            });
            disponibleColumn.setCellValueFactory(new PropertyValueFactory<>("disponible"));
            nbrArticleColumn.setCellValueFactory(new PropertyValueFactory<>("nbrArticle"));
            nbrLikesColumn.setCellValueFactory(new PropertyValueFactory<>("nbrLikes"));
            categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
            
            // Configuration de la colonne contenu pour afficher les images
            contenuColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));
            contenuColumn.setCellFactory(column -> new TableCell<Article, String>() {
                private final ImageView imageView = new ImageView();
                private final HBox hbox = new HBox();
                
                {
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    hbox.setAlignment(Pos.CENTER);
                    hbox.getChildren().add(imageView);
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        try {
                            String imagePath = item;
                            // Si le chemin ne commence pas par src/main/resources, l'ajouter
                            if (!imagePath.startsWith("src/main/resources/")) {
                                imagePath = "src/main/resources/" + imagePath;
                            }
                            
                            // Essayer de charger depuis les ressources
                            InputStream input = getClass().getResourceAsStream("/" + imagePath);
                            if (input != null) {
                                Image image = new Image(input);
                                imageView.setImage(image);
                                setGraphic(hbox);
                            } else {
                                // Essayer de charger depuis le système de fichiers
                                File imageFile = new File(imagePath);
                                if (imageFile.exists()) {
                                    Image image = new Image(imageFile.toURI().toString());
                                    imageView.setImage(image);
                                    setGraphic(hbox);
                                } else {
                                    setText("Image non trouvée");
                                    setGraphic(null);
                                }
                            }
                        } catch (Exception e) {
                            setText("Erreur d'image");
                            setGraphic(null);
                        }
                    }
                }
            });
            
            // Formater la colonne prix
            prixColumn.setCellFactory(column -> new TableCell<Article, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(PRIX_FORMAT.format(item));
                    }
                }
            });
            
            // Charger les données
            List<Article> articles = serviceArticle.findAll();
            allArticles = FXCollections.observableArrayList(articles);
            afficherArticles(articles);
            
            // Ajouter un listener pour la recherche
            if (searchField != null) {
                searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filterArticles(newValue);
                });
            }
            
            // Ajouter un listener pour la sélection
            articlesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    articleSelectionne = newSelection;
                    afficherDetailsArticle(newSelection);
                }
            });
            
            // Définir explicitement le texte des boutons
            if (newArticleBtn != null) {
                newArticleBtn.setText("Nouvel Article");
            }
            
            if (editBtn != null) {
                editBtn.setText("Modifier");
            }
            
            if (deleteBtn != null) {
                deleteBtn.setText("Supprimer");
            }
            if (exportExcelBtn != null) {
                exportExcelBtn.setText("Exporter Excel");
            }
            
            if (backToGaleriesBtn != null) {
                backToGaleriesBtn.setText("Retour aux Galeries");
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données", e);
        }
    }
    
    private void afficherArticles(List<Article> articles) {
        ObservableList<Article> articlesObservable = FXCollections.observableArrayList(articles);
        articlesTableView.setItems(articlesObservable);
    }
    
    private void afficherDetailsArticle(Article article) {
        titreField.setText(article.getTitre());
        descriptionField.setText(article.getDescription());
        prixField.setText(PRIX_FORMAT.format(article.getPrix()));
        categorieField.setText(article.getCategorie());
        
        // Charger l'image
        try {
            String contenu = article.getContenu();
            if (contenu != null && !contenu.isEmpty()) {
                // Construire le chemin complet
                String imagePath = contenu;
                if (!imagePath.startsWith("src/main/resources/UploadsGalerie/")) {
                    imagePath = "src/main/resources/UploadsGalerie/" + new File(contenu).getName();
                }
                
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    contenuImageView.setImage(image);
                } else {
                    contenuImageView.setImage(null);
                    System.err.println("Image non trouvée: " + imagePath);
                }
            } else {
                contenuImageView.setImage(null);
            }
        } catch (Exception e) {
            contenuImageView.setImage(null);
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
        }
    }
    
    private void afficherErreur(String titre, String message, Exception e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(message);
        if (e != null) {
            alert.setContentText(e.getMessage());
        }
        alert.showAndWait();
    }

    @FXML
    private void handleNouvelArticle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/AddArticle.fxml"));
            Parent root = loader.load();

            // Chercher dynamiquement la zone centrale "contentArea"
            AnchorPane contentArea = findContentArea(newArticleBtn);  // Utilise le bouton lui-même comme point de départ
            if (contentArea != null) {
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                contentArea.getChildren().setAll(root);
            } else {
                afficherErreur("Erreur", "Zone de contenu principale non trouvée", null);
            }

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible d'accéder au formulaire d'ajout d'article", e);
        }
    }


    @FXML
    private void handleModifier() {
        Article selectedArticle = articlesTableView.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/EditArticle.fxml"));
                Parent root = loader.load();

                // Passer l'article au contrôleur
                EditArticleController controller = loader.getController();
                controller.setArticleToEdit(selectedArticle);

                // 🔍 Trouver le contentArea dans la hiérarchie
                AnchorPane contentArea = findContentArea(articlesTableView);
                if (contentArea != null) {
                    AnchorPane.setTopAnchor(root, 0.0);
                    AnchorPane.setBottomAnchor(root, 0.0);
                    AnchorPane.setLeftAnchor(root, 0.0);
                    AnchorPane.setRightAnchor(root, 0.0);
                    contentArea.getChildren().setAll(root);
                } else {
                    afficherErreur("Erreur", "Impossible de trouver la zone de contenu principale.", null);
                }
            } catch (IOException e) {
                afficherErreur("Erreur", "Erreur lors du chargement du formulaire de modification", e);
            }
        } else {
            afficherErreur("Erreur", "Veuillez sélectionner un article à modifier", null);
        }
    }

    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle
            Scene currentScene = articlesTableView.getScene();
            if (currentScene != null) {
                // Remplacer le contenu de la scène
                currentScene.setRoot(root);
            }
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de charger la page", e);
        }
    }
    
    @FXML
    private void handleSupprimer() {
        try {
            if (articleSelectionne == null) {
                afficherErreur("Erreur", "Veuillez sélectionner un article à supprimer", null);
                return;
            }
            
            Alert confirmation = new Alert(AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer l'article");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet article ?");
            
            if (confirmation.showAndWait().get() == ButtonType.OK) {
                serviceArticle.delete(articleSelectionne);
                
                // Rafraîchir la liste
                List<Article> articles = serviceArticle.findAll();
                afficherArticles(articles);
                
                // Réinitialiser les champs
                titreField.clear();
                descriptionField.clear();
                prixField.clear();
                categorieField.clear();
                contenuImageView.setImage(null);
                articleSelectionne = null;
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de supprimer l'article", e);
        }
    }
    
    @Override
    public void onArticleAdded() {
        try {
            List<Article> articles = serviceArticle.findAll();
            afficherArticles(articles);
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de rafraîchir la liste", e);
        }
    }
    
    @FXML
    private void handleRetourGaleries() {
        try {
            loadPage("/Views/Galerie/ShowGalerie.fxml");
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de retourner à la page des galeries", e);
        }
    }
    @FXML
    private void handleRetourArticles() {
        try {
            loadPage("/Views/Galerie/ShowArticles.fxml");
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de retourner à la page des galeries", e);
        }
    }
    private AnchorPane findContentArea(Node node) {
        if (node == null) return null;
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof AnchorPane && "contentArea".equals(parent.getId())) {
                return (AnchorPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void filterArticles(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            articlesTableView.setItems(allArticles);
            return;
        }
        
        String lowerCaseSearch = searchText.toLowerCase();
        ObservableList<Article> filteredArticles = allArticles.filtered(article -> 
            article.getTitre().toLowerCase().contains(lowerCaseSearch) ||
            article.getDescription().toLowerCase().contains(lowerCaseSearch) ||
            article.getCategorie().toLowerCase().contains(lowerCaseSearch)
        );
        
        articlesTableView.setItems(filteredArticles);
    }

    @FXML
    private void handleExportExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Articles");

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Titre", "Description", "Prix", "Date Publication", 
                              "Disponible", "Quantité", "Likes", "Catégorie"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            int rowNum = 1;
            for (Article article : articlesTableView.getItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(article.getId());
                row.createCell(1).setCellValue(article.getTitre());
                row.createCell(2).setCellValue(article.getDescription());
                row.createCell(3).setCellValue(article.getPrix());
                row.createCell(4).setCellValue(article.getDatePub().format(DATE_FORMATTER));
                row.createCell(5).setCellValue(article.isDisponible() ? "Oui" : "Non");
                row.createCell(6).setCellValue(article.getNbrArticle());
                row.createCell(7).setCellValue(article.getNbrLikes());
                row.createCell(8).setCellValue(article.getCategorie());
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter vers Excel");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
            );
            
            String defaultFileName = "articles_" + 
                                  LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                                  ".xlsx";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) articlesTableView.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }
                workbook.close();

                Alert successAlert = new Alert(AlertType.INFORMATION);
                successAlert.setTitle("Exportation réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Les articles ont été exportés avec succès dans le fichier :\n" + file.getAbsolutePath());
                successAlert.showAndWait();
            } else {
                workbook.close();
            }

        } catch (IOException e) {
            afficherErreur("Erreur d'exportation", "Impossible d'exporter les articles vers Excel", e);
        }
    }

} 
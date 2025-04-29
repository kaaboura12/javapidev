package Controllers.Article;

import Models.Article;
import Models.Galerie;
import Services.ServiceArticle;
import Services.ServiceGalerie;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.regex.Pattern;

public class EditArticleController {
    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField prixField;
    @FXML
    private ComboBox<String> categorieComboBox;
    @FXML
    private ComboBox<Galerie> galerieComboBox;
    @FXML
    private TextField nbrArticleField;
    @FXML
    private CheckBox disponibleCheckBox;
    @FXML
    private TextField imagePathField;
    @FXML
    private ImageView imagePreview;
    
    private ServiceArticle serviceArticle;
    private ServiceGalerie serviceGalerie;
    private ArticleRefreshListener refreshListener;
    private Article articleToEdit;
    private File selectedImageFile;
    
    private static final Pattern TITRE_PATTERN = Pattern.compile("^\\D+$");

    @FXML
    public void initialize() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidatabase", "root", "");
            serviceArticle = new ServiceArticle(con);
            serviceGalerie = new ServiceGalerie(con);
            
            // Initialiser les catégories
            categorieComboBox.setItems(FXCollections.observableArrayList(
                "Peinture", "Sculpture", "Photographie", "DessinEtIllustration", 
                "ArtNumerique", "Architecture"
            ));
            
            // Charger les galeries
            List<Galerie> galeries = serviceGalerie.findAll();
            galerieComboBox.setItems(FXCollections.observableArrayList(galeries));
            
            // Configurer l'affichage des galeries dans le ComboBox
            galerieComboBox.setCellFactory(lv -> new ListCell<Galerie>() {
                @Override
                protected void updateItem(Galerie item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            
        } catch (Exception e) {
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données", e);
        }
    }

    public void setArticleToEdit(Article article) {
        this.articleToEdit = article;
        
        // Remplir les champs avec les données de l'article
        titreField.setText(article.getTitre());
        descriptionField.setText(article.getDescription());
        prixField.setText(String.valueOf(article.getPrix()));
        categorieComboBox.setValue(article.getCategorie());
        
        // Sélectionner la galerie
        for (Galerie galerie : galerieComboBox.getItems()) {
            if (galerie.getId() == article.getGalerieId()) {
                galerieComboBox.setValue(galerie);
                break;
            }
        }
        
        nbrArticleField.setText(String.valueOf(article.getNbrArticle()));
        disponibleCheckBox.setSelected(article.isDisponible());
        
        // Afficher l'image existante
        if (article.getContenu() != null && !article.getContenu().isEmpty()) {
            imagePathField.setText(article.getContenu());
            try {
                File imageFile = new File(article.getContenu());
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    imagePreview.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    public void setRefreshListener(ArticleRefreshListener listener) {
        this.refreshListener = listener;
    }

    @FXML
    private void handleParcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File file = fileChooser.showOpenDialog(imagePathField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imagePathField.setText(file.getAbsolutePath());
            
            // Afficher l'aperçu de l'image
            try {
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
            } catch (Exception e) {
                afficherErreur("Erreur", "Impossible de charger l'aperçu de l'image", e);
            }
        }
    }

    @FXML
    private void handleEnregistrer() {
        try {
            // Validation des champs
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            String prixText = prixField.getText().trim();
            String categorie = categorieComboBox.getValue();
            Galerie galerie = galerieComboBox.getValue();
            String nbrArticleText = nbrArticleField.getText().trim();
            boolean disponible = disponibleCheckBox.isSelected();
            
            // Validation du titre
            if (titre.isEmpty()) {
                afficherErreur("Erreur", "Le titre ne peut pas être vide", null);
                return;
            }
            
            if (!TITRE_PATTERN.matcher(titre).matches()) {
                afficherErreur("Erreur", "Le titre ne peut pas être uniquement numérique", null);
                return;
            }
            
            // Validation de la description
            if (description.isEmpty()) {
                afficherErreur("Erreur", "La description ne peut pas être vide", null);
                return;
            }
            
            if (description.length() < 10) {
                afficherErreur("Erreur", "La description doit contenir au moins 10 caractères", null);
                return;
            }
            
            // Validation du prix
            if (prixText.isEmpty()) {
                afficherErreur("Erreur", "Le prix est obligatoire", null);
                return;
            }
            
            double prix;
            try {
                prix = Double.parseDouble(prixText.replace(",", "."));
                if (prix <= 0) {
                    afficherErreur("Erreur", "Le prix doit être supérieur à 0", null);
                    return;
                }
            } catch (NumberFormatException e) {
                afficherErreur("Erreur", "Le prix doit être un nombre valide", null);
                return;
            }
            
            // Validation de la catégorie
            if (categorie == null || categorie.isEmpty()) {
                afficherErreur("Erreur", "Veuillez choisir une catégorie", null);
                return;
            }
            
            // Validation de la galerie
            if (galerie == null) {
                afficherErreur("Erreur", "Veuillez choisir une galerie", null);
                return;
            }
            
            // Validation de la quantité
            int nbrArticle = 0;
            if (!nbrArticleText.isEmpty()) {
                try {
                    nbrArticle = Integer.parseInt(nbrArticleText);
                    if (nbrArticle < 1) {
                        afficherErreur("Erreur", "Le nombre d'articles doit être au moins 1", null);
                        return;
                    }
                } catch (NumberFormatException e) {
                    afficherErreur("Erreur", "La quantité doit être un nombre entier valide", null);
                    return;
                }
            } else {
                afficherErreur("Erreur", "La quantité est obligatoire", null);
                return;
            }
            
            // Mise à jour de l'article
            articleToEdit.setTitre(titre);
            articleToEdit.setDescription(description);
            articleToEdit.setPrix(prix);
            articleToEdit.setCategorie(categorie);
            articleToEdit.setGalerieId(galerie.getId());
            articleToEdit.setDisponible(disponible);
            articleToEdit.setNbrArticle(nbrArticle);
            
            // Gestion de l'image
            if (selectedImageFile != null) {
                // Copier l'image dans un dossier d'upload et stocker le chemin
                String imagePath = copyImageToUploadFolder(selectedImageFile);
                articleToEdit.setContenu(imagePath);
            }
            
            // Mise à jour dans la base de données
            serviceArticle.update(articleToEdit);
            
            // Notifier le contrôleur principal
            if (refreshListener != null) {
                refreshListener.onArticleAdded();
            }
            
            // Rediriger vers la page des articles
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/ShowArticle.fxml"));
            Parent page = loader.load();

            // Trouver le contentArea depuis un champ quelconque, ici par exemple `titreField`
            AnchorPane contentArea = findContentArea(titreField);

            if (contentArea != null) {
                AnchorPane.setTopAnchor(page, 0.0);
                AnchorPane.setBottomAnchor(page, 0.0);
                AnchorPane.setLeftAnchor(page, 0.0);
                AnchorPane.setRightAnchor(page, 0.0);

                contentArea.getChildren().setAll(page);
            } else {
                afficherErreur("Erreur", "Impossible de trouver la zone de contenu du back-office", null);
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de modifier l'article", e);
        }
    }
    
    private String copyImageToUploadFolder(File sourceFile) {
        try {
            // Créer le dossier d'upload s'il n'existe pas
            File uploadDir = new File("src/main/resources/UploadsGalerie");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Générer un nom de fichier unique
            String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
            File destFile = new File(uploadDir, fileName);
            
            // Copier le fichier
            java.nio.file.Files.copy(
                sourceFile.toPath(), 
                destFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            // Retourner le chemin relatif
            return "src/main/resources/UploadsGalerie/" + fileName;
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de copier l'image", e);
            return null;
        }
    }

    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent root = loader.load();
            Scene scene = titreField.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de charger la page", e);
        }
    }

    @FXML
    private void handleAnnuler() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/ShowArticle.fxml"));
            Parent page = loader.load();

            // Trouver le contentArea dans la hiérarchie
            AnchorPane contentArea = findContentArea(titreField);

            if (contentArea != null) {
                AnchorPane.setTopAnchor(page, 0.0);
                AnchorPane.setBottomAnchor(page, 0.0);
                AnchorPane.setLeftAnchor(page, 0.0);
                AnchorPane.setRightAnchor(page, 0.0);
                contentArea.getChildren().setAll(page);
            } else {
                afficherErreur("Erreur", "Impossible de trouver la zone contentArea", null);
            }

        } catch (IOException e) {
            e.printStackTrace();
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

} 
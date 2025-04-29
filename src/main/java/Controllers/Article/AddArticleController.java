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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class AddArticleController {
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
    private File selectedImageFile;
    
    private static final Pattern TITRE_PATTERN = Pattern.compile("^\\D+$");
    private static final String UPLOAD_DIR = "src/main/resources/UploadsGalerie";

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
    private void handleAjouter() {
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
            
            // Création de l'article
            Article nouvelArticle = new Article();
            nouvelArticle.setTitre(titre);
            nouvelArticle.setDescription(description);
            nouvelArticle.setPrix(prix);
            nouvelArticle.setCategorie(categorie);
            nouvelArticle.setGalerieId(galerie.getId());
            nouvelArticle.setDatePub(LocalDate.now());
            nouvelArticle.setDisponible(disponible);
            nouvelArticle.setNbrArticle(nbrArticle);
            nouvelArticle.setNbrLikes(0);
            
            // Gestion de l'image
            if (selectedImageFile != null) {
                // Copier l'image dans un dossier d'upload et stocker le chemin
                String imagePath = copyImageToUploadFolder(selectedImageFile);
                nouvelArticle.setContenu(imagePath);
            }
            
            // Insertion dans la base de données
            serviceArticle.insert(nouvelArticle);
            
            // Notifier le contrôleur principal
            if (refreshListener != null) {
                refreshListener.onArticleAdded();
            }

            // Rediriger dans le contentArea (navigation interne)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/ShowArticle.fxml"));
            Parent page = loader.load();

            AnchorPane contentArea = findContentArea(titreField);  // ou un autre nœud de ton formulaire
            if (contentArea != null) {
                AnchorPane.setTopAnchor(page, 0.0);
                AnchorPane.setBottomAnchor(page, 0.0);
                AnchorPane.setLeftAnchor(page, 0.0);
                AnchorPane.setRightAnchor(page, 0.0);
                contentArea.getChildren().setAll(page);
            } else {
                afficherErreur("Erreur", "Zone de contenu principale introuvable", null);
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'ajouter l'article", e);
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

    @FXML
    private void handleAnnuler() {
        try {
            // Charger dynamiquement la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Article/ShowArticle.fxml"));
            Parent root = loader.load();

            // Rechercher le contentArea dynamiquement depuis n’importe quel composant (ex : titreField)
            AnchorPane contentArea = findContentArea(titreField);  // ou un autre nœud FXML
            if (contentArea != null) {
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                contentArea.getChildren().setAll(root);
            } else {
                afficherErreur("Erreur", "Zone de contenu principale introuvable", null);
            }

        } catch (IOException e) {
            afficherErreur("Erreur", "Impossible de charger la page des articles", e);
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
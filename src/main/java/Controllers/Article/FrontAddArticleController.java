package Controllers.Article;

import Models.Article;
import Models.Galerie;
import Services.ServiceArticle;
import Services.ServiceGalerie;
import Utils.MyDb;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.sql.SQLException;

public class FrontAddArticleController implements Initializable {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField nbrArticleField;
    @FXML
    private ComboBox<String> categoryField;
    @FXML
    private ComboBox<Galerie> galerieField;
    @FXML
    private TextField imagePathField;
    @FXML
    private ImageView imagePreview;
    @FXML
    private Label errorLabel;
    @FXML
    private HBox buttonsContainer; // injecté depuis le fx:id ajouté plus haut


    private ServiceArticle serviceArticle;
    private ServiceGalerie serviceGalerie;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "src/main/resources/UploadsGalerie";
    private Integer galerieId;
    private Article article;
    private boolean isEditMode = false;
    private FrontMonespaceController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Connection connection = MyDb.getInstance().getConnection();
            serviceArticle = new ServiceArticle(connection);
            serviceGalerie = new ServiceGalerie(connection);

            // Ajouter toutes les catégories
            categoryField.getItems().addAll(
                    "Peinture",
                    "Sculpture",
                    "Photographie",
                    "DessinEtIllustration",
                    "ArtNumerique",
                    "Architecture"
            );

            // Charger les galeries disponibles
            loadGaleries();

            // Configurer les validateurs
            setupValidators();
        } catch (Exception e) {
            showError("Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadGaleries() {
        try {
            List<Galerie> galeries = serviceGalerie.findAll();
            if (galeries != null && !galeries.isEmpty()) {
                galerieField.getItems().clear();
                galerieField.getItems().addAll(galeries);
                
                // Configurer l'affichage des galeries dans le ComboBox
                galerieField.setCellFactory(lv -> new ListCell<Galerie>() {
                    @Override
                    protected void updateItem(Galerie item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getNom());
                    }
                });
                galerieField.setButtonCell(new ListCell<Galerie>() {
                    @Override
                    protected void updateItem(Galerie item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : item.getNom());
                    }
                });
            } else {
                showError("Aucune galerie disponible. Veuillez d'abord créer une galerie.");
            }
        } catch (Exception e) {
            showError("Erreur lors du chargement des galeries: " + e.getMessage());
        }
    }

    private void setupValidators() {
        // Valider le prix (allow decimals)
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });

        // Valider la quantité (allow only digits)
        nbrArticleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nbrArticleField.setText(oldValue);
            }
        });
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
            try {
                // Vérifier la taille du fichier (max 5MB)
                if (file.length() > 5 * 1024 * 1024) {
                    showError("L'image ne doit pas dépasser 5MB");
                    return;
                }

                selectedImageFile = file;
                imagePathField.setText(file.getAbsolutePath());

                // Afficher l'aperçu de l'image
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setFitWidth(200);
                imagePreview.setFitHeight(200);
                imagePreview.setPreserveRatio(true);
            } catch (Exception e) {
                showError("Impossible de charger l'aperçu de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEnregistrer() {
        if (validateFields()) {
            try {
                // Pas de mode édition ici, toujours création
                createArticle();

                // Retourner à la page précédente via le parent
                if (parentController != null) {
                    parentController.reloadContentAndShowMainView();
                } else {
                    showError("Erreur : Contrôleur parent non défini.");
                }
            } catch (Exception e) {
                showError("Erreur lors de l'enregistrement: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        // Retourner à la page précédente via le parent
        if (parentController != null) {
            parentController.reloadContentAndShowMainView();
            } else {
            showError("Erreur : Contrôleur parent non défini.");
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (titleField.getText().trim().isEmpty()) {
            errors.append("Le titre est requis\n");
        }

        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("La description est requise\n");
        }

        if (priceField.getText().trim().isEmpty()) {
            errors.append("Le prix est requis\n");
        } else {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    errors.append("Le prix doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Le prix doit être un nombre valide\n");
            }
        }

        if (nbrArticleField.getText().trim().isEmpty()) {
            errors.append("La quantité est requise\n");
        } else {
            try {
                int quantity = Integer.parseInt(nbrArticleField.getText().trim());
                if (quantity <= 0) {
                    errors.append("La quantité doit être supérieure à 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("La quantité doit être un nombre entier valide\n");
            }
        }

        if (categoryField.getValue() == null) {
            errors.append("La catégorie est requise\n");
        }

        if (galerieField.getValue() == null) {
            errors.append("La galerie est requise\n");
        }

        if (selectedImageFile == null && (article == null || article.getContenu() == null || article.getContenu().isEmpty())) {
            errors.append("Une image est requise\n");
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }

        return true;
    }

    private String copyImageToUploadFolder(File sourceFile) {
        try {
            // Créer le dossier uploads/images s'il n'existe pas
            Path uploadDir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Vérifier les permissions du dossier
            if (!Files.isWritable(uploadDir)) {
                showError("Le dossier uploads/images n'a pas les permissions d'écriture");
                return null;
            }

            // Générer un nom de fichier unique avec la date
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = timestamp + "_" + System.currentTimeMillis() + "_" + sourceFile.getName();
            Path destFile = uploadDir.resolve(fileName);

            // Copier le fichier avec remplacement si existe déjà
            Files.copy(sourceFile.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le chemin relatif
            return "src/main/resources/UploadsGalerie/" + fileName;
        } catch (Exception e) {
            showError("Erreur lors de la copie de l'image: " + e.getMessage());
            return null;
        }
    }

    private void createArticle() throws SQLException {
        Article newArticle = new Article();
        newArticle.setTitre(titleField.getText());
        newArticle.setDescription(descriptionField.getText());
        newArticle.setPrix(Double.parseDouble(priceField.getText()));
        newArticle.setCategorie(categoryField.getValue());
        newArticle.setNbrArticle(Integer.parseInt(nbrArticleField.getText()));
        
        // Utiliser le galerieId défini
        if (this.galerieId != null) {
            newArticle.setGalerieId(this.galerieId);
        } else if (galerieField.getValue() != null) {
            // Fallback si galerieField est utilisé (ce qui ne devrait pas être le cas ici)
            newArticle.setGalerieId(galerieField.getValue().getId());
        } else {
            throw new SQLException("ID de galerie non défini pour le nouvel article.");
        }

        newArticle.setDatePub(LocalDate.now());
        newArticle.setNbrLikes(0);
        newArticle.setDisponible(true);

        // Copier l'image dans le dossier uploads
        if (selectedImageFile != null) {
            String imagePath = copyImageToUploadFolder(selectedImageFile);
            if (imagePath != null) {
                newArticle.setContenu(imagePath);
            } else {
                throw new SQLException("Erreur lors de la copie de l'image.");
            }
        } else {
            throw new SQLException("Aucune image sélectionnée pour le nouvel article.");
        }

        serviceArticle.insert(newArticle);
        // Le message de succès sera implicite par le retour à la vue principale
        // showError("Article créé avec succès"); 
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    public void setParentController(FrontMonespaceController parentController) {
        this.parentController = parentController;
    }

    public void setGalerieId(Integer galerieId) {
        this.galerieId = galerieId;
        // On pourrait désactiver ou masquer galerieField ici car l'ID est fixé
        galerieField.setDisable(true);
        // Essayer de sélectionner la galerie correspondante si elle est chargée
        if (galerieField.getItems() != null) {
             for (Galerie galerie : galerieField.getItems()) {
                 if (galerie.getId() == galerieId) {
                     galerieField.setValue(galerie);
                     break;
                 }
             }
        }
    }

    public void setArticle(Article article) {
        this.article = article;
        // ... (code pour peupler les champs si nécessaire, mais ce contrôleur est pour l'ajout)
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
         // ... (adapter le bouton si nécessaire, mais ce contrôleur est pour l'ajout)
    }
    
    private StackPane findContentArea() {
        // Cette méthode est probablement obsolète maintenant
        return null;
    }
} 
package Controllers.Commande;

import Models.Commande;
import Services.ServiceCommande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class CommandeController {
    @FXML
    private TableView<Commande> commandesTableView;
    @FXML
    private TableColumn<Commande, Integer> idColumn;
    @FXML
    private TableColumn<Commande, String> numeroColumn;
    @FXML
    private TableColumn<Commande, LocalDateTime> dateCommandeColumn;
    @FXML
    private TableColumn<Commande, Integer> userIdColumn;
    @FXML
    private TableColumn<Commande, Integer> articleIdColumn;
    @FXML
    private TableColumn<Commande, Integer> quantiteColumn;
    @FXML
    private TableColumn<Commande, Double> prixUnitaireColumn;
    @FXML
    private TableColumn<Commande, Double> totalColumn;
    
    @FXML
    private TextField searchField;
    @FXML
    private Button exportExcelBtn;
    @FXML
    private Button deleteBtn;
    
    private ServiceCommande serviceCommande;
    private ObservableList<Commande> commandesList;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat PRIX_FORMAT = new DecimalFormat("#,##0.000 DT");

    @FXML
    public void initialize() {
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidatabase", "root", "");
            serviceCommande = new ServiceCommande(con);
            
            // Configuration des colonnes
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
            dateCommandeColumn.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
            dateCommandeColumn.setCellFactory(column -> new TableCell<Commande, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DATE_FORMATTER));
                    }
                }
            });
            userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
            articleIdColumn.setCellValueFactory(new PropertyValueFactory<>("articleId"));
            quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
            prixUnitaireColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
            prixUnitaireColumn.setCellFactory(column -> new TableCell<Commande, Double>() {
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
            totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
            totalColumn.setCellFactory(column -> new TableCell<Commande, Double>() {
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
            List<Commande> commandes = serviceCommande.findAll();
            commandesList = FXCollections.observableArrayList(commandes);
            
            // Configuration de la recherche
            FilteredList<Commande> filteredData = new FilteredList<>(commandesList, b -> true);
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(commande -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase();
                    return commande.getNumero().toLowerCase().contains(lowerCaseFilter) ||
                           String.valueOf(commande.getUserId()).contains(lowerCaseFilter) ||
                           String.valueOf(commande.getArticleId()).contains(lowerCaseFilter);
                });
            });
            
            SortedList<Commande> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(commandesTableView.comparatorProperty());
            commandesTableView.setItems(sortedData);
            
            // Activer/désactiver le bouton de suppression en fonction de la sélection
            commandesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                deleteBtn.setDisable(newSelection == null);
            });
            if (exportExcelBtn != null) {
                exportExcelBtn.setText("Export Excel");
            }
            if (deleteBtn != null) {
                deleteBtn.setText("Supprimer");
            }
        } catch (Exception e) {
            afficherErreur("Erreur de connexion", "Impossible de se connecter à la base de données", e);
        }
    }

    @FXML
    private void handleSupprimer() {
        Commande selectedCommande = commandesTableView.getSelectionModel().getSelectedItem();
        if (selectedCommande != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer la commande");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ?");
            
            if (confirmation.showAndWait().get() == ButtonType.OK) {
                try {
                    serviceCommande.delete(selectedCommande);
                    commandesList.remove(selectedCommande);
                } catch (Exception e) {
                    afficherErreur("Erreur", "Impossible de supprimer la commande", e);
                }
            }
        }
    }
    
    @FXML
    private void handleExportExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Commandes");

            // Créer l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Numéro", "Date Commande", "User ID", "Article ID", 
                              "Quantité", "Prix Unitaire", "Total"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            int rowNum = 1;
            for (Commande commande : commandesTableView.getItems()) {
                Row row = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
                cell0.setCellValue(commande.getId());
                
                org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
                cell1.setCellValue(commande.getNumero());
                
                org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
                cell2.setCellValue(commande.getDateCommande().format(DATE_FORMATTER));
                
                org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
                cell3.setCellValue(commande.getUserId());
                
                org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
                cell4.setCellValue(commande.getArticleId());
                
                org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
                cell5.setCellValue(commande.getQuantite());
                
                org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
                cell6.setCellValue(commande.getPrixUnitaire());
                
                org.apache.poi.ss.usermodel.Cell cell7 = row.createCell(7);
                cell7.setCellValue(commande.getTotal());
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter vers Excel");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
            );
            
            String defaultFileName = "commandes_" + 
                                  java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                                  ".xlsx";
            fileChooser.setInitialFileName(defaultFileName);

            Stage stage = (Stage) commandesTableView.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }
                workbook.close();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Exportation réussie");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Les commandes ont été exportées avec succès dans le fichier :\n" + file.getAbsolutePath());
                successAlert.showAndWait();
            } else {
                workbook.close();
            }

        } catch (IOException e) {
            afficherErreur("Erreur d'exportation", "Impossible d'exporter les commandes vers Excel", e);
        }
    }
    
    private void afficherErreur(String titre, String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(message);
        if (e != null) {
            alert.setContentText(e.getMessage());
        }
        alert.showAndWait();
    }
} 
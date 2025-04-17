package Controllers.backuser;

import Models.ArtisteResident;
import Models.User;
import Services.ArtisteResidentService;
import Services.UserService;
import Utils.AlertUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ArtisteResidentManagerController implements Initializable {

    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    @FXML
    private TableView<ArtisteResident> requestsTableView;
    
    @FXML
    private TableColumn<ArtisteResident, Integer> idColumn;
    
    @FXML
    private TableColumn<ArtisteResident, String> userColumn;
    
    @FXML
    private TableColumn<ArtisteResident, String> statusColumn;
    
    @FXML
    private TableColumn<ArtisteResident, String> dateColumn;
    
    @FXML
    private VBox detailsPane;
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label userEmailLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextArea projectDescriptionText;
    
    @FXML
    private Button approveButton;
    
    @FXML
    private Button rejectButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button viewPortfolioButton;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button refreshButton;
    
    // Services
    private final ArtisteResidentService artisteResidentService = new ArtisteResidentService();
    private final UserService userService = new UserService();
    
    // Data lists
    private ObservableList<ArtisteResident> allRequests = FXCollections.observableArrayList();
    private FilteredList<ArtisteResident> filteredRequests;
    
    // Date formatter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les filtres
        initializeFilters();
        
        // Configurer la table
        setupTable();
        
        // Charger les données
        loadRequests();
        
        // Configurer les actions sur les boutons
        setupButtonActions();
        
        // Initialiser la recherche
        initializeSearch();
    }
    
    /**
     * Initialise les filtres de statut
     */
    private void initializeFilters() {
        // Préparer les options de filtre
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Tous", "En attente", "Approuvé", "Rejeté"
        );
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.getSelectionModel().selectFirst();
        
        // Configurer l'événement de changement de filtre
        statusFilterComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> applyFilters(newValue)
        );
    }
    
    /**
     * Configure la recherche par nom
     */
    private void initializeSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredRequests != null) {
                filteredRequests.setPredicate(request -> {
                    // Appliquer d'abord le filtre de statut
                    String statusFilter = statusFilterComboBox.getValue();
                    boolean matchesStatus = true;
                    
                    if (statusFilter != null && !statusFilter.equals("Tous")) {
                        String status = request.getStatus();
                        switch (statusFilter) {
                            case "En attente":
                                matchesStatus = status.equals("PENDING");
                                break;
                            case "Approuvé":
                                matchesStatus = status.equals("APPROVED");
                                break;
                            case "Rejeté":
                                matchesStatus = status.equals("REJECTED");
                                break;
                        }
                    }
                    
                    // Si la recherche est vide, on applique seulement le filtre de statut
                    if (newValue == null || newValue.isEmpty()) {
                        return matchesStatus;
                    }
                    
                    // Recherche par nom d'utilisateur
                    User user = request.getUser();
                    if (user != null) {
                        String fullName = user.getNom() + " " + user.getPrenom();
                        boolean matchesSearch = fullName.toLowerCase().contains(newValue.toLowerCase());
                        return matchesStatus && matchesSearch;
                    }
                    
                    return matchesStatus;
                });
            }
        });
    }
    
    /**
     * Configure la table des demandes
     */
    private void setupTable() {
        // Configurer les colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        userColumn.setCellValueFactory(cellData -> {
            ArtisteResident request = cellData.getValue();
            User user = request.getUser();
            return new SimpleStringProperty(user != null ? user.getNom() + " " + user.getPrenom() : "N/A");
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            String statusText;
            switch (status) {
                case "PENDING":
                    statusText = "En attente";
                    break;
                case "APPROVED":
                    statusText = "Approuvé";
                    break;
                case "REJECTED":
                    statusText = "Rejeté";
                    break;
                default:
                    statusText = status;
            }
            return new SimpleStringProperty(statusText);
        });
        
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateDemande() != null) {
                return new SimpleStringProperty(dateFormat.format(cellData.getValue().getDateDemande()));
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Configurer la sélection de ligne pour afficher les détails
        requestsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDetails(newValue)
        );
    }
    
    /**
     * Charge toutes les demandes d'artiste résident
     */
    private void loadRequests() {
        try {
            List<ArtisteResident> requests = artisteResidentService.findAll();
            allRequests.clear();
            allRequests.addAll(requests);
            
            // Initialiser la liste filtrée
            filteredRequests = new FilteredList<>(allRequests, p -> true);
            requestsTableView.setItems(filteredRequests);
            
            // Si la table contient des données, sélectionner la première ligne
            if (!requests.isEmpty()) {
                requestsTableView.getSelectionModel().selectFirst();
            } else {
                // Effacer les détails si aucune donnée n'est disponible
                clearDetails();
            }
            
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les demandes", e.getMessage());
        }
    }
    
    /**
     * Applique les filtres à la liste des demandes
     * @param statusFilter le statut sélectionné pour filtrer
     */
    private void applyFilters(String statusFilter) {
        filteredRequests.setPredicate(request -> {
            if (statusFilter == null || statusFilter.equals("Tous")) {
                return true;
            }
            
            String status = request.getStatus();
            switch (statusFilter) {
                case "En attente":
                    return status.equals("PENDING");
                case "Approuvé":
                    return status.equals("APPROVED");
                case "Rejeté":
                    return status.equals("REJECTED");
                default:
                    return true;
            }
        });
        
        // Si la liste filtrée contient des éléments, sélectionner le premier
        if (!filteredRequests.isEmpty()) {
            requestsTableView.getSelectionModel().selectFirst();
        } else {
            clearDetails();
        }
    }
    
    /**
     * Affiche les détails d'une demande sélectionnée
     * @param request la demande sélectionnée
     */
    private void showDetails(ArtisteResident request) {
        if (request == null) {
            clearDetails();
            return;
        }
        
        // Rendre le panneau de détails visible
        detailsPane.setVisible(true);
        
        // Afficher les informations de l'utilisateur
        User user = request.getUser();
        if (user != null) {
            userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            userEmailLabel.setText(user.getEmail());
        } else {
            userNameLabel.setText("N/A");
            userEmailLabel.setText("N/A");
        }
        
        // Afficher le statut avec le style approprié
        statusLabel.setText(getStatusText(request.getStatus()));
        updateStatusLabelStyle(request.getStatus());
        
        // Afficher la description du projet
        projectDescriptionText.setText(request.getProjet() != null ? request.getProjet() : "Aucune description disponible");
        
        // Gérer l'état des boutons selon le statut
        boolean isPending = "PENDING".equals(request.getStatus());
        
        // Activer les boutons au lieu de simplement les rendre visibles
        approveButton.setDisable(!isPending);
        rejectButton.setDisable(!isPending);
        
        // Le bouton de suppression est toujours disponible
        deleteButton.setDisable(false);
        
        // Configurer le bouton de portfolio s'il y a une URL
        String portfolioUrl = request.getPortfolio();
        boolean hasPortfolio = portfolioUrl != null && !portfolioUrl.isEmpty();
        viewPortfolioButton.setDisable(!hasPortfolio);
        
        // Rendre tous les boutons visibles
        approveButton.setVisible(true);
        rejectButton.setVisible(true);
        deleteButton.setVisible(true);
        viewPortfolioButton.setVisible(true);
    }
    
    /**
     * Efface les détails quand aucune demande n'est sélectionnée
     */
    private void clearDetails() {
        detailsPane.setVisible(false);
        userNameLabel.setText("");
        userEmailLabel.setText("");
        statusLabel.setText("");
        projectDescriptionText.setText("");
        
        // Désactiver et cacher les boutons d'action
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        deleteButton.setDisable(true);
        viewPortfolioButton.setDisable(true);
        
        approveButton.setVisible(false);
        rejectButton.setVisible(false);
        deleteButton.setVisible(false);
        viewPortfolioButton.setVisible(false);
    }
    
    /**
     * Configure les actions des boutons
     */
    private void setupButtonActions() {
        // Action du bouton de rafraîchissement
        refreshButton.setOnAction(event -> loadRequests());
        
        // Action du bouton d'approbation
        approveButton.setOnAction(event -> {
            ArtisteResident selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                approveRequest(selectedRequest);
            }
        });
        
        // Action du bouton de rejet
        rejectButton.setOnAction(event -> {
            ArtisteResident selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                rejectRequest(selectedRequest);
            }
        });
        
        // Action du bouton de suppression
        deleteButton.setOnAction(event -> {
            ArtisteResident selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                deleteRequest(selectedRequest);
            }
        });
        
        // Action du bouton de visualisation du portfolio
        viewPortfolioButton.setOnAction(event -> {
            ArtisteResident selectedRequest = requestsTableView.getSelectionModel().getSelectedItem();
            if (selectedRequest != null && selectedRequest.getPortfolio() != null) {
                openPortfolio(selectedRequest.getPortfolio());
            }
        });
    }
    
    /**
     * Approuve une demande d'artiste résident
     * @param request la demande à approuver
     */
    private void approveRequest(ArtisteResident request) {
        // Demander confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Approuver la demande");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir approuver cette demande d'artiste résident ?\n" +
                "L'utilisateur obtiendra le statut d'administrateur dans le système.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Mettre à jour le statut de la demande
                    request.setStatus("APPROVED");
                    artisteResidentService.update(request);
                    
                    // Mettre à jour le rôle de l'utilisateur
                    User user = request.getUser();
                    if (user != null) {
                        // Utiliser le format JSON approprié pour les rôles
                        // Attribuer le rôle ADMIN à l'utilisateur
                        String newRoles = "[\"ROLE_USER\",\"ROLE_ADMIN\"]";
                        
                        // Mettre à jour l'utilisateur avec les nouveaux rôles
                        user.setRoles(newRoles);
                        userService.update(user);
                    }
                    
                    // Recharger les données
                    loadRequests();
                    
                    // Afficher une confirmation
                    AlertUtils.showInfo("Succès", "Demande approuvée", 
                            "La demande a été approuvée avec succès et l'utilisateur est maintenant un administrateur.");
                    
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur", "Impossible d'approuver la demande", e.getMessage());
                }
            }
        });
    }
    
    /**
     * Rejette une demande d'artiste résident
     * @param request la demande à rejeter
     */
    private void rejectRequest(ArtisteResident request) {
        // Demander confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Rejeter la demande");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir rejeter cette demande d'artiste résident ?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Mettre à jour le statut de la demande
                    request.setStatus("REJECTED");
                    artisteResidentService.update(request);
                    
                    // Recharger les données
                    loadRequests();
                    
                    // Afficher une confirmation
                    AlertUtils.showInfo("Succès", "Demande rejetée", 
                            "La demande a été rejetée avec succès.");
                    
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur", "Impossible de rejeter la demande", e.getMessage());
                }
            }
        });
    }
    
    /**
     * Supprime une demande d'artiste résident
     * @param request la demande à supprimer
     */
    private void deleteRequest(ArtisteResident request) {
        // Demander confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer la demande");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer définitivement cette demande d'artiste résident ?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Supprimer la demande
                    artisteResidentService.delete(request.getId());
                    
                    // Recharger les données
                    loadRequests();
                    
                    // Afficher une confirmation
                    AlertUtils.showInfo("Succès", "Demande supprimée", 
                            "La demande a été supprimée avec succès.");
                    
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur", "Impossible de supprimer la demande", e.getMessage());
                }
            }
        });
    }
    
    /**
     * Ouvre le portfolio de l'artiste dans le navigateur par défaut
     * @param url l'URL du portfolio
     */
    private void openPortfolio(String url) {
        if (url == null || url.isEmpty()) {
            AlertUtils.showWarning("Portfolio", "URL non disponible", 
                    "L'URL du portfolio n'est pas disponible pour cet artiste.");
            return;
        }
        
        try {
            // Vérifier si l'URL est valide
            URI uri = new URI(url);
            
            // Vérifier si le bureau est pris en charge
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                } else {
                    AlertUtils.showWarning("Portfolio", "Navigation non supportée", 
                            "Votre système ne prend pas en charge l'ouverture de navigateurs.");
                }
            } else {
                AlertUtils.showWarning("Portfolio", "Desktop non supporté", 
                        "Votre système ne prend pas en charge l'ouverture de navigateurs.");
            }
        } catch (URISyntaxException | IOException e) {
            AlertUtils.showError("Erreur", "Impossible d'ouvrir l'URL", e.getMessage());
        }
    }
    
    /**
     * Retourne le texte formaté du statut
     * @param status le code de statut
     * @return le texte formaté
     */
    private String getStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "En attente";
            case "APPROVED":
                return "Approuvé";
            case "REJECTED":
                return "Rejeté";
            default:
                return status;
        }
    }
    
    /**
     * Met à jour le style du label de statut en fonction du statut
     * @param status le code de statut
     */
    private void updateStatusLabelStyle(String status) {
        // Supprimer toutes les classes de style précédentes
        statusLabel.getStyleClass().removeAll("status-pending", "status-approved", "status-rejected");
        
        // Ajouter la classe appropriée
        switch (status) {
            case "PENDING":
                statusLabel.getStyleClass().add("status-pending");
                break;
            case "APPROVED":
                statusLabel.getStyleClass().add("status-approved");
                break;
            case "REJECTED":
                statusLabel.getStyleClass().add("status-rejected");
                break;
        }
    }
    
    /**
     * Rafraîchit la liste des demandes
     */
    @FXML
    private void refreshData() {
        loadRequests();
    }
} 
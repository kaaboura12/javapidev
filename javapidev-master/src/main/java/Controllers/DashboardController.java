package Controllers;

import Models.ArtisteResident;
import Models.Event;
import Models.User;
import Models.Donation;
import Models.Reservation;
import Services.ArtisteResidentService;
import Services.EventService;
import Services.UserService;
import Services.DonationService;
import Services.ReservationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalEventsLabel;
    
    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label totalDonationsLabel;
    
    @FXML
    private Label totalReservationsLabel;
    
    @FXML
    private Label pendingArtisteRequestsLabel;
    
    @FXML
    private BarChart<String, Number> eventsBarChart;
    
    @FXML
    private PieChart donationPieChart;
    
    @FXML
    private BorderPane dashboardContent;
    
    @FXML
    private Button userManagementButton;
    
    @FXML
    private Button artisteResidentManagerButton;
    
    @FXML
    private TabPane userManagementTabs;
    
    @FXML
    private Tab usersTab;
    
    @FXML
    private Tab artisteResidentsTab;
    
    @FXML
    private StackPane userContentArea;
    
    @FXML
    private StackPane artisteResidentContentArea;
    
    // Services
    private final EventService eventService = new EventService();
    private final UserService userService = new UserService();
    private final DonationService donationService = new DonationService();
    private final ReservationService reservationService = new ReservationService();
    private final ArtisteResidentService artisteResidentService = new ArtisteResidentService();
    
    // Formatter for displaying monetary values
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSummaryData();
        populateEventsBarChart();
        populateDonationsPieChart();
        
        // Configure buttons
        setupUserManagementButtons();
    }
    
    /**
     * Configure les boutons de gestion des utilisateurs et des artistes résidents
     */
    private void setupUserManagementButtons() {
        // Configurer le bouton de gestion des utilisateurs
        if (userManagementButton != null) {
            userManagementButton.setOnAction(event -> {
                showUserManagementPanel();
            });
        }
        
        // Configurer le bouton de gestion des artistes résidents
        if (artisteResidentManagerButton != null) {
            artisteResidentManagerButton.setOnAction(event -> {
                showArtisteResidentManager();
            });
        }
        
        // Configurer les onglets si disponibles
        if (userManagementTabs != null) {
            userManagementTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == usersTab) {
                    loadUserManagement();
                } else if (newValue == artisteResidentsTab) {
                    loadArtisteResidentManager();
                }
            });
        }
    }
    
    /**
     * Charge les données sommaires pour les cartes du tableau de bord
     */
    private void loadSummaryData() {
        try {
            // Get counts from services
            List<Event> events = eventService.findAll();
            List<User> users = userService.findAll();
            List<Donation> donations = donationService.findAll();
            List<Reservation> reservations = reservationService.findAll();
            List<ArtisteResident> pendingRequests = artisteResidentService.findByStatus("PENDING");
            
            // Calculate totals
            int eventCount = events.size();
            int userCount = users.size();
            double totalDonationAmount = donations.stream()
                    .mapToDouble(Donation::getMontant)
                    .sum();
            int reservationCount = reservations.size();
            int pendingRequestsCount = pendingRequests.size();
            
            // Update UI
            totalEventsLabel.setText(String.valueOf(eventCount));
            totalUsersLabel.setText(String.valueOf(userCount));
            totalDonationsLabel.setText(currencyFormat.format(totalDonationAmount));
            totalReservationsLabel.setText(String.valueOf(reservationCount));
            
            // Mettre à jour le nombre de demandes d'artistes en attente si le label existe
            if (pendingArtisteRequestsLabel != null) {
                pendingArtisteRequestsLabel.setText(String.valueOf(pendingRequestsCount));
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populate events bar chart with attendance data
     */
    private void populateEventsBarChart() {
        try {
            // Prepare chart data
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Attendance");
            
            // Get events and their reservations
            List<Event> events = eventService.findAll();
            
            // Limited to 5 events for clarity
            int count = 0;
            for (Event event : events) {
                if (count >= 5) break;
                
                int attendance = reservationService.getTotalReservedSeatsForEvent(event.getIdevent());
                series.getData().add(new XYChart.Data<>(event.getTitre(), attendance));
                count++;
            }
            
            // Add series to chart
            eventsBarChart.getData().add(series);
            
        } catch (SQLException e) {
            System.err.println("Error loading event chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populate donations pie chart with data
     */
    private void populateDonationsPieChart() {
        try {
            // Get donations data grouped by event
            List<Donation> donations = donationService.findAll();
            Map<String, Double> donationsByEvent = new HashMap<>();
            
            // Calculate total donations by event
            for (Donation donation : donations) {
                String eventName = donation.getEvent().getTitre();
                donationsByEvent.put(
                    eventName, 
                    donationsByEvent.getOrDefault(eventName, 0.0) + donation.getMontant()
                );
            }
            
            // Create pie chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Add data to pie chart
            donationsByEvent.forEach((eventName, amount) -> 
                pieChartData.add(new PieChart.Data(eventName + " (" + currencyFormat.format(amount) + ")", amount))
            );
            
            // Set data to pie chart
            donationPieChart.setData(pieChartData);
            
        } catch (SQLException e) {
            System.err.println("Error loading donation chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche le panneau de gestion des utilisateurs
     */
    private void showUserManagementPanel() {
        if (dashboardContent != null) {
            try {
                // Charger le panneau de gestion des utilisateurs avec onglets
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backoffice/user_management_tabs.fxml"));
                BorderPane userManagementPane = loader.load();
                
                // Remplacer le contenu du tableau de bord
                dashboardContent.setCenter(userManagementPane);
                
                // Si les onglets sont disponibles, sélectionner l'onglet des utilisateurs par défaut
                if (userManagementTabs != null) {
                    userManagementTabs.getSelectionModel().select(usersTab);
                    loadUserManagement();
                }
                
            } catch (IOException e) {
                System.err.println("Error loading user management panel: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Charge le gestionnaire d'utilisateurs standard
     */
    private void loadUserManagement() {
        if (userContentArea != null) {
            try {
                // Charger la vue de gestion des utilisateurs
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backoffice/user_management.fxml"));
                BorderPane userManagementContent = loader.load();
                
                // Effacer et ajouter le contenu
                userContentArea.getChildren().clear();
                userContentArea.getChildren().add(userManagementContent);
                
            } catch (IOException e) {
                System.err.println("Error loading user management content: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Affiche directement le gestionnaire d'artistes résidents
     */
    private void showArtisteResidentManager() {
        if (dashboardContent != null) {
            try {
                // Charger directement la vue de gestion des artistes résidents
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backoffice/artiste_resident_manager.fxml"));
                BorderPane artisteResidentManagerPane = loader.load();
                
                // Remplacer le contenu du tableau de bord
                dashboardContent.setCenter(artisteResidentManagerPane);
                
            } catch (IOException e) {
                System.err.println("Error loading artiste resident manager: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Charge le gestionnaire d'artistes résidents dans l'onglet approprié
     */
    private void loadArtisteResidentManager() {
        if (artisteResidentContentArea != null) {
            try {
                // Charger la vue de gestion des artistes résidents
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/backoffice/artiste_resident_manager.fxml"));
                BorderPane artisteResidentManagerContent = loader.load();
                
                // Effacer et ajouter le contenu
                artisteResidentContentArea.getChildren().clear();
                artisteResidentContentArea.getChildren().add(artisteResidentManagerContent);
                
            } catch (IOException e) {
                System.err.println("Error loading artiste resident manager content: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 
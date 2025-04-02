package Controllers;

import Models.Event;
import Models.User;
import Models.Donation;
import Models.Reservation;
import Services.EventService;
import Services.UserService;
import Services.DonationService;
import Services.ReservationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

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
    private BarChart<String, Number> eventsBarChart;
    
    @FXML
    private PieChart donationPieChart;
    
    // Services
    private final EventService eventService = new EventService();
    private final UserService userService = new UserService();
    private final DonationService donationService = new DonationService();
    private final ReservationService reservationService = new ReservationService();
    
    // Formatter for displaying monetary values
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSummaryData();
        populateEventsBarChart();
        populateDonationsPieChart();
    }
    
    /**
     * Load summary data for dashboard cards
     */
    private void loadSummaryData() {
        try {
            // Get counts from services
            List<Event> events = eventService.findAll();
            List<User> users = userService.findAll();
            List<Donation> donations = donationService.findAll();
            List<Reservation> reservations = reservationService.findAll();
            
            // Calculate totals
            int eventCount = events.size();
            int userCount = users.size();
            double totalDonationAmount = donations.stream()
                    .mapToDouble(Donation::getMontant)
                    .sum();
            int reservationCount = reservations.size();
            
            // Update UI
            totalEventsLabel.setText(String.valueOf(eventCount));
            totalUsersLabel.setText(String.valueOf(userCount));
            totalDonationsLabel.setText(currencyFormat.format(totalDonationAmount));
            totalReservationsLabel.setText(String.valueOf(reservationCount));
            
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
} 
package Tests;

import Models.User;
import Models.Event;
import Models.Donation;
import Models.Reservation;
import Services.UserService;
import Services.EventService;
import Services.DonationService;
import Services.ReservationService;
import Utils.MyDb;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        MyDb db = MyDb.getInstance();
        
        // Initialize all services
        UserService userService = new UserService();
        EventService eventService = new EventService();
        DonationService donationService = new DonationService();
        ReservationService reservationService = new ReservationService();
        
        try {
            // 1. Test User functionality
            System.out.println("===== TESTING USER SERVICE =====");
            // Get user list
            System.out.println("All Users: " + userService.findAll());
            
            // 2. Test Event functionality
            System.out.println("\n===== TESTING EVENT SERVICE =====");
            // Create event
            Event event = new Event(
                "Charity Gala", 
                "Annual fundraiser", 
                "Grand Hall", 
                100, 
                "event.jpg", 
                LocalTime.of(19, 0), 
                "Fundraising for children", 
                5000.0, 
                25.0, 
                LocalDateTime.now().plusDays(30)
            );
            eventService.insert(event);
            System.out.println("Event created: " + event.getTitre());
            
            // Get event list
            for (Event e : eventService.findAll()) {
                System.out.println("Event: " + e.getTitre() + " (ID: " + e.getIdevent() + ")");
            }
            
            // Get the first event for testing relationships
            Event firstEvent = null;
            if (!eventService.findAll().isEmpty()) {
                firstEvent = eventService.findAll().get(0);
            }
            
            // Get the first user for testing relationships
            User firstUser = null;
            if (!userService.findAll().isEmpty()) {
                firstUser = userService.findAll().get(0);
            }
            
            // 3. Test Donation functionality if we have event and user
            if (firstEvent != null && firstUser != null) {
                System.out.println("\n===== TESTING DONATION SERVICE =====");
                
                Donation donation = new Donation(
                    firstEvent,
                    firstUser,
                    firstUser.getPrenom() + " " + firstUser.getNom(),
                    50.0,
                    LocalDate.now(),
                    "Credit Card",
                    firstUser.getNumtlf()
                );
                
                donationService.insert(donation);
                System.out.println("Donation created for event: " + firstEvent.getTitre());
                
                // Show all donations
                for (Donation d : donationService.findAll()) {
                    System.out.println("Donation: $" + d.getMontant() + " by " + d.getDonorname());
                }
                
                // 4. Test Reservation functionality
                System.out.println("\n===== TESTING RESERVATION SERVICE =====");
                
                Reservation reservation = new Reservation(
                    firstUser,
                    firstEvent,
                    LocalDate.now(),
                    2,
                    2 * firstEvent.getSeatprice()
                );
                
                reservationService.insert(reservation);
                System.out.println("Reservation created: " + 
                    reservation.getSeats_reserved() + " seats for " + firstEvent.getTitre());
                
                // Show all reservations
                for (Reservation r : reservationService.findAll()) {
                    System.out.println("Reservation: " + r.getSeats_reserved() + 
                        " seats, total: $" + r.getTotal_amount());
                }
                
                // Test the relationship functions
                int totalSeats = reservationService.getTotalReservedSeatsForEvent(firstEvent.getIdevent());
                System.out.println("Total seats reserved for " + firstEvent.getTitre() + 
                    ": " + totalSeats);
            }
            
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

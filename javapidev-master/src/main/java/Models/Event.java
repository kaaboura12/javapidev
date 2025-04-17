package Models;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int idevent;
    private String titre;
    private String description;
    private String lieu;
    private int nombreBillets;
    private String image;
    private LocalTime timestart;
    private String event_mission;
    private double donation_objective;
    private double seatprice;
    private LocalDateTime dateEvenement;
    private List<Donation> donations = new ArrayList<>(); // Collection of donations for this event
    private List<Reservation> reservations = new ArrayList<>(); // Collection of reservations for this event

    public Event() {
    }

    public Event(String titre, String description, String lieu, int nombreBillets, 
                String image, LocalTime timestart, String event_mission, 
                double donation_objective, double seatprice, LocalDateTime dateEvenement) {
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.nombreBillets = nombreBillets;
        this.image = image;
        this.timestart = timestart;
        this.event_mission = event_mission;
        this.donation_objective = donation_objective;
        this.seatprice = seatprice;
        this.dateEvenement = dateEvenement;
    }

    public Event(int idevent, String titre, String description, String lieu, 
                int nombreBillets, String image, LocalTime timestart, 
                String event_mission, double donation_objective, double seatprice, 
                LocalDateTime dateEvenement) {
        this.idevent = idevent;
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.nombreBillets = nombreBillets;
        this.image = image;
        this.timestart = timestart;
        this.event_mission = event_mission;
        this.donation_objective = donation_objective;
        this.seatprice = seatprice;
        this.dateEvenement = dateEvenement;
    }

    public int getIdevent() {
        return idevent;
    }

    public void setIdevent(int idevent) {
        this.idevent = idevent;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public int getNombreBillets() {
        return nombreBillets;
    }

    public void setNombreBillets(int nombreBillets) {
        this.nombreBillets = nombreBillets;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalTime getTimestart() {
        return timestart;
    }

    public void setTimestart(LocalTime timestart) {
        this.timestart = timestart;
    }

    public String getEvent_mission() {
        return event_mission;
    }

    public void setEvent_mission(String event_mission) {
        this.event_mission = event_mission;
    }

    public double getDonation_objective() {
        return donation_objective;
    }

    public void setDonation_objective(double donation_objective) {
        this.donation_objective = donation_objective;
    }

    public double getSeatprice() {
        return seatprice;
    }

    public void setSeatprice(double seatprice) {
        this.seatprice = seatprice;
    }

    public LocalDateTime getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(LocalDateTime dateEvenement) {
        this.dateEvenement = dateEvenement;
    }
    
    // Getter for the donations collection
    public List<Donation> getDonations() {
        return donations;
    }
    
    // Methods to manage the relationship
    public void addDonation(Donation donation) {
        donations.add(donation);
        donation.setEvent(this);
    }
    
    public void removeDonation(Donation donation) {
        donations.remove(donation);
        if (donation.getEvent() == this) {
            donation.setEvent(null);
        }
    }
    
    // Calculate total donations amount
    public double getTotalDonations() {
        double total = 0;
        for (Donation donation : donations) {
            total += donation.getMontant();
        }
        return total;
    }

    // Getter for the reservations collection
    public List<Reservation> getReservations() {
        return reservations;
    }

    // Methods to manage the relationship with Reservation
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setEvent(this);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        if (reservation.getEvent() == this) {
            reservation.setEvent(null);
        }
    }

    // Calculate total reserved seats
    public int getTotalReservedSeats() {
        int total = 0;
        for (Reservation reservation : reservations) {
            total += reservation.getSeats_reserved();
        }
        return total;
    }

    // Calculate available seats
    public int getAvailableSeats() {
        return nombreBillets - getTotalReservedSeats();
    }

    @Override
    public String toString() {
        return "Event{" +
                "idevent=" + idevent +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", lieu='" + lieu + '\'' +
                ", nombreBillets=" + nombreBillets +
                ", image='" + image + '\'' +
                ", timestart=" + timestart +
                ", event_mission='" + event_mission + '\'' +
                ", donation_objective=" + donation_objective +
                ", seatprice=" + seatprice +
                ", dateEvenement=" + dateEvenement +
                '}';
    }
} 
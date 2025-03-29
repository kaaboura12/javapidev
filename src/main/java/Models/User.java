package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int user_id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String numtlf;
    private Integer age;
    private String avatar_url;
    private String roles;
    private LocalDateTime reset_token_expires_at;
    private boolean is_verified;
    private String reset_token;
    private LocalDateTime last_login_at;
    private List<Donation> donations = new ArrayList<>(); // Collection of donations made by this user
    private List<Reservation> reservations = new ArrayList<>(); // Collection of reservations made by this user

    public User() {
    }

    public User(String nom, String prenom, String email, String password, String numtlf, 
              Integer age, String avatar_url, String roles, boolean is_verified) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.numtlf = numtlf;
        this.age = age;
        this.avatar_url = avatar_url;
        this.roles = roles;
        this.is_verified = is_verified;
    }

    public User(int user_id, String nom, String prenom, String email, String password, 
              String numtlf, Integer age, String avatar_url, String roles, 
              LocalDateTime reset_token_expires_at, boolean is_verified, 
              String reset_token, LocalDateTime last_login_at) {
        this.user_id = user_id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.numtlf = numtlf;
        this.age = age;
        this.avatar_url = avatar_url;
        this.roles = roles;
        this.reset_token_expires_at = reset_token_expires_at;
        this.is_verified = is_verified;
        this.reset_token = reset_token;
        this.last_login_at = last_login_at;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumtlf() {
        return numtlf;
    }

    public void setNumtlf(String numtlf) {
        this.numtlf = numtlf;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public LocalDateTime getReset_token_expires_at() {
        return reset_token_expires_at;
    }

    public void setReset_token_expires_at(LocalDateTime reset_token_expires_at) {
        this.reset_token_expires_at = reset_token_expires_at;
    }

    public boolean isIs_verified() {
        return is_verified;
    }

    public void setIs_verified(boolean is_verified) {
        this.is_verified = is_verified;
    }

    public String getReset_token() {
        return reset_token;
    }

    public void setReset_token(String reset_token) {
        this.reset_token = reset_token;
    }

    public LocalDateTime getLast_login_at() {
        return last_login_at;
    }

    public void setLast_login_at(LocalDateTime last_login_at) {
        this.last_login_at = last_login_at;
    }
    
    // Getter for the donations collection
    public List<Donation> getDonations() {
        return donations;
    }
    
    // Methods to manage the relationship with Donation
    public void addDonation(Donation donation) {
        donations.add(donation);
        donation.setUser(this);
    }
    
    public void removeDonation(Donation donation) {
        donations.remove(donation);
        if (donation.getUser() == this) {
            donation.setUser(null);
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
        reservation.setUser(this);
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        if (reservation.getUser() == this) {
            reservation.setUser(null);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", numtlf='" + numtlf + '\'' +
                ", age=" + age +
                ", avatar_url='" + avatar_url + '\'' +
                ", roles='" + roles + '\'' +
                ", reset_token_expires_at=" + reset_token_expires_at +
                ", is_verified=" + is_verified +
                ", reset_token='" + reset_token + '\'' +
                ", last_login_at=" + last_login_at +
                '}';
    }
}

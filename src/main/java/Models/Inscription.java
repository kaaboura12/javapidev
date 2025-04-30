package Models;

import java.time.LocalDate;

public class Inscription {
    private int inscription_id;
    private int formation_id;
    private int user_id;
    private LocalDate date_inscription;
    private String statut;
    private LocalDate date_creation;
    private Formation formation;
    private User user;

    public Inscription() {
    }

    public Inscription(int formation_id, int user_id, LocalDate date_inscription, String statut, LocalDate date_creation) {
        this.formation_id = formation_id;
        this.user_id = user_id;
        this.date_inscription = date_inscription;
        this.statut = statut;
        this.date_creation = date_creation;
    }

    // Getters
    public int getInscription_id() {
        return inscription_id;
    }

    public int getFormation_id() {
        return formation_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public LocalDate getDate_inscription() {
        return date_inscription;
    }

    public String getStatut() {
        return statut;
    }

    public LocalDate getDate_creation() {
        return date_creation;
    }

    public Formation getFormation() {
        return formation;
    }

    public User getUser() {
        return user;
    }

    // Setters
    public void setInscription_id(int inscription_id) {
        this.inscription_id = inscription_id;
    }

    public void setFormation_id(int formation_id) {
        this.formation_id = formation_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setDate_inscription(LocalDate date_inscription) {
        this.date_inscription = date_inscription;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setDate_creation(LocalDate date_creation) {
        this.date_creation = date_creation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        if (formation != null) {
            this.formation_id = formation.getId();
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.user_id = user.getUser_id();
        }
    }
}

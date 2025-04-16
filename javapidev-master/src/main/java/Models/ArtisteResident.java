package Models;

import java.sql.Date;

public class ArtisteResident {
    private int id;
    private int userId;
    private String description;
    private String portfolioPath;
    private Date dateCreated;
    private String status; // 'PENDING', 'APPROVED', 'REJECTED'
    private User user; // Pour stocker les détails de l'utilisateur
    
    // Constructor with parameters
    public ArtisteResident(int userId, String description, String portfolioPath) {
        this.userId = userId;
        this.description = description;
        this.portfolioPath = portfolioPath;
        this.status = "PENDING";
    }
    
    // Default constructor
    public ArtisteResident() {
        this.status = "PENDING";
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPortfolioPath() {
        return portfolioPath;
    }
    
    public void setPortfolioPath(String portfolioPath) {
        this.portfolioPath = portfolioPath;
    }
    
    public Date getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // Alternative methods to match what's used in the controller
    
    /**
     * Alternative name for getDateCreated for controller compatibility
     * @return The date the artist residence request was created
     */
    public Date getDateDemande() {
        return dateCreated;
    }
    
    /**
     * Alternative name for getDescription for controller compatibility
     * @return The project description
     */
    public String getProjet() {
        return description;
    }
    
    /**
     * Alternative name for getPortfolioPath for controller compatibility
     * @return The portfolio URL
     */
    public String getPortfolio() {
        return portfolioPath;
    }
    
    public String getFullName() {
        if (user != null) {
            return user.getPrenom() + " " + user.getNom();
        }
        return "Utilisateur inconnu";
    }
    
    @Override
    public String toString() {
        return "ArtisteResident{" +
                "id=" + id +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                ", portfolioPath='" + portfolioPath + '\'' +
                ", dateCreated=" + dateCreated +
                ", status='" + status + '\'' +
                ", user=" + (user != null ? user.getPrenom() + " " + user.getNom() : "null") +
                '}';
    }
} 
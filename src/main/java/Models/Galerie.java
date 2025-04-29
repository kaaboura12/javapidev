package Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Galerie {
    private int id;
    private Integer userId;
    private String nom;
    private LocalDateTime dateCreation;
    private String description;
    private List<Article> articles = new ArrayList<>();

    public Galerie() {
    }
    public Galerie(int id, Integer userId, String nom, LocalDateTime dateCreation, String description) {
        this.id = id;
        this.userId = userId;
        this.nom = nom;
        this.dateCreation = dateCreation;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //afficher les articles
    public List<Article> getArticles() { return articles; }

    // Ajouter un article
    public void ajouterArticle(Article article) {
        if (article != null) {
            articles.add(article);
        }
    }

    // Supprimer un article
    public boolean supprimerArticle(Article article) {
        return articles.remove(article); // retourne true si supprimé
    }

    // Compter le nombre total d'articles
    public int getNombreTotalArticles() {
        return articles.size();
    }

    @Override
    public String toString() {
        return "Galerie{" +
                "id=" + id +
                ", userId=" + userId +
                ", nom='" + nom + '\'' +
                ", dateCreation=" + dateCreation +
                ", description='" + description + '\'' +
                '}';
    }
}

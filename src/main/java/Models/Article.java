package Models;

import java.time.LocalDate;

public class Article {
    private int id;
    private Integer galerieId;
    private String titre;
    private String description;
    private double prix;
    private LocalDate datePub;
    private boolean disponible;
    private int nbrArticle;
    private int nbrLikes;
    private String contenu;
    private String categorie;

    public Article() {}

    public Article(int id, Integer galerieId, String titre, String description, double prix,
                   LocalDate datePub, boolean disponible, int nbrArticle, int nbrLikes,
                   String contenu, String categorie) {
        this.id = id;
        this.galerieId = galerieId;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.datePub = datePub;
        this.disponible = disponible;
        this.nbrArticle = nbrArticle;
        this.nbrLikes = nbrLikes;
        this.contenu = contenu;
        this.categorie = categorie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getGalerieId() {
        return galerieId;
    }

    public void setGalerieId(Integer galerieId) {
        this.galerieId = galerieId;
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

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public LocalDate getDatePub() {
        return datePub;
    }

    public void setDatePub(LocalDate datePub) {
        this.datePub = datePub;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getNbrArticle() {
        return nbrArticle;
    }

    public void setNbrArticle(int nbrArticle) {
        this.nbrArticle = nbrArticle;
    }

    public int getNbrLikes() {
        return nbrLikes;
    }

    public void setNbrLikes(int nbrLikes) {
        this.nbrLikes = nbrLikes;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", galerieId=" + galerieId +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", datePub=" + datePub +
                ", disponible=" + disponible +
                ", nbrArticle=" + nbrArticle +
                ", nbrLikes=" + nbrLikes +
                ", contenu='" + contenu + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}

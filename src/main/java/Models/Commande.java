package Models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Commande {
    private int id;
    private int user_id;
    private int article_id;
    private String numero; // Numéro de commande unique
    private LocalDateTime date_commande;
    private int quantite;
    private double prix_unitaire;
    private double total;

    // Constructeurs
    public Commande() {
    }

    public Commande(int id, int user_id, int article_id, String numero, LocalDateTime date_commande, int quantite, double prix_unitaire, double total) {
        this.id = id;
        this.user_id = user_id;
        this.article_id = article_id;
        this.numero = numero;
        this.date_commande = date_commande;
        this.quantite = quantite;
        this.prix_unitaire = prix_unitaire;
        this.total = total;
    }

    // Constructeur pratique (sans id, date auto)
    public Commande(int user_id, int article_id, String numero, int quantite, double prix_unitaire) {
        this.user_id = user_id;
        this.article_id = article_id;
        this.numero = numero;
        this.date_commande = LocalDateTime.now();
        this.quantite = quantite;
        this.prix_unitaire = prix_unitaire;
        this.total = quantite * prix_unitaire;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return user_id; }
    public int getArticleId() { return article_id; }
    public String getNumero() { return numero; }
    public LocalDateTime getDateCommande() { return date_commande; }
    public int getQuantite() { return quantite; }
    public double getPrixUnitaire() { return prix_unitaire; }
    public double getTotal() { return total; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
    public void setArticleId(int article_id) { this.article_id = article_id; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setDateCommande(LocalDateTime date_commande) { this.date_commande = date_commande; }
    public void setQuantite(int quantite) {
        this.quantite = quantite;
        recalculateTotal(); // Recalculer si la quantité change
    }
    public void setPrixUnitaire(double prix_unitaire) {
        this.prix_unitaire = prix_unitaire;
        recalculateTotal(); // Recalculer si le prix change
    }
    public void setTotal(double total) { this.total = total; } // Principalement pour la lecture DB

    // Méthode utilitaire
    private void recalculateTotal() {
        this.total = this.quantite * this.prix_unitaire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commande commande = (Commande) o;
        return id == commande.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Commande{" +
               "id=" + id +
               ", user_id=" + user_id +
               ", article_id=" + article_id +
               ", numero='" + numero + '\'' +
               ", date_commande=" + date_commande +
               ", quantite=" + quantite +
               ", prix_unitaire=" + prix_unitaire +
               ", total=" + total +
               '}';
    }
}

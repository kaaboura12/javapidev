package Services;

import Interfaces.ICrud;
import Models.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceArticle implements ICrud<Article> {

    private Connection connection;

    public ServiceArticle(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Article a) throws SQLException {
        String sql = "INSERT INTO article (galerie_id, titre, description, prix, date_pub, disponible, nbrarticle, nbrlikes, contenu, categorie) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, a.getGalerieId());
            ps.setString(2, a.getTitre());
            ps.setString(3, a.getDescription());
            ps.setDouble(4, a.getPrix());
            ps.setDate(5, Date.valueOf(a.getDatePub()));
            ps.setBoolean(6, a.isDisponible());
            ps.setInt(7, a.getNbrArticle());
            ps.setInt(8, a.getNbrLikes());
            ps.setString(9, a.getContenu());
            ps.setString(10, a.getCategorie());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Article a) throws SQLException {
        String sql = "UPDATE article SET galerie_id=?, titre=?, description=?, prix=?, date_pub=?, disponible=?, nbrarticle=?, nbrlikes=?, contenu=?, categorie=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, a.getGalerieId());
            ps.setString(2, a.getTitre());
            ps.setString(3, a.getDescription());
            ps.setDouble(4, a.getPrix());
            ps.setDate(5, Date.valueOf(a.getDatePub()));
            ps.setBoolean(6, a.isDisponible());
            ps.setInt(7, a.getNbrArticle());
            ps.setInt(8, a.getNbrLikes());
            ps.setString(9, a.getContenu());
            ps.setString(10, a.getCategorie());
            ps.setInt(11, a.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Article a) throws SQLException {
        // Supprimer d'abord les likes associés
        String deleteLikesSql = "DELETE FROM art_like WHERE article_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteLikesSql)) {
            ps.setInt(1, a.getId());
            ps.executeUpdate();
        }

        // Supprimer les commandes associées
        String deleteCommandesSql = "DELETE FROM commande WHERE article_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteCommandesSql)) {
            ps.setInt(1, a.getId());
            ps.executeUpdate();
        }

        // Supprimer l'article
        String sql = "DELETE FROM article WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, a.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Article> findAll() throws SQLException {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM article";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Article a = new Article();
                a.setId(rs.getInt("id"));
                a.setGalerieId((Integer) rs.getObject("galerie_id"));
                a.setTitre(rs.getString("titre"));
                a.setDescription(rs.getString("description"));
                a.setPrix(rs.getDouble("prix"));
                a.setDatePub(rs.getDate("date_pub").toLocalDate());
                a.setDisponible(rs.getBoolean("disponible"));
                a.setNbrArticle(rs.getInt("nbrarticle"));
                a.setNbrLikes(rs.getInt("nbrlikes"));
                a.setContenu(rs.getString("contenu"));
                a.setCategorie(rs.getString("categorie"));
                articles.add(a);
            }
        }
        return articles;
    }

    /**
     * Récupère un article par son ID.
     *
     * @param id L'ID de l'article à rechercher.
     * @return L'objet Article trouvé, ou null si non trouvé.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public Article findById(int id) throws SQLException {
        String query = "SELECT * FROM article WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToArticle(resultSet);
                }
            }
        }
        return null;
    }

    /**
     * Incrémente le compteur de likes pour un article.
     *
     * @param articleId L'ID de l'article.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public void incrementLikeCount(int articleId) throws SQLException {
        String query = "UPDATE article SET nbrLikes = nbrLikes + 1 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, articleId);
            statement.executeUpdate();
        }
    }

    /**
     * Décrémente le compteur de likes pour un article (ne descend pas en dessous de 0).
     *
     * @param articleId L'ID de l'article.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public void decrementLikeCount(int articleId) throws SQLException {
        String query = "UPDATE article SET nbrLikes = nbrLikes - 1 WHERE id = ? AND nbrLikes > 0";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, articleId);
            statement.executeUpdate();
        }
    }

    /**
     * Décrémente la quantité disponible pour un article.
     *
     * @param articleId L'ID de l'article.
     * @param quantityToDecrement La quantité à décrémenter.
     * @throws SQLException Si une erreur se produit ou si la quantité devient négative.
     */
    public void decrementQuantity(int articleId, int quantityToDecrement) throws SQLException {
        // Optionnel : Vérifier d'abord si la quantité est suffisante pour éviter les erreurs DB
        Article article = findById(articleId);
        if (article != null && article.getNbrArticle() < quantityToDecrement) {
            throw new SQLException("Quantité insuffisante en stock pour l'article ID: " + articleId);
        }
        
        String query = "UPDATE article SET nbrarticle = nbrarticle - ? WHERE id = ? AND nbrarticle >= ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, quantityToDecrement);
            statement.setInt(2, articleId);
            statement.setInt(3, quantityToDecrement); // Assure de ne pas descendre sous 0

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                // Cela peut arriver si la quantité était déjà insuffisante (condition WHERE)
                throw new SQLException("Échec de la décrémentation de la quantité (quantité insuffisante ou article non trouvé) pour ID: " + articleId);
            }
        }
    }

    private Article mapResultSetToArticle(ResultSet resultSet) throws SQLException {
        Article article = new Article();
        article.setId(resultSet.getInt("id"));
        article.setTitre(resultSet.getString("titre"));
        article.setDescription(resultSet.getString("description"));
        article.setContenu(resultSet.getString("contenu"));
        article.setPrix(resultSet.getDouble("prix"));
        article.setDatePub(resultSet.getDate("date_pub").toLocalDate());
        article.setNbrLikes(resultSet.getInt("nbrLikes")); // Correction nbr_likes -> nbrLikes si c'est le nom de la colonne
        article.setCategorie(resultSet.getString("categorie"));
        article.setGalerieId(resultSet.getInt("galerie_id"));
        if (resultSet.wasNull()) {
            article.setGalerieId(null); 
        }
        article.setNbrArticle(resultSet.getInt("nbrarticle"));
        article.setDisponible(resultSet.getBoolean("disponible"));
        return article;
    }
}

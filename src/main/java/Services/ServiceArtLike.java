package Services;

import Models.Art_like;

import java.sql.*;
import java.time.LocalDateTime;

public class ServiceArtLike {
    private Connection connection;

    public ServiceArtLike(Connection connection) {
        this.connection = connection;
    }

    /**
     * Vérifie si un utilisateur a aimé un article spécifique.
     *
     * @param userId    L'ID de l'utilisateur.
     * @param articleId L'ID de l'article.
     * @return true si l'utilisateur a aimé l'article, false sinon.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public boolean isLiked(int userId, int articleId) throws SQLException {
        String query = "SELECT COUNT(*) FROM art_like WHERE user_id = ? AND article_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, articleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Ajoute un like pour un utilisateur sur un article.
     *
     * @param userId    L'ID de l'utilisateur.
     * @param articleId L'ID de l'article.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public void addLike(int userId, int articleId) throws SQLException {
        // Vérifier d'abord si le like existe déjà pour éviter les doublons (optionnel, dépend de la contrainte DB)
        if (!isLiked(userId, articleId)) {
            String query = "INSERT INTO art_like (user_id, article_id, liked_at) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, articleId);
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                statement.executeUpdate();
            }
        } else {
            System.out.println("Like déjà existant pour user " + userId + " sur article " + articleId);
        }
    }

    /**
     * Supprime un like pour un utilisateur sur un article.
     *
     * @param userId    L'ID de l'utilisateur.
     * @param articleId L'ID de l'article.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public void removeLike(int userId, int articleId) throws SQLException {
        String query = "DELETE FROM art_like WHERE user_id = ? AND article_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, articleId);
            statement.executeUpdate();
        }
    }

    /**
     * Obtient le nombre total de likes pour un article spécifique.
     *
     * @param articleId L'ID de l'article.
     * @return Le nombre de likes.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public int getLikeCount(int articleId) throws SQLException {
        String query = "SELECT COUNT(*) FROM art_like WHERE article_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, articleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Trouve un like spécifique par utilisateur et article.
     *
     * @param userId    L'ID de l'utilisateur.
     * @param articleId L'ID de l'article.
     * @return L'objet Art_like s'il existe, null sinon.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public Art_like findLike(int userId, int articleId) throws SQLException {
        String query = "SELECT * FROM art_like WHERE user_id = ? AND article_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, articleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Art_like like = new Art_like();
                    like.setId(resultSet.getInt("id"));
                    like.setUserId(resultSet.getInt("user_id"));
                    like.setArticleId(resultSet.getInt("article_id"));
                    Timestamp ts = resultSet.getTimestamp("liked_at");
                    if (ts != null) {
                        like.setLikedAt(ts.toLocalDateTime());
                    }
                    return like;
                }
            }
        }
        return null;
    }
}

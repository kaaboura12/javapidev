package Services;

import Models.Post;
import Utils.MyDb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePost {
    private Connection connection;

    public ServicePost() {
        try {
            connection = MyDb.getInstance().getConnection();
        } catch (Exception e) {
            System.err.println("Erreur de connexion dans ServicePost: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create
    public boolean createPost(Post p) {
        String query = "INSERT INTO post (author_id, title, content, created_at, updated_at, image) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, p.getAuthorId());
            statement.setString(2, p.getTitle());
            statement.setString(3, p.getContent());
            statement.setTimestamp(4, Timestamp.valueOf(p.getCreatedAt()));
            statement.setTimestamp(5, Timestamp.valueOf(p.getUpdatedAt()));
            statement.setString(6, p.getImage());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création du post a échoué, aucune ligne n'a été affectée.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    p.setId(generatedKeys.getInt(1));
                    return true;
                } else {
                    throw new SQLException("La création du post a échoué, aucun ID n'a été généré.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création du post: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Read
    public Post getPostById(int id) {
        String query = "SELECT * FROM post WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Post p = new Post();
                p.setId(resultSet.getInt("id"));
                p.setAuthorId(resultSet.getInt("author_id"));
                p.setTitle(resultSet.getString("title"));
                p.setContent(resultSet.getString("content"));
                p.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                Timestamp ts = resultSet.getTimestamp("updated_at");
                if (ts != null) {
                    p.setUpdatedAt(ts.toLocalDateTime());
                }
                p.setImage(resultSet.getString("image"));
                return p;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du post: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM post ORDER BY created_at DESC";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Post p = new Post();
                p.setId(resultSet.getInt("id"));
                p.setAuthorId(resultSet.getInt("author_id"));
                p.setTitle(resultSet.getString("title"));
                p.setContent(resultSet.getString("content"));
                p.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                Timestamp ts = resultSet.getTimestamp("updated_at");
                if (ts != null) {
                    p.setUpdatedAt(ts.toLocalDateTime());
                }
                p.setImage(resultSet.getString("image"));
                posts.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des posts: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }

    // Update
    public boolean updatePost(Post p) {
        System.out.println("ServicePost.updatePost called for post ID: " + p.getId());
        String query = "UPDATE post SET author_id = ?, title = ?, content = ?, updated_at = ?, image = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, p.getAuthorId());
            statement.setString(2, p.getTitle());
            statement.setString(3, p.getContent());
            statement.setTimestamp(4, Timestamp.valueOf(p.getUpdatedAt()));
            statement.setString(5, p.getImage());
            statement.setInt(6, p.getId());

            System.out.println("Executing update query for post ID: " + p.getId());
            int affectedRows = statement.executeUpdate();
            System.out.println("Update affected " + affectedRows + " rows");
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du post: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete
    public boolean deletePost(int id) {
        try {
            // Supprimer les réactions associées
            String deleteReactionsQuery = "DELETE FROM reaction WHERE post_id = ?";
            try (PreparedStatement reactionStmt = connection.prepareStatement(deleteReactionsQuery)) {
                reactionStmt.setInt(1, id);
                reactionStmt.executeUpdate();
            }

            // Supprimer les likes associés
            String deleteLikesQuery = "DELETE FROM `like` WHERE post_id = ?";
            try (PreparedStatement likeStmt = connection.prepareStatement(deleteLikesQuery)) {
                likeStmt.setInt(1, id);
                likeStmt.executeUpdate();
            }

            // Supprimer les commentaires associés
            String deleteCommentsQuery = "DELETE FROM comment WHERE post_id = ?";
            try (PreparedStatement commentStmt = connection.prepareStatement(deleteCommentsQuery)) {
                commentStmt.setInt(1, id);
                commentStmt.executeUpdate();
            }

            // Supprimer le post
            String deletePostQuery = "DELETE FROM post WHERE id = ?";
            try (PreparedStatement postStmt = connection.prepareStatement(deletePostQuery)) {
                postStmt.setInt(1, id);
                int affectedRows = postStmt.executeUpdate();
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du post: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}

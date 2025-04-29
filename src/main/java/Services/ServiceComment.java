package Services;

import Models.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceComment {
    private Connection connection;

    public ServiceComment() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidatabase", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Create
    public boolean createComment(Comment comment) {
        String query = "INSERT INTO comment (post_id, author_id, content, gif_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, comment.getPostId());
            statement.setInt(2, comment.getUserId());
            statement.setString(3, comment.getContent());
            statement.setString(4, comment.getGifUrl());
            statement.setTimestamp(5, Timestamp.valueOf(comment.getCreatedAt()));
            statement.setTimestamp(6, Timestamp.valueOf(comment.getUpdatedAt()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read
    public List<Comment> getCommentsByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE post_id = ? ORDER BY created_at DESC";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setId(resultSet.getInt("id"));
                comment.setPostId(resultSet.getInt("post_id"));
                comment.setUserId(resultSet.getInt("author_id"));
                comment.setContent(resultSet.getString("content"));
                comment.setGifUrl(resultSet.getString("gif_url"));
                comment.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                comment.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    // Update
    public void updateComment(Comment comment) {
        String query = "UPDATE comment SET content = ?, gif_url = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, comment.getContent());
            statement.setString(2, comment.getGifUrl());
            statement.setTimestamp(3, Timestamp.valueOf(comment.getUpdatedAt()));
            statement.setInt(4, comment.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete
    public boolean deleteComment(int id) {
        String query = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

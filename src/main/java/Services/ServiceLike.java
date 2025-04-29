package Services;

import Models.Like;
import Utils.MyDb;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceLike {
    private Connection connection;
    private Statement statement;
    private PreparedStatement preparedStatement;

    // Constructor that initializes the database connection
    public ServiceLike() {
        try {
            connection = MyDb.getInstance().getConnection();
            statement = connection.createStatement();
        } catch (SQLException ex) {
            System.err.println("Error connecting to database: " + ex.getMessage());
            // Don't throw exception, just log it so UI can still load
        }
    }

    // Add a new like to a post
    public boolean addLike(int userId, int postId) {
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }

        try {
            // Check if user already liked this post
            if (hasUserLikedPost(userId, postId)) {
                return false; // User already liked this post
            }

            String query = "INSERT INTO `like` (user_id, post_id, created_at) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, postId);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("Error adding like: " + ex.getMessage());
            return false;
        }
    }

    // Remove a like from a post
    public boolean removeLike(int userId, int postId) {
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }

        try {
            String query = "DELETE FROM `like` WHERE user_id = ? AND post_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, postId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("Error removing like: " + ex.getMessage());
            return false;
        }
    }

    // Toggle like status (add if not liked, remove if already liked)
    public boolean toggleLike(int userId, int postId) {
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }

        if (hasUserLikedPost(userId, postId)) {
            return removeLike(userId, postId);
        } else {
            return addLike(userId, postId);
        }
    }

    // Check if a user has liked a specific post
    public boolean hasUserLikedPost(int userId, int postId) {
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }

        try {
            String query = "SELECT COUNT(*) FROM `like` WHERE user_id = ? AND post_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, postId);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException ex) {
            System.err.println("Error checking like status: " + ex.getMessage());
            return false;
        }
    }

    // Count likes for a specific post
    public int countLikesForPost(int postId) {
        if (connection == null) {
            System.err.println("Database connection not available");
            return 0;
        }

        try {
            String query = "SELECT COUNT(*) FROM `like` WHERE post_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, postId);

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            System.err.println("Error counting likes: " + ex.getMessage());
            return 0;
        }
    }

    // Get all likes for a post
    public List<Like> getLikesForPost(int postId) {
        List<Like> likes = new ArrayList<>();

        if (connection == null) {
            System.err.println("Database connection not available");
            return likes;
        }

        try {
            String query = "SELECT * FROM `like` WHERE post_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, postId);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Like like = new Like();
                like.setId(rs.getInt("id"));
                like.setUserId(rs.getInt("user_id"));
                like.setPostId(rs.getInt("post_id"));
                like.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                likes.add(like);
            }
        } catch (SQLException ex) {
            System.err.println("Error getting likes: " + ex.getMessage());
        }
        return likes;
    }

    // Get all users who liked a post
    public List<Integer> getUsersWhoLikedPost(int postId) {
        List<Integer> userIds = new ArrayList<>();

        if (connection == null) {
            System.err.println("Database connection not available");
            return userIds;
        }

        try {
            String query = "SELECT user_id FROM `like` WHERE post_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, postId);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException ex) {
            System.err.println("Error getting users who liked: " + ex.getMessage());
        }
        return userIds;
    }

    // Delete all likes for a post (used when deleting a post)
    public boolean deleteAllLikesForPost(int postId) {
        if (connection == null) {
            System.err.println("Database connection not available");
            return false;
        }

        try {
            String query = "DELETE FROM `like` WHERE post_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, postId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println("Error deleting likes for post: " + ex.getMessage());
            return false;
        }
    }

    // Close database connection
    public void closeConnection() {
        try {
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException ex) {
            System.err.println("Error closing connection: " + ex.getMessage());
        }
    }

    public int getLikeCountByPostId(int id) {
        return countLikesForPost(id);
    }
}

package Services;

import Models.Reaction;
import Utils.MyDb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServiceReaction {
    private Connection conn;
    private PreparedStatement pst;

    public ServiceReaction() {
        conn = MyDb.getInstance().getConnection();
    }

    public boolean addReaction(Reaction reaction) {
        String query = "INSERT INTO reaction (user_id, post_id, type, emoji, created_at) VALUES (?, ?, ?, ?, ?)";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, reaction.getUserId());
            pst.setInt(2, reaction.getPostId());
            pst.setString(3, reaction.getType());
            pst.setString(4, reaction.getEmoji());
            pst.setTimestamp(5, Timestamp.valueOf(reaction.getCreatedAt()));
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    public boolean removeReaction(int userId, int postId) {
        String query = "DELETE FROM reaction WHERE user_id = ? AND post_id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            pst.setInt(2, postId);
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    public boolean updateReaction(Reaction reaction) {
        String query = "UPDATE reaction SET type = ?, emoji = ? WHERE user_id = ? AND post_id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, reaction.getType());
            pst.setString(2, reaction.getEmoji());
            pst.setInt(3, reaction.getUserId());
            pst.setInt(4, reaction.getPostId());
            
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    public List<Reaction> getReactionsByPostId(int postId) {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction WHERE post_id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, postId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Reaction reaction = new Reaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("post_id"),
                    rs.getString("type"),
                    rs.getString("emoji"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                reactions.add(reaction);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return reactions;
    }

    public Map<String, Integer> getReactionCountsByPostId(int postId) {
        Map<String, Integer> counts = new HashMap<>();
        String query = "SELECT type, COUNT(*) as count FROM reaction WHERE post_id = ? GROUP BY type";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, postId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                counts.put(rs.getString("type"), rs.getInt("count"));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return counts;
    }

    public Reaction getUserReaction(int userId, int postId) {
        String query = "SELECT * FROM reaction WHERE user_id = ? AND post_id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            pst.setInt(2, postId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return new Reaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("post_id"),
                    rs.getString("type"),
                    rs.getString("emoji"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    public boolean hasUserReacted(int userId, int postId) {
        String query = "SELECT COUNT(*) as count FROM reaction WHERE user_id = ? AND post_id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            pst.setInt(2, postId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }

    public boolean toggleReaction(int userId, int postId, String type, String emoji) {
        if (hasUserReacted(userId, postId)) {
            Reaction existingReaction = getUserReaction(userId, postId);
            if (existingReaction.getType().equals(type) && existingReaction.getEmoji().equals(emoji)) {
                // If same reaction type and emoji, remove it
                return removeReaction(userId, postId);
            } else {
                // If different reaction type or emoji, update it
                Reaction updatedReaction = new Reaction();
                updatedReaction.setUserId(userId);
                updatedReaction.setPostId(postId);
                updatedReaction.setType(type);
                updatedReaction.setEmoji(emoji);
                return updateReaction(updatedReaction);
            }
        } else {
            // Add new reaction
            Reaction newReaction = new Reaction();
            newReaction.setUserId(userId);
            newReaction.setPostId(postId);
            newReaction.setType(type);
            newReaction.setEmoji(emoji);
            newReaction.setCreatedAt(java.time.LocalDateTime.now());
            return addReaction(newReaction);
        }
    }
}

package Models;

import java.time.LocalDateTime;

public class Reaction {
    private int id;
    private int userId;
    private int postId;
    private String type;        // This is the emoji type
    private String emoji;
    private LocalDateTime createdAt;

    public Reaction() {
    }

    public Reaction(int id, int userId, int postId, String type, String emoji, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.type = type;
        this.emoji = emoji;
        this.createdAt = createdAt;
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

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", postId=" + postId +
                ", type='" + type + '\'' +
                ", emoji='" + emoji + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

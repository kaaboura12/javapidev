package Models;

import java.time.LocalDateTime;

public class Like {
    private int id;
    private int user_id;
    private int post_id;
    private LocalDateTime created_at;

    // Default constructor
    public Like() {
    }

    // Constructor with userId and postId
    public Like(int user_id, int post_id) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.created_at = LocalDateTime.now();
    }

    // Full constructor
    public Like(int id, int user_id, int post_id, LocalDateTime created_at) {
        this.id = id;
        this.user_id = user_id;
        this.post_id = post_id;
        this.created_at = created_at;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public int getPostId() {
        return post_id;
    }

    public void setPostId(int post_id) {
        this.post_id = post_id;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", post_id=" + post_id +
                ", created_at=" + created_at +
                '}';
    }
}

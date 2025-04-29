package Models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Art_like {
    private int id;
    private int user_id;
    private int article_id;
    private LocalDateTime liked_at;

    // Constructeurs
    public Art_like() {
    }

    public Art_like(int id, int user_id, int article_id, LocalDateTime liked_at) {
        this.id = id;
        this.user_id = user_id;
        this.article_id = article_id;
        this.liked_at = liked_at;
    }

    public Art_like(int user_id, int article_id) {
        this.user_id = user_id;
        this.article_id = article_id;
        this.liked_at = LocalDateTime.now(); // Default to now
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return user_id;
    }

    public int getArticleId() {
        return article_id;
    }

    public LocalDateTime getLikedAt() {
        return liked_at;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public void setArticleId(int article_id) {
        this.article_id = article_id;
    }

    public void setLikedAt(LocalDateTime liked_at) {
        this.liked_at = liked_at;
    }

    // equals, hashCode, toString (optionnel mais recommandé)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Art_like artLike = (Art_like) o;
        return id == artLike.id && user_id == artLike.user_id && article_id == artLike.article_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user_id, article_id);
    }

    @Override
    public String toString() {
        return "Art_like{" +
               "id=" + id +
               ", user_id=" + user_id +
               ", article_id=" + article_id +
               ", liked_at=" + liked_at +
               '}';
    }
}

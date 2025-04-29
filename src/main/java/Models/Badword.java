package Models;

public class Badword {
    private int id;
    private String word;
    private String replacement;

    // Default constructor
    public Badword() {
    }

    // Constructor without id (for creation)
    public Badword(String word, String replacement) {
        this.word = word;
        this.replacement = replacement;
    }

    // Full constructor
    public Badword(int id, String word, String replacement) {
        this.id = id;
        this.word = word;
        this.replacement = replacement;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    @Override
    public String toString() {
        return "Badword{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", replacement='" + replacement + '\'' +
                '}';
    }
}

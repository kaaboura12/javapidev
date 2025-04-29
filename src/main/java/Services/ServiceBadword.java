package Services;

import Models.Badword;
import Utils.MyDb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ServiceBadword {
    private Connection connection;
    private PreparedStatement preparedStatement;

    public ServiceBadword() {
        connection = MyDb.getInstance().getConnection();
    }

    // Create a new bad word
    public boolean addBadword(Badword badword) {
        String query = "INSERT INTO bad_word (word, replacement) VALUES (?, ?)";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, badword.getWord().toLowerCase());
            preparedStatement.setString(2, badword.getReplacement());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    // Update an existing bad word
    public boolean updateBadword(Badword badword) {
        String query = "UPDATE bad_word SET word = ?, replacement = ? WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, badword.getWord().toLowerCase());
            preparedStatement.setString(2, badword.getReplacement());
            preparedStatement.setInt(3, badword.getId());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    // Delete a bad word
    public boolean deleteBadword(int id) {
        String query = "DELETE FROM bad_word WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
    }

    // Get a bad word by ID
    public Badword getBadwordById(int id) {
        String query = "SELECT * FROM bad_word WHERE id = ?";
        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                return new Badword(
                    rs.getInt("id"),
                    rs.getString("word"),
                    rs.getString("replacement")
                );
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return null;
    }

    // Get all bad words
    public List<Badword> getAllBadwords() {
        List<Badword> badwords = new ArrayList<>();
        String query = "SELECT * FROM bad_word";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                badwords.add(new Badword(
                    rs.getInt("id"),
                    rs.getString("word"),
                    rs.getString("replacement")
                ));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return badwords;
    }

    // Filter text to replace bad words with their replacements
    public String filterText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String filteredText = text;
        List<Badword> badwords = getAllBadwords();

        for (Badword badword : badwords) {
            // Create a case-insensitive pattern
            String pattern = "(?i)\\b" + Pattern.quote(badword.getWord()) + "\\b";
            filteredText = filteredText.replaceAll(pattern, badword.getReplacement());
        }

        return filteredText;
    }

    // Check if text contains any bad words
    public boolean containsBadwords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        List<Badword> badwords = getAllBadwords();

        for (Badword badword : badwords) {
            String pattern = "\\b" + Pattern.quote(badword.getWord().toLowerCase()) + "\\b";
            if (Pattern.compile(pattern).matcher(lowerText).find()) {
                return true;
            }
        }

        return false;
    }

    // Get all bad words found in a text
    public List<String> findBadwords(String text) {
        List<String> foundBadwords = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return foundBadwords;
        }

        String lowerText = text.toLowerCase();
        List<Badword> badwords = getAllBadwords();

        for (Badword badword : badwords) {
            String pattern = "\\b" + Pattern.quote(badword.getWord().toLowerCase()) + "\\b";
            if (Pattern.compile(pattern).matcher(lowerText).find()) {
                foundBadwords.add(badword.getWord());
            }
        }

        return foundBadwords;
    }

    // Close the database connection
    public void closeConnection() {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
}

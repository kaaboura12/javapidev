package Services;

import Models.User;
import Models.Inscription;
import Utils.MyDb;
import java.sql.*;
import java.time.LocalDate;

public class InscriptionService {
    private final Connection conn;
    private final UserService userService;
    public static final int TEST_USER_ID = 2; // Changed from 1 to 2 to match existing user ID

    public InscriptionService() {
        conn = MyDb.getInstance().getConnection();
        userService = new UserService();
        ensureTestUserExists();
    }

    private void ensureTestUserExists() {
        try {
            User existingUser = userService.findById(TEST_USER_ID);
            
            if (existingUser == null) {
                // Create test user if it doesn't exist
                User testUser = new User(
                    "Test",           // nom
                    "User",           // prenom 
                    "test@test.com",  // email
                    "test123",        // password
                    "12345678",       // numtlf
                    25,               // age
                    null,             // avatar_url
                    "USER",           // roles (not role)
                    true              // is_verified
                );
                testUser.setUser_id(TEST_USER_ID);
                userService.insert(testUser);
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring test user exists: " + e.getMessage());
        }
    }

    public void insert(Inscription inscription) throws SQLException {
        conn.setAutoCommit(false); // Start transaction
        try {
            // First insert the inscription
            String query = "INSERT INTO inscription (formation_id, user_id, date_inscription, statut, date_creation) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, inscription.getFormation_id());
            ps.setInt(2, inscription.getUser_id());
            ps.setDate(3, Date.valueOf(inscription.getDate_inscription()));
            ps.setString(4, inscription.getStatut());
            ps.setDate(5, Date.valueOf(inscription.getDate_creation()));
            ps.executeUpdate();

            // Then decrease the number of available spots
            String updateQuery = "UPDATE formation SET nbrpart = nbrpart - 1 WHERE formation_id = ? AND nbrpart > 0";
            PreparedStatement updatePs = conn.prepareStatement(updateQuery);
            updatePs.setInt(1, inscription.getFormation_id());
            int updatedRows = updatePs.executeUpdate();
            
            if (updatedRows == 0) {
                throw new SQLException("No more spots available for this formation");
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            conn.rollback(); // Rollback on error
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean checkIfAlreadyRegistered(int userId, int formationId) throws SQLException {
        String query = "SELECT COUNT(*) FROM inscription WHERE user_id = ? AND formation_id = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, formationId);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}

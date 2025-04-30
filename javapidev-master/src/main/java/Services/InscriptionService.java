package Services;

import Models.User;
import Models.Inscription;
import Utils.MyDb;
import java.sql.*;
import java.time.LocalDate;

public class InscriptionService {
    private final Connection conn;
    private final UserService userService;
    public int TEST_USER_ID = 2;





    public void insert(Inscription inscription) throws SQLException {
        conn.setAutoCommit(false); // Start transaction
        try {
            // First insert the inscription
            String query = "INSERT INTO inscription (formation_id, user_id, date_inscription, statut, date_creation) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, inscription.getFormation_id());
            ps.setInt(2,);
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

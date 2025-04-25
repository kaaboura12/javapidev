package Services;

import Models.Categorie;
import Utils.MyDb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private Connection conn;

    public CategorieService() {
        conn = MyDb.getInstance().getConnection();
    }
    
    public void insert(Categorie categorie) throws SQLException {
        String query = "INSERT INTO categorie (nom, description, date_creation) VALUES (?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, categorie.getNom());
        pst.setString(2, categorie.getDescription());
        pst.setTimestamp(3, Timestamp.valueOf(categorie.getDateCreation()));
        pst.executeUpdate();
    }
    
    public void update(Categorie categorie) throws SQLException {
        String query = "UPDATE categorie SET nom = ?, description = ? WHERE categorie_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, categorie.getNom());
        pst.setString(2, categorie.getDescription());
        pst.setInt(3, categorie.getId());
        pst.executeUpdate();
    }
    
    public void delete(Categorie categorie) throws SQLException {
        Connection conn = null;
        PreparedStatement pstDeleteFormations = null;
        PreparedStatement pstDeleteCategory = null;
        
        try {
            // Get a connection and disable auto-commit for transaction
            conn = MyDb.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // First delete all formations related to this category
            String deleteFormationsQuery = "DELETE FROM formation WHERE categorie_id = ?";
            pstDeleteFormations = conn.prepareStatement(deleteFormationsQuery);
            pstDeleteFormations.setInt(1, categorie.getId());
            pstDeleteFormations.executeUpdate();
            
            // Then delete the category
            String deleteCategoryQuery = "DELETE FROM categorie WHERE categorie_id = ?";
            pstDeleteCategory = conn.prepareStatement(deleteCategoryQuery);
            pstDeleteCategory.setInt(1, categorie.getId());
            pstDeleteCategory.executeUpdate();
            
            // Commit the transaction
            conn.commit();
        } catch (SQLException e) {
            // Rollback on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Error rolling back transaction: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            // Clean up resources and restore auto-commit
            if (pstDeleteFormations != null) {
                pstDeleteFormations.close();
            }
            if (pstDeleteCategory != null) {
                pstDeleteCategory.close();
            }
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    public Categorie getById(int id) throws SQLException {
        String query = "SELECT * FROM categorie WHERE categorie_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            Categorie categorie = new Categorie();
            categorie.setId(rs.getInt("categorie_id"));
            categorie.setNom(rs.getString("nom"));
            categorie.setDescription(rs.getString("description"));
            categorie.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            return categorie;
        }
        return null;
    }

    public List<Categorie> getAll() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String query = "SELECT * FROM categorie";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        
        while (rs.next()) {
            Categorie categorie = new Categorie();
            categorie.setId(rs.getInt("categorie_id"));
            categorie.setNom(rs.getString("nom"));
            categorie.setDescription(rs.getString("description"));
            categorie.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            categories.add(categorie);
        }
        return categories;
    }
}

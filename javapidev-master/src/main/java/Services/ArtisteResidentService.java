package Services;

import Models.ArtisteResident;
import Models.User;
import Utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtisteResidentService {
    
    private final UserService userService = new UserService();
    
    /**
     * Récupère toutes les demandes d'artistes résidents
     * 
     * @return Liste des demandes d'artistes résidents
     * @throws SQLException En cas d'erreur SQL
     */
    public List<ArtisteResident> findAll() throws SQLException {
        List<ArtisteResident> requests = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM artiste_resident ORDER BY date_created DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                ArtisteResident request = mapResultSetToArtisteResident(rs);
                requests.add(request);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        
        return requests;
    }
    
    /**
     * Récupère les demandes par statut
     * 
     * @param status Le statut des demandes à récupérer (PENDING, APPROVED, REJECTED)
     * @return Liste des demandes avec le statut spécifié
     * @throws SQLException En cas d'erreur SQL
     */
    public List<ArtisteResident> findByStatus(String status) throws SQLException {
        List<ArtisteResident> requests = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM artiste_resident WHERE status = ? ORDER BY date_created DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                ArtisteResident request = mapResultSetToArtisteResident(rs);
                requests.add(request);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        
        return requests;
    }
    
    /**
     * Récupère une demande par son ID
     * 
     * @param id L'ID de la demande
     * @return La demande correspondante, ou null si non trouvée
     * @throws SQLException En cas d'erreur SQL
     */
    public ArtisteResident findById(int id) throws SQLException {
        ArtisteResident request = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "SELECT * FROM artiste_resident WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                request = mapResultSetToArtisteResident(rs);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        
        return request;
    }
    
    /**
     * Approuve une demande d'artiste résident et met à jour le rôle de l'utilisateur
     * 
     * @param requestId L'ID de la demande à approuver
     * @return true si la demande a été approuvée avec succès
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean approveRequest(int requestId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Update request status
            String updateStatusSql = "UPDATE artiste_resident SET status = 'APPROVED' WHERE id = ?";
            stmt = conn.prepareStatement(updateStatusSql);
            stmt.setInt(1, requestId);
            int rowsUpdated = stmt.executeUpdate();
            stmt.close();
            
            if (rowsUpdated == 0) {
                conn.rollback();
                return false;
            }
            
            // 2. Get the user ID from the request
            String getUserIdSql = "SELECT user_id FROM artiste_resident WHERE id = ?";
            stmt = conn.prepareStatement(getUserIdSql);
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                conn.rollback();
                return false;
            }
            
            int userId = rs.getInt("user_id");
            rs.close();
            stmt.close();
            
            // 3. Update user's role to ARTIST
            User user = userService.findById(userId);
            if (user != null) {
                String roles = user.getRoles();
                if (!roles.contains("ARTIST")) {
                    roles = roles.isEmpty() ? "ARTIST" : roles + ",ARTIST";
                    
                    String updateUserRoleSql = "UPDATE users SET roles = ? WHERE id = ?";
                    stmt = conn.prepareStatement(updateUserRoleSql);
                    stmt.setString(1, roles);
                    stmt.setInt(2, userId);
                    stmt.executeUpdate();
                }
            }
            
            // Commit the transaction
            conn.commit();
            return true;
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Rejette une demande d'artiste résident
     * 
     * @param requestId L'ID de la demande à rejeter
     * @return true si la demande a été rejetée avec succès
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean rejectRequest(int requestId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE artiste_resident SET status = 'REJECTED' WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, requestId);
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
            
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Supprime une demande d'artiste résident
     * 
     * @param requestId L'ID de la demande à supprimer
     * @return true si la demande a été supprimée avec succès
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean deleteRequest(int requestId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "DELETE FROM artiste_resident WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, requestId);
            
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
            
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Met à jour une demande d'artiste résident
     * 
     * @param request La demande à mettre à jour
     * @return true si la demande a été mise à jour avec succès
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean update(ArtisteResident request) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            String sql = "UPDATE artiste_resident SET status = ?, description = ?, portfolio_path = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getStatus());
            stmt.setString(2, request.getDescription());
            stmt.setString(3, request.getPortfolioPath());
            stmt.setInt(4, request.getId());
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
            
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Supprime une demande d'artiste résident
     * 
     * @param requestId L'ID de la demande à supprimer
     * @return true si la demande a été supprimée avec succès
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean delete(int requestId) throws SQLException {
        return deleteRequest(requestId);
    }
    
    /**
     * Convertit un ResultSet en objet ArtisteResident
     * 
     * @param rs Le ResultSet à convertir
     * @return Un objet ArtisteResident contenant les données du ResultSet
     * @throws SQLException En cas d'erreur SQL
     */
    private ArtisteResident mapResultSetToArtisteResident(ResultSet rs) throws SQLException {
        ArtisteResident request = new ArtisteResident();
        request.setId(rs.getInt("id"));
        request.setUserId(rs.getInt("user_id"));
        request.setDescription(rs.getString("description"));
        request.setPortfolioPath(rs.getString("portfolio_path"));
        request.setDateCreated(rs.getDate("date_created"));
        request.setStatus(rs.getString("status"));
        
        // Récupérer les détails de l'utilisateur
        try {
            User user = userService.findById(request.getUserId());
            request.setUser(user);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
        }
        
        return request;
    }
} 
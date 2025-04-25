package Services;

import Models.Formation;
import Utils.MyDb;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService {
    private Connection conn;

    public FormationService() {
        conn = MyDb.getInstance().getConnection();
    }

    public void insert(Formation formation) throws SQLException {
        String query = "INSERT INTO formation (titre, description, date_debut, date_fin, nbrpart, prix, categorie_id, date_creation, video) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, formation.getTitre());
        pst.setString(2, formation.getDescription());
        pst.setTimestamp(3, Timestamp.valueOf(formation.getDateDebut()));
        pst.setTimestamp(4, Timestamp.valueOf(formation.getDateFin()));
        pst.setInt(5, formation.getNbrpart());
        pst.setDouble(6, formation.getPrix());
        pst.setInt(7, formation.getCategorie().getId());
        pst.setTimestamp(8, Timestamp.valueOf(formation.getDateCreation()));
        pst.setString(9, formation.getVideo());
        pst.executeUpdate();
    }
    
    public void update(Formation formation) throws SQLException {
        String query = "UPDATE formation SET titre = ?, description = ?, date_debut = ?, date_fin = ?, nbrpart = ?, prix = ?, categorie_id = ?, video = ? WHERE formation_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, formation.getTitre());
        pst.setString(2, formation.getDescription());
        pst.setTimestamp(3, Timestamp.valueOf(formation.getDateDebut()));
        pst.setTimestamp(4, Timestamp.valueOf(formation.getDateFin()));
        pst.setInt(5, formation.getNbrpart());
        pst.setDouble(6, formation.getPrix());
        pst.setInt(7, formation.getCategorie().getId());
        pst.setString(8, formation.getVideo());
        pst.setInt(9, formation.getId());
        pst.executeUpdate();
    }
    
    public void delete(Formation formation) throws SQLException {
        String query = "DELETE FROM formation WHERE formation_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, formation.getId());
        pst.executeUpdate();
    }
    
    public Formation getById(int id) throws SQLException {
        String query = "SELECT * FROM formation WHERE formation_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        
        CategorieService categorieService = new CategorieService();
        
        if (rs.next()) {
            Formation formation = new Formation();
            formation.setId(rs.getInt("formation_id"));
            formation.setTitre(rs.getString("titre"));
            formation.setDescription(rs.getString("description"));
            formation.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
            formation.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
            formation.setNbrpart(rs.getInt("nbrpart"));
            formation.setPrix(rs.getDouble("prix"));
            formation.setCategorie(categorieService.getById(rs.getInt("categorie_id")));
            formation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            formation.setVideo(rs.getString("video"));
            return formation;
        }
        return null;
    }

    public List<Formation> getAll() throws SQLException {
        List<Formation> formations = new ArrayList<>();
        String query = "SELECT * FROM formation";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        
        CategorieService categorieService = new CategorieService();
        
        while (rs.next()) {
            Formation formation = new Formation();
            formation.setId(rs.getInt("formation_id"));
            formation.setTitre(rs.getString("titre"));
            formation.setDescription(rs.getString("description"));
            formation.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
            formation.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
            formation.setNbrpart(rs.getInt("nbrpart"));
            formation.setPrix(rs.getDouble("prix"));
            formation.setCategorie(categorieService.getById(rs.getInt("categorie_id")));
            formation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            formation.setVideo(rs.getString("video"));
            formations.add(formation);
        }
        return formations;
    }

    public List<Formation> getByCategorie(int categorieId) throws SQLException {
        List<Formation> formations = new ArrayList<>();
        String query = "SELECT * FROM formation WHERE categorie_id = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setInt(1, categorieId);
        ResultSet rs = pst.executeQuery();
        
        CategorieService categorieService = new CategorieService();
        
        while (rs.next()) {
            Formation formation = new Formation();
            formation.setId(rs.getInt("formation_id"));
            formation.setTitre(rs.getString("titre"));
            formation.setDescription(rs.getString("description"));
            formation.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
            formation.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
            formation.setNbrpart(rs.getInt("nbrpart"));
            formation.setPrix(rs.getDouble("prix"));
            formation.setCategorie(categorieService.getById(rs.getInt("categorie_id")));
            formation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
            formation.setVideo(rs.getString("video"));
            formations.add(formation);
        }
        
        return formations;
    }
}

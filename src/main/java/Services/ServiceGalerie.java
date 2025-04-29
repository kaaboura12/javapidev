package Services;

import Interfaces.ICrud;
import Models.Galerie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceGalerie implements ICrud<Galerie> {

    private final Connection con;

    public ServiceGalerie(Connection con) {
        this.con = con;
    }

    @Override
    public void insert(Galerie g) throws SQLException {
        // Vérifier si l'utilisateur existe
        String checkUserSql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
        try (PreparedStatement checkPs = con.prepareStatement(checkUserSql)) {
            checkPs.setObject(1, g.getUserId());
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("L'utilisateur spécifié n'existe pas");
            }
        }

        String sql = "INSERT INTO galerie (user_id, nom, datecreation, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, g.getUserId());
            ps.setString(2, g.getNom());
            ps.setTimestamp(3, Timestamp.valueOf(g.getDateCreation()));
            ps.setString(4, g.getDescription());
            ps.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    g.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Galerie g) throws SQLException {
        // Vérifier si la galerie existe
        String checkSql = "SELECT COUNT(*) FROM galerie WHERE id = ?";
        try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setInt(1, g.getId());
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("La galerie n'existe pas");
            }
        }

        // Vérifier si l'utilisateur existe
        String checkUserSql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
        try (PreparedStatement checkPs = con.prepareStatement(checkUserSql)) {
            checkPs.setObject(1, g.getUserId());
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("L'utilisateur spécifié n'existe pas");
            }
        }

        String sql = "UPDATE galerie SET user_id=?, nom=?, datecreation=?, description=? WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, g.getUserId());
            ps.setString(2, g.getNom());
            ps.setTimestamp(3, Timestamp.valueOf(g.getDateCreation()));
            ps.setString(4, g.getDescription());
            ps.setInt(5, g.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Galerie g) throws SQLException {
        // Vérifier si la galerie existe
        String checkSql = "SELECT COUNT(*) FROM galerie WHERE id = ?";
        try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
            checkPs.setInt(1, g.getId());
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new SQLException("La galerie n'existe pas");
            }
        }

        // Supprimer d'abord les commandes des articles de la galerie
        String deleteCommandesSql = "DELETE c FROM commande c " +
                                  "INNER JOIN article a ON c.article_id = a.id " +
                                  "WHERE a.galerie_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteCommandesSql)) {
            ps.setInt(1, g.getId());
            ps.executeUpdate();
        }

        // Supprimer les likes des articles de la galerie
        String deleteLikesSql = "DELETE al FROM art_like al " +
                              "INNER JOIN article a ON al.article_id = a.id " +
                              "WHERE a.galerie_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteLikesSql)) {
            ps.setInt(1, g.getId());
            ps.executeUpdate();
        }

        // Supprimer les articles de la galerie
        String deleteArticlesSql = "DELETE FROM article WHERE galerie_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteArticlesSql)) {
            ps.setInt(1, g.getId());
            ps.executeUpdate();
        }

        // Supprimer la galerie
        String sql = "DELETE FROM galerie WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, g.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Galerie> findAll() throws SQLException {
        List<Galerie> galeries = new ArrayList<>();
        String sql = "SELECT * FROM galerie";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Galerie g = new Galerie();
                g.setId(rs.getInt("id"));
                g.setUserId((Integer) rs.getObject("user_id"));
                g.setNom(rs.getString("nom"));
                g.setDateCreation(rs.getTimestamp("datecreation").toLocalDateTime());
                g.setDescription(rs.getString("description"));
                galeries.add(g);
            }
        }
        return galeries;
    }

    public List<Galerie> findByUserId(int userId) throws SQLException {
        List<Galerie> galeries = new ArrayList<>();
        String sql = "SELECT * FROM galerie WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, 15);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Galerie g = new Galerie();
                    g.setId(rs.getInt("id"));
                    g.setUserId((Integer) rs.getObject("user_id"));
                    g.setNom(rs.getString("nom"));
                    g.setDateCreation(rs.getTimestamp("datecreation").toLocalDateTime());
                    g.setDescription(rs.getString("description"));
                    galeries.add(g);
                }
            }
        }
        return galeries;
    }
}

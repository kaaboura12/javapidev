package Services;

import Models.Commande;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommande {

    private Connection connection;

    public ServiceCommande(Connection connection) {
        this.connection = connection;
    }

    /**
     * Insère une nouvelle commande dans la base de données.
     *
     * @param commande L'objet Commande à insérer.
     * @throws SQLException Si une erreur de base de données se produit.
     */
    public void insert(Commande commande) throws SQLException {
        String query = "INSERT INTO commande (user_id, article_id, numero, date_commande, quantite, prix_unitaire, total) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, commande.getUserId());
            ps.setInt(2, commande.getArticleId());
            ps.setString(3, commande.getNumero());
            ps.setTimestamp(4, Timestamp.valueOf(commande.getDateCommande()));
            ps.setInt(5, commande.getQuantite());
            ps.setDouble(6, commande.getPrixUnitaire());
            ps.setDouble(7, commande.getTotal());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création de la commande a échoué, aucune ligne affectée.");
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commande.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de la commande a échoué, aucun ID obtenu.");
                }
            }
            System.out.println("Commande insérée avec succès : " + commande);
        } catch (SQLException e) {
             System.err.println("Erreur SQL lors de l'insertion de la commande: " + e.getMessage());
             throw e; // Relancer pour que le contrôleur puisse la gérer
        }
    }

    /**
     * Récupère toutes les commandes d'un utilisateur spécifique.
     *
     * @param userId L'ID de l'utilisateur
     * @return Liste des commandes de l'utilisateur
     * @throws SQLException Si une erreur de base de données se produit
     */
    public List<Commande> getCommandesByUserId(int userId) throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande WHERE user_id = ? ORDER BY date_commande DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("article_id"),
                        rs.getString("numero"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getDouble("total")
                    );
                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
            throw e;
        }
        return commandes;
    }

    /**
     * Récupère une commande par son ID.
     *
     * @param id L'ID de la commande
     * @return La commande trouvée ou null si non trouvée
     * @throws SQLException Si une erreur de base de données se produit
     */
    public Commande findById(int id) throws SQLException {
        String query = "SELECT * FROM commande WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Commande(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("article_id"),
                        rs.getString("numero"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getDouble("total")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de la commande: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Récupère toutes les commandes de la base de données.
     *
     * @return Liste de toutes les commandes
     * @throws SQLException Si une erreur de base de données se produit
     */
    public List<Commande> findAll() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande ORDER BY date_commande DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("article_id"),
                        rs.getString("numero"),
                        rs.getTimestamp("date_commande").toLocalDateTime(),
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"),
                        rs.getDouble("total")
                    );
                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
            throw e;
        }
        return commandes;
    }

    /**
     * Supprime une commande de la base de données.
     *
     * @param commande La commande à supprimer
     * @throws SQLException Si une erreur de base de données se produit
     */
    public void delete(Commande commande) throws SQLException {
        String query = "DELETE FROM commande WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, commande.getId());
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La suppression de la commande a échoué, aucune ligne affectée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la commande: " + e.getMessage());
            throw e;
        }
    }

    // Vous pouvez ajouter d'autres méthodes ici si nécessaire (findAll, findById, etc.)
}
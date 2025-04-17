package Services;

import Interfaces.ICrud;
import Models.Event;
import Utils.MyDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventService implements ICrud<Event> {
    private Connection con;

    public EventService() {
        this.con = MyDb.getInstance().getConnection();
    }

    @Override
    public void insert(Event obj) throws SQLException {
        String sql = "INSERT INTO event(titre, description, lieu, nombreBillets, image, timestart, event_mission, donation_objective, seatprice, dateEvenement) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setString(1, obj.getTitre());
        pstmt.setString(2, obj.getDescription());
        pstmt.setString(3, obj.getLieu());
        pstmt.setInt(4, obj.getNombreBillets());
        pstmt.setString(5, obj.getImage());
        if (obj.getTimestart() != null) {
            pstmt.setTime(6, Time.valueOf(obj.getTimestart()));
        } else {
            pstmt.setNull(6, Types.TIME);
        }
        pstmt.setString(7, obj.getEvent_mission());
        pstmt.setDouble(8, obj.getDonation_objective());
        pstmt.setDouble(9, obj.getSeatprice());
        if (obj.getDateEvenement() != null) {
            pstmt.setTimestamp(10, Timestamp.valueOf(obj.getDateEvenement()));
        } else {
            pstmt.setNull(10, Types.TIMESTAMP);
        }
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void update(Event obj) throws SQLException {
        String sql = "UPDATE event SET titre=?, description=?, lieu=?, nombreBillets=?, image=?, timestart=?, event_mission=?, donation_objective=?, seatprice=?, dateEvenement=? WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setString(1, obj.getTitre());
        pstmt.setString(2, obj.getDescription());
        pstmt.setString(3, obj.getLieu());
        pstmt.setInt(4, obj.getNombreBillets());
        pstmt.setString(5, obj.getImage());
        if (obj.getTimestart() != null) {
            pstmt.setTime(6, Time.valueOf(obj.getTimestart()));
        } else {
            pstmt.setNull(6, Types.TIME);
        }
        pstmt.setString(7, obj.getEvent_mission());
        pstmt.setDouble(8, obj.getDonation_objective());
        pstmt.setDouble(9, obj.getSeatprice());
        if (obj.getDateEvenement() != null) {
            pstmt.setTimestamp(10, Timestamp.valueOf(obj.getDateEvenement()));
        } else {
            pstmt.setNull(10, Types.TIMESTAMP);
        }
        pstmt.setInt(11, obj.getIdevent());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void delete(Event obj) throws SQLException {
        // First delete related records from dependent tables before deleting the event
        cascadeDelete(obj.getIdevent());
        
        // Then delete the event itself
        String sql = "DELETE FROM event WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, obj.getIdevent());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Delete an event by its ID
     * 
     * @param id The ID of the event to delete
     * @throws SQLException If a database error occurs
     */
    public void delete(int id) throws SQLException {
        // First delete related records from dependent tables before deleting the event
        cascadeDelete(id);
        
        // Then delete the event itself
        String sql = "DELETE FROM event WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Performs a cascade delete for an event, removing all dependent records first
     * 
     * @param eventId ID of the event to cascade delete
     * @throws SQLException If a database error occurs
     */
    private void cascadeDelete(int eventId) throws SQLException {
        try {
            // Start a transaction to ensure all deletes are atomic
            con.setAutoCommit(false);
            
            // Delete records from model3_d table that reference this event
            String deleteModel3DSQL = "DELETE FROM model3_d WHERE event_id = ?";
            PreparedStatement model3dStmt = this.con.prepareStatement(deleteModel3DSQL);
            model3dStmt.setInt(1, eventId);
            model3dStmt.executeUpdate();
            model3dStmt.close();
            
            // Delete records from the reservation table related to this event
            String deleteReservationsSQL = "DELETE FROM reservation WHERE idevent = ?";
            PreparedStatement reservationStmt = this.con.prepareStatement(deleteReservationsSQL);
            reservationStmt.setInt(1, eventId);
            reservationStmt.executeUpdate();
            reservationStmt.close();
            
            // Delete records from the donation table related to this event
            String deleteDonationsSQL = "DELETE FROM donation WHERE idevent = ?";
            PreparedStatement donationStmt = this.con.prepareStatement(deleteDonationsSQL);
            donationStmt.setInt(1, eventId);
            donationStmt.executeUpdate();
            donationStmt.close();
            
            // If there are any other tables with foreign key relationships to event,
            // add delete statements for them here
            
            // Commit the transaction
            con.commit();
        } catch (SQLException e) {
            // If there's an error, roll back the transaction
            try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e; // Re-throw the exception
        } finally {
            // Restore auto-commit mode
            try {
                con.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public List<Event> findAll() throws SQLException {
        String sql = "SELECT * FROM event";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        List<Event> list = new ArrayList<>();
        while (rs.next()) {
            Event event = new Event();
            event.setIdevent(rs.getInt("idevent"));
            event.setTitre(rs.getString("titre"));
            event.setDescription(rs.getString("description"));
            event.setLieu(rs.getString("lieu"));
            event.setNombreBillets(rs.getInt("nombreBillets"));
            event.setImage(rs.getString("image"));
            if (rs.getTime("timestart") != null) {
                event.setTimestart(rs.getTime("timestart").toLocalTime());
            }
            event.setEvent_mission(rs.getString("event_mission"));
            event.setDonation_objective(rs.getDouble("donation_objective"));
            event.setSeatprice(rs.getDouble("seatprice"));
            if (rs.getTimestamp("dateEvenement") != null) {
                event.setDateEvenement(rs.getTimestamp("dateEvenement").toLocalDateTime());
            }
            list.add(event);
        }
        rs.close();
        pstmt.close();
        return list;
    }
    
    // Find an event by its ID
    public Event findById(int id) throws SQLException {
        String sql = "SELECT * FROM event WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            Event event = new Event();
            event.setIdevent(rs.getInt("idevent"));
            event.setTitre(rs.getString("titre"));
            event.setDescription(rs.getString("description"));
            event.setLieu(rs.getString("lieu"));
            event.setNombreBillets(rs.getInt("nombreBillets"));
            event.setImage(rs.getString("image"));
            if (rs.getTime("timestart") != null) {
                event.setTimestart(rs.getTime("timestart").toLocalTime());
            }
            event.setEvent_mission(rs.getString("event_mission"));
            event.setDonation_objective(rs.getDouble("donation_objective"));
            event.setSeatprice(rs.getDouble("seatprice"));
            if (rs.getTimestamp("dateEvenement") != null) {
                event.setDateEvenement(rs.getTimestamp("dateEvenement").toLocalDateTime());
            }
            rs.close();
            pstmt.close();
            return event;
        }
        
        rs.close();
        pstmt.close();
        return null;
    }

    /**
     * Count the total number of events in the database
     * @return Total number of events
     * @throws SQLException If a database error occurs
     */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM event";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
        
        return 0;
    }
    
    /**
     * Count the number of upcoming events (events with a date in the future)
     * @return Number of upcoming events
     * @throws SQLException If a database error occurs
     */
    public int countUpcoming() throws SQLException {
        String sql = "SELECT COUNT(*) FROM event WHERE dateEvenement > ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
        
        return 0;
    }

    /**
     * Find all upcoming events (events with a date in the future)
     * @return List of upcoming events
     * @throws SQLException If a database error occurs
     */
    public List<Event> findUpcomingEvents() throws SQLException {
        String sql = "SELECT * FROM event WHERE dateEvenement > NOW() ORDER BY dateEvenement ASC";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        
        List<Event> events = new ArrayList<>();
        while (rs.next()) {
            Event event = new Event();
            event.setIdevent(rs.getInt("idevent"));
            event.setTitre(rs.getString("titre"));
            event.setDescription(rs.getString("description"));
            event.setLieu(rs.getString("lieu"));
            event.setNombreBillets(rs.getInt("nombreBillets"));
            event.setImage(rs.getString("image"));
            if (rs.getTime("timestart") != null) {
                event.setTimestart(rs.getTime("timestart").toLocalTime());
            }
            event.setEvent_mission(rs.getString("event_mission"));
            event.setDonation_objective(rs.getDouble("donation_objective"));
            event.setSeatprice(rs.getDouble("seatprice"));
            if (rs.getTimestamp("dateEvenement") != null) {
                event.setDateEvenement(rs.getTimestamp("dateEvenement").toLocalDateTime());
            }
            events.add(event);
        }
        
        rs.close();
        pstmt.close();
        return events;
    }

    /**
     * Count the number of upcoming events (events with a date in the future)
     * @return Number of upcoming events
     * @throws SQLException If a database error occurs
     */
    public int countUpcomingEvents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM event WHERE dateEvenement > NOW()";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
        
        return 0;
    }
} 
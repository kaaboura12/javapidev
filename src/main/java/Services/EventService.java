package Services;

import Models.Event;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements ICrud<Event> {
    private Connection con;

    public EventService() {
        this.con = MyDb.getInstance().getConnection();
    }

    @Override
    public void insert(Event obj) throws SQLException {
        String sql = "INSERT INTO event(titre, description, lieu, nombreBillets, image, timestart, event_mission, donation_objective, seatprice, dateEvenement) VALUES('"
                + obj.getTitre() + "','"
                + obj.getDescription() + "','"
                + obj.getLieu() + "','"
                + obj.getNombreBillets() + "','"
                + obj.getImage() + "','"
                + obj.getTimestart() + "','"
                + obj.getEvent_mission() + "','"
                + obj.getDonation_objective() + "','"
                + obj.getSeatprice() + "','"
                + obj.getDateEvenement() + "')";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void update(Event obj) throws SQLException {
        String sql = "UPDATE event SET titre='" + obj.getTitre()
                + "', description='" + obj.getDescription()
                + "', lieu='" + obj.getLieu()
                + "', nombreBillets='" + obj.getNombreBillets()
                + "', image='" + obj.getImage()
                + "', timestart='" + obj.getTimestart()
                + "', event_mission='" + obj.getEvent_mission()
                + "', donation_objective='" + obj.getDonation_objective()
                + "', seatprice='" + obj.getSeatprice()
                + "', dateEvenement='" + obj.getDateEvenement()
                + "' WHERE idevent = '" + obj.getIdevent() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void delete(Event obj) throws SQLException {
        String sql = "DELETE FROM event WHERE idevent = '" + obj.getIdevent() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public List<Event> findAll() throws SQLException {
        String sql = "SELECT * FROM event";
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
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

        return list;
    }
    
    // Find an event by its ID
    public Event findById(int id) throws SQLException {
        String sql = "SELECT * FROM event WHERE idevent = " + id;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
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
            return event;
        }
        
        return null;
    }
} 
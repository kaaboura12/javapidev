package Services;

import Models.Reservation;
import Models.Event;
import Models.User;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements ICrud<Reservation> {
    private Connection con;
    private UserService userService;
    private EventService eventService;

    public ReservationService() {
        this.con = MyDb.getInstance().getConnection();
        this.userService = new UserService();
        this.eventService = new EventService();
    }

    @Override
    public void insert(Reservation obj) throws SQLException {
        String sql = "INSERT INTO reservation(userid, idevent, reservation_date, seats_reserved, total_amount) VALUES("
                + (obj.getUserid() != null ? ("'" + obj.getUserid() + "'") : "NULL") + ","
                + (obj.getIdevent() != null ? ("'" + obj.getIdevent() + "'") : "NULL") + ",'"
                + obj.getReservation_date() + "','"
                + obj.getSeats_reserved() + "','"
                + obj.getTotal_amount() + "')";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void update(Reservation obj) throws SQLException {
        String sql = "UPDATE reservation SET userid=" + (obj.getUserid() != null ? ("'" + obj.getUserid() + "'") : "NULL")
                + ", idevent=" + (obj.getIdevent() != null ? ("'" + obj.getIdevent() + "'") : "NULL")
                + ", reservation_date='" + obj.getReservation_date()
                + "', seats_reserved='" + obj.getSeats_reserved()
                + "', total_amount='" + obj.getTotal_amount()
                + "' WHERE id_reservation = '" + obj.getId_reservation() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void delete(Reservation obj) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_reservation = '" + obj.getId_reservation() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservation";
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<Reservation> list = new ArrayList<>();
        while (rs.next()) {
            Reservation reservation = new Reservation();
            reservation.setId_reservation(rs.getInt("id_reservation"));
            
            // Handle nullable userid
            if (rs.getObject("userid") != null) {
                reservation.setUserid(rs.getInt("userid"));
                
                // Load associated User
                User user = userService.findById(rs.getInt("userid"));
                if (user != null) {
                    reservation.setUser(user);
                }
            }
            
            // Handle nullable idevent
            if (rs.getObject("idevent") != null) {
                reservation.setIdevent(rs.getInt("idevent"));
                
                // Load associated Event
                Event event = eventService.findById(rs.getInt("idevent"));
                if (event != null) {
                    reservation.setEvent(event);
                }
            }
            
            if (rs.getDate("reservation_date") != null) {
                reservation.setReservation_date(rs.getDate("reservation_date").toLocalDate());
            }
            reservation.setSeats_reserved(rs.getInt("seats_reserved"));
            reservation.setTotal_amount(rs.getDouble("total_amount"));
            
            list.add(reservation);
        }

        return list;
    }
    
    // Find a reservation by its ID
    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id_reservation = " + id;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        if (rs.next()) {
            Reservation reservation = new Reservation();
            reservation.setId_reservation(rs.getInt("id_reservation"));
            
            // Handle nullable userid
            if (rs.getObject("userid") != null) {
                reservation.setUserid(rs.getInt("userid"));
                
                // Load associated User
                User user = userService.findById(rs.getInt("userid"));
                if (user != null) {
                    reservation.setUser(user);
                }
            }
            
            // Handle nullable idevent
            if (rs.getObject("idevent") != null) {
                reservation.setIdevent(rs.getInt("idevent"));
                
                // Load associated Event
                Event event = eventService.findById(rs.getInt("idevent"));
                if (event != null) {
                    reservation.setEvent(event);
                }
            }
            
            if (rs.getDate("reservation_date") != null) {
                reservation.setReservation_date(rs.getDate("reservation_date").toLocalDate());
            }
            reservation.setSeats_reserved(rs.getInt("seats_reserved"));
            reservation.setTotal_amount(rs.getDouble("total_amount"));
            
            return reservation;
        }
        
        return null;
    }
    
    // Find all reservations by user ID
    public List<Reservation> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE userid = " + userId;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<Reservation> list = new ArrayList<>();
        
        while (rs.next()) {
            Reservation reservation = new Reservation();
            reservation.setId_reservation(rs.getInt("id_reservation"));
            reservation.setUserid(rs.getInt("userid"));
            
            // Handle nullable idevent
            if (rs.getObject("idevent") != null) {
                reservation.setIdevent(rs.getInt("idevent"));
            }
            
            if (rs.getDate("reservation_date") != null) {
                reservation.setReservation_date(rs.getDate("reservation_date").toLocalDate());
            }
            reservation.setSeats_reserved(rs.getInt("seats_reserved"));
            reservation.setTotal_amount(rs.getDouble("total_amount"));
            
            list.add(reservation);
        }
        
        return list;
    }
    
    // Find all reservations by event ID
    public List<Reservation> findByEventId(int eventId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE idevent = " + eventId;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<Reservation> list = new ArrayList<>();
        
        while (rs.next()) {
            Reservation reservation = new Reservation();
            reservation.setId_reservation(rs.getInt("id_reservation"));
            
            // Handle nullable userid
            if (rs.getObject("userid") != null) {
                reservation.setUserid(rs.getInt("userid"));
            }
            
            reservation.setIdevent(rs.getInt("idevent"));
            
            if (rs.getDate("reservation_date") != null) {
                reservation.setReservation_date(rs.getDate("reservation_date").toLocalDate());
            }
            reservation.setSeats_reserved(rs.getInt("seats_reserved"));
            reservation.setTotal_amount(rs.getDouble("total_amount"));
            
            list.add(reservation);
        }
        
        return list;
    }
    
    // Calculate total reserved seats for an event
    public int getTotalReservedSeatsForEvent(int eventId) throws SQLException {
        String sql = "SELECT SUM(seats_reserved) as total FROM reservation WHERE idevent = " + eventId;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        if (rs.next()) {
            return rs.getInt("total");
        }
        
        return 0;
    }
} 
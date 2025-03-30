package Services;

import Interfaces.ICrud;
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
        String sql = "INSERT INTO reservation(userid, idevent, reservation_date, seats_reserved, total_amount) VALUES(?, ?, ?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        
        if (obj.getUserid() != null) {
            pstmt.setInt(1, obj.getUserid());
        } else {
            pstmt.setNull(1, Types.INTEGER);
        }
        
        if (obj.getIdevent() != null) {
            pstmt.setInt(2, obj.getIdevent());
        } else {
            pstmt.setNull(2, Types.INTEGER);
        }
        
        if (obj.getReservation_date() != null) {
            pstmt.setDate(3, Date.valueOf(obj.getReservation_date()));
        } else {
            pstmt.setNull(3, Types.DATE);
        }
        
        pstmt.setInt(4, obj.getSeats_reserved());
        pstmt.setDouble(5, obj.getTotal_amount());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void update(Reservation obj) throws SQLException {
        String sql = "UPDATE reservation SET userid=?, idevent=?, reservation_date=?, seats_reserved=?, total_amount=? WHERE id_reservation = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        
        if (obj.getUserid() != null) {
            pstmt.setInt(1, obj.getUserid());
        } else {
            pstmt.setNull(1, Types.INTEGER);
        }
        
        if (obj.getIdevent() != null) {
            pstmt.setInt(2, obj.getIdevent());
        } else {
            pstmt.setNull(2, Types.INTEGER);
        }
        
        if (obj.getReservation_date() != null) {
            pstmt.setDate(3, Date.valueOf(obj.getReservation_date()));
        } else {
            pstmt.setNull(3, Types.DATE);
        }
        
        pstmt.setInt(4, obj.getSeats_reserved());
        pstmt.setDouble(5, obj.getTotal_amount());
        pstmt.setInt(6, obj.getId_reservation());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void delete(Reservation obj) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id_reservation = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, obj.getId_reservation());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservation";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
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
        rs.close();
        pstmt.close();
        return list;
    }
    
    // Find a reservation by its ID
    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE id_reservation = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
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
            
            rs.close();
            pstmt.close();
            return reservation;
        }
        
        rs.close();
        pstmt.close();
        return null;
    }
    
    // Find all reservations by user ID
    public List<Reservation> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE userid = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();
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
        
        rs.close();
        pstmt.close();
        return list;
    }
    
    // Find all reservations by event ID
    public List<Reservation> findByEventId(int eventId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, eventId);
        ResultSet rs = pstmt.executeQuery();
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
        
        rs.close();
        pstmt.close();
        return list;
    }
    
    // Calculate total reserved seats for an event
    public int getTotalReservedSeatsForEvent(int eventId) throws SQLException {
        String sql = "SELECT SUM(seats_reserved) as total FROM reservation WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, eventId);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            int total = rs.getInt("total");
            rs.close();
            pstmt.close();
            return total;
        }
        
        rs.close();
        pstmt.close();
        return 0;
    }
} 
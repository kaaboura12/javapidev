package Services;

import Interfaces.ICrud;
import Models.Donation;
import Models.Event;
import Models.User;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationService implements ICrud<Donation> {
    private Connection con;
    private UserService userService;
    private EventService eventService;

    public DonationService() {
        this.con = MyDb.getInstance().getConnection();
        this.userService = new UserService();
        this.eventService = new EventService();
    }

    @Override
    public void insert(Donation obj) throws SQLException {
        String sql = "INSERT INTO donation(idevent, userid, donorname, email, montant, date, payment_method, num_tlf) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, obj.getIdevent());
        if (obj.getUserid() != null) {
            pstmt.setInt(2, obj.getUserid());
        } else {
            pstmt.setNull(2, Types.INTEGER);
        }
        pstmt.setString(3, obj.getDonorname());
        pstmt.setString(4, obj.getEmail());
        pstmt.setDouble(5, obj.getMontant());
        if (obj.getDate() != null) {
            pstmt.setDate(6, Date.valueOf(obj.getDate()));
        } else {
            pstmt.setNull(6, Types.DATE);
        }
        pstmt.setString(7, obj.getPayment_method());
        pstmt.setString(8, obj.getNum_tlf());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void update(Donation obj) throws SQLException {
        String sql = "UPDATE donation SET idevent=?, userid=?, donorname=?, email=?, montant=?, date=?, payment_method=?, num_tlf=? WHERE iddon = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, obj.getIdevent());
        if (obj.getUserid() != null) {
            pstmt.setInt(2, obj.getUserid());
        } else {
            pstmt.setNull(2, Types.INTEGER);
        }
        pstmt.setString(3, obj.getDonorname());
        pstmt.setString(4, obj.getEmail());
        pstmt.setDouble(5, obj.getMontant());
        if (obj.getDate() != null) {
            pstmt.setDate(6, Date.valueOf(obj.getDate()));
        } else {
            pstmt.setNull(6, Types.DATE);
        }
        pstmt.setString(7, obj.getPayment_method());
        pstmt.setString(8, obj.getNum_tlf());
        pstmt.setInt(9, obj.getIddon());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void delete(Donation obj) throws SQLException {
        String sql = "DELETE FROM donation WHERE iddon = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, obj.getIddon());
        pstmt.executeUpdate();
        pstmt.close();
    }
    
    /**
     * Delete a donation by its ID
     * 
     * @param id The ID of the donation to delete
     * @throws SQLException If a database error occurs
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM donation WHERE iddon = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public List<Donation> findAll() throws SQLException {
        String sql = "SELECT * FROM donation";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        List<Donation> list = new ArrayList<>();
        while (rs.next()) {
            Donation donation = new Donation();
            donation.setIddon(rs.getInt("iddon"));
            donation.setIdevent(rs.getInt("idevent"));
            
            // Handle nullable userid
            if (rs.getObject("userid") != null) {
                donation.setUserid(rs.getInt("userid"));
                
                // Load associated User
                User user = userService.findById(rs.getInt("userid"));
                if (user != null) {
                    donation.setUser(user);
                }
            }
            
            donation.setDonorname(rs.getString("donorname"));
            donation.setEmail(rs.getString("email"));
            donation.setMontant(rs.getDouble("montant"));
            if (rs.getDate("date") != null) {
                donation.setDate(rs.getDate("date").toLocalDate());
            }
            donation.setPayment_method(rs.getString("payment_method"));
            donation.setNum_tlf(rs.getString("num_tlf"));
            
            // Load associated Event
            Event event = eventService.findById(rs.getInt("idevent"));
            if (event != null) {
                donation.setEvent(event);
            }
            
            list.add(donation);
        }
        rs.close();
        pstmt.close();
        return list;
    }
    
    // Find a donation by its ID
    public Donation findById(int id) throws SQLException {
        String sql = "SELECT * FROM donation WHERE iddon = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            Donation donation = new Donation();
            donation.setIddon(rs.getInt("iddon"));
            donation.setIdevent(rs.getInt("idevent"));
            
            // Handle nullable userid
            if (rs.getObject("userid") != null) {
                donation.setUserid(rs.getInt("userid"));
                
                // Load associated User
                User user = userService.findById(rs.getInt("userid"));
                if (user != null) {
                    donation.setUser(user);
                }
            }
            
            donation.setDonorname(rs.getString("donorname"));
            donation.setEmail(rs.getString("email"));
            donation.setMontant(rs.getDouble("montant"));
            if (rs.getDate("date") != null) {
                donation.setDate(rs.getDate("date").toLocalDate());
            }
            donation.setPayment_method(rs.getString("payment_method"));
            donation.setNum_tlf(rs.getString("num_tlf"));
            
            // Load associated Event
            Event event = eventService.findById(rs.getInt("idevent"));
            if (event != null) {
                donation.setEvent(event);
            }
            
            rs.close();
            pstmt.close();
            return donation;
        }
        
        rs.close();
        pstmt.close();
        return null;
    }
    
    // Find all donations by event ID
    public List<Donation> findByEventId(int eventId) throws SQLException {
        String sql = "SELECT * FROM donation WHERE idevent = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, eventId);
        ResultSet rs = pstmt.executeQuery();
        List<Donation> list = new ArrayList<>();
        
        while (rs.next()) {
            Donation donation = new Donation();
            donation.setIddon(rs.getInt("iddon"));
            donation.setIdevent(rs.getInt("idevent"));
            
            // Handle nullable userid
            if (rs.getObject("userid") != null) {
                donation.setUserid(rs.getInt("userid"));
            }
            
            donation.setDonorname(rs.getString("donorname"));
            donation.setEmail(rs.getString("email"));
            donation.setMontant(rs.getDouble("montant"));
            if (rs.getDate("date") != null) {
                donation.setDate(rs.getDate("date").toLocalDate());
            }
            donation.setPayment_method(rs.getString("payment_method"));
            donation.setNum_tlf(rs.getString("num_tlf"));
            
            list.add(donation);
        }
        
        rs.close();
        pstmt.close();
        return list;
    }
    
    // Find all donations by user ID
    public List<Donation> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM donation WHERE userid = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();
        List<Donation> list = new ArrayList<>();
        
        while (rs.next()) {
            Donation donation = new Donation();
            donation.setIddon(rs.getInt("iddon"));
            donation.setIdevent(rs.getInt("idevent"));
            donation.setUserid(rs.getInt("userid"));
            donation.setDonorname(rs.getString("donorname"));
            donation.setEmail(rs.getString("email"));
            donation.setMontant(rs.getDouble("montant"));
            if (rs.getDate("date") != null) {
                donation.setDate(rs.getDate("date").toLocalDate());
            }
            donation.setPayment_method(rs.getString("payment_method"));
            donation.setNum_tlf(rs.getString("num_tlf"));
            
            list.add(donation);
        }
        
        rs.close();
        pstmt.close();
        return list;
    }
} 
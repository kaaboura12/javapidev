package Services;

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
        String sql = "INSERT INTO donation(idevent, userid, donorname, email, montant, date, payment_method, num_tlf) VALUES('"
                + obj.getIdevent() + "','"
                + (obj.getUserid() != null ? obj.getUserid() : "NULL") + "','"
                + obj.getDonorname() + "','"
                + obj.getEmail() + "','"
                + obj.getMontant() + "','"
                + obj.getDate() + "','"
                + obj.getPayment_method() + "','"
                + obj.getNum_tlf() + "')";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void update(Donation obj) throws SQLException {
        String sql = "UPDATE donation SET idevent='" + obj.getIdevent()
                + "', userid=" + (obj.getUserid() != null ? ("'" + obj.getUserid() + "'") : "NULL")
                + ", donorname='" + obj.getDonorname()
                + "', email='" + obj.getEmail()
                + "', montant='" + obj.getMontant()
                + "', date='" + obj.getDate()
                + "', payment_method='" + obj.getPayment_method()
                + "', num_tlf='" + obj.getNum_tlf()
                + "' WHERE iddon = '" + obj.getIddon() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void delete(Donation obj) throws SQLException {
        String sql = "DELETE FROM donation WHERE iddon = '" + obj.getIddon() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public List<Donation> findAll() throws SQLException {
        String sql = "SELECT * FROM donation";
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
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

        return list;
    }
    
    // Find a donation by its ID
    public Donation findById(int id) throws SQLException {
        String sql = "SELECT * FROM donation WHERE iddon = " + id;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
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
            
            return donation;
        }
        
        return null;
    }
    
    // Find all donations by event ID
    public List<Donation> findByEventId(int eventId) throws SQLException {
        String sql = "SELECT * FROM donation WHERE idevent = " + eventId;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
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
        
        return list;
    }
    
    // Find all donations by user ID
    public List<Donation> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM donation WHERE userid = " + userId;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
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
        
        return list;
    }
} 
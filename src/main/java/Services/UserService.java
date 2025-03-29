package Services;

import Models.User;
import Utils.MyDb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ICrud<User> {
    private Connection con;

    public UserService() {
        this.con = MyDb.getInstance().getConnection();
    }

    @Override
    public void insert(User obj) throws SQLException {
        String sql = "INSERT INTO user(nom,prenom,email,password,numtlf,age,avatar_url,roles,is_verified) VALUES('" 
                    + obj.getNom() + "','" 
                    + obj.getPrenom() + "','" 
                    + obj.getEmail() + "','" 
                    + obj.getPassword() + "','" 
                    + obj.getNumtlf() + "','" 
                    + obj.getAge() + "','" 
                    + obj.getAvatar_url() + "','" 
                    + obj.getRoles() + "','" 
                    + obj.isIs_verified() + "')";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void update(User obj) throws SQLException {
        String sql = "UPDATE user SET nom='" + obj.getNom() 
                    + "', prenom='" + obj.getPrenom() 
                    + "', email='" + obj.getEmail() 
                    + "', password='" + obj.getPassword() 
                    + "', numtlf='" + obj.getNumtlf() 
                    + "', age='" + obj.getAge() 
                    + "', avatar_url='" + obj.getAvatar_url() 
                    + "', roles='" + obj.getRoles() 
                    + "', is_verified='" + obj.isIs_verified() 
                    + "' WHERE user_id = '" + obj.getUser_id() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public void delete(User obj) throws SQLException {
        String sql = "DELETE FROM user WHERE user_id = '" + obj.getUser_id() + "'";
        Statement stmt = this.con.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM user";
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        List<User> list = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setUser_id(rs.getInt("user_id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setNumtlf(rs.getString("numtlf"));
            user.setAge(rs.getInt("age"));
            user.setAvatar_url(rs.getString("avatar_url"));
            user.setRoles(rs.getString("roles"));
            user.setIs_verified(rs.getBoolean("is_verified"));
            if (rs.getTimestamp("reset_token_expires_at") != null) {
                user.setReset_token_expires_at(rs.getTimestamp("reset_token_expires_at").toLocalDateTime());
            }
            user.setReset_token(rs.getString("reset_token"));
            if (rs.getTimestamp("last_login_at") != null) {
                user.setLast_login_at(rs.getTimestamp("last_login_at").toLocalDateTime());
            }
            list.add(user);
        }

        return list;
    }

    // Find a user by ID
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE user_id = " + id;
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        if (rs.next()) {
            User user = new User();
            user.setUser_id(rs.getInt("user_id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setNumtlf(rs.getString("numtlf"));
            user.setAge(rs.getInt("age"));
            user.setAvatar_url(rs.getString("avatar_url"));
            user.setRoles(rs.getString("roles"));
            user.setIs_verified(rs.getBoolean("is_verified"));
            if (rs.getTimestamp("reset_token_expires_at") != null) {
                user.setReset_token_expires_at(rs.getTimestamp("reset_token_expires_at").toLocalDateTime());
            }
            user.setReset_token(rs.getString("reset_token"));
            if (rs.getTimestamp("last_login_at") != null) {
                user.setLast_login_at(rs.getTimestamp("last_login_at").toLocalDateTime());
            }
            return user;
        }
        
        return null;
    }
}

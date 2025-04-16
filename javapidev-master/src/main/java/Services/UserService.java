package Services;

import Interfaces.ICrud;
import Models.User;
import Utils.MyDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ICrud<User> {
    private Connection con;

    public UserService() {
        this.con = MyDb.getInstance().getConnection();
    }

    @Override
    public void insert(User obj) throws SQLException {
        if (con == null || con.isClosed()) {
            throw new SQLException("Database connection is not available");
        }

        try {
            String sql = "INSERT INTO user(nom,prenom,email,password,numtlf,age,avatar_url,roles,is_verified) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = this.con.prepareStatement(sql);
            pstmt.setString(1, obj.getNom());
            pstmt.setString(2, obj.getPrenom());
            pstmt.setString(3, obj.getEmail());
            pstmt.setString(4, obj.getPassword());
            pstmt.setString(5, obj.getNumtlf());
            pstmt.setInt(6, obj.getAge());
            pstmt.setString(7, obj.getAvatar_url());
            pstmt.setString(8, obj.getRoles());
            pstmt.setBoolean(9, obj.isIs_verified());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error during user insertion: " + e.getMessage());
            throw new SQLException("Failed to insert user: " + e.getMessage());
        }
    }

    @Override
    public void update(User obj) throws SQLException {
        String sql = "UPDATE user SET nom=?, prenom=?, email=?, password=?, numtlf=?, age=?, avatar_url=?, roles=?, is_verified=? WHERE user_id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setString(1, obj.getNom());
        pstmt.setString(2, obj.getPrenom());
        pstmt.setString(3, obj.getEmail());
        pstmt.setString(4, obj.getPassword());
        pstmt.setString(5, obj.getNumtlf());
        pstmt.setInt(6, obj.getAge());
        pstmt.setString(7, obj.getAvatar_url());
        pstmt.setString(8, obj.getRoles());
        pstmt.setBoolean(9, obj.isIs_verified());
        pstmt.setInt(10, obj.getUser_id());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public void delete(User obj) throws SQLException {
        String sql = "DELETE FROM user WHERE user_id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, obj.getUser_id());
        pstmt.executeUpdate();
        pstmt.close();
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM user";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
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
        rs.close();
        pstmt.close();
        return list;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, id);
        ResultSet rs = pstmt.executeQuery();

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
            rs.close();
            pstmt.close();
            return user;
        }

        rs.close();
        pstmt.close();
        return null;
    }

    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setString(1, email);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();

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

            // Update last login time
            updateLastLogin(user.getUser_id());

            rs.close();
            pstmt.close();
            return user;
        }

        rs.close();
        pstmt.close();
        return null;
    }

    private void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE user SET last_login_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(sql);
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        pstmt.close();
    }
}

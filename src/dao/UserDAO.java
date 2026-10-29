package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;
import utility.DBConnection;

/**
 * Data Access Object for Users table.
 */
public class UserDAO {

    /**
     * Retrieves a user by their database ID.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Retrieves a user by their username.
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Retrieves a user by their email address.
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Inserts a new user into the database.
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO Users (username, email, password, role, status, storage_limit) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole() != null ? user.getRole() : "USER");
            stmt.setString(5, user.getStatus() != null ? user.getStatus() : "ACTIVE");
            stmt.setLong(6, user.getStorageLimit() > 0 ? user.getStorageLimit() : 104857600); // 100MB default
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Updates an existing user's details.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET username = ?, email = ?, role = ?, status = ?, storage_limit = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getStatus());
            stmt.setLong(5, user.getStorageLimit());
            stmt.setInt(6, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Updates a user's password.
     */
    public boolean updatePassword(String email, String newPasswordHash) {
        String sql = "UPDATE Users SET password = ? WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Deletes a user by their database ID.
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM Users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Retrieves all users (for Admin Dashboard).
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY id DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Calculates storage space currently used by a user (in bytes).
     */
    public long getUsedStorage(int userId) {
        String sql = "SELECT SUM(file_size) FROM Documents WHERE owner_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return 0;
    }

    // OTP PASSWORD RESET FLOW METHODS

    /**
     * Saves an OTP password reset record.
     */
    public boolean saveOTP(String email, String code, Timestamp expiry) {
        // Clear previous OTPs first
        deleteOTP(email);
        String sql = "INSERT INTO Password_Reset (email, otp_code, expiry_time) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, code);
            stmt.setTimestamp(3, expiry);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Verifies if an OTP code is correct and not expired.
     */
    public boolean verifyOTP(String email, String code) {
        String sql = "SELECT * FROM Password_Reset WHERE email = ? AND otp_code = ? AND expiry_time > NOW()";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, code);
            rs = stmt.executeQuery();
            return rs.next(); // True if record exists (OTP matches and is valid)
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return false;
    }

    /**
     * Deletes a password reset request.
     */
    public boolean deleteOTP(String email) {
        String sql = "DELETE FROM Password_Reset WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getLong("storage_limit"),
            rs.getTimestamp("created_at")
        );
    }
}

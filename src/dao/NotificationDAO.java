package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Notification;
import utility.DBConnection;

/**
 * Data Access Object for Notifications table.
 */
public class NotificationDAO {

    /**
     * Creates a notification for a user.
     */
    public boolean createNotification(Notification notif) {
        String sql = "INSERT INTO Notifications (user_id, message, is_read) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, notif.getUserId());
            stmt.setString(2, notif.getMessage());
            stmt.setBoolean(3, notif.isRead());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Gets all notifications for a specific user (unread first, then ordered by date).
     */
    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE user_id = ? ORDER BY is_read ASC, created_at DESC LIMIT 50";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Notification(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("message"),
                    rs.getBoolean("is_read"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Marks a specific notification as read.
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE Notifications SET is_read = TRUE WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Marks all notifications as read for a specific user.
     */
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE Notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }
}

package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.AuditLog;
import utility.DBConnection;

/**
 * Data Access Object for Audit_Log table.
 */
public class AuditLogDAO {

    /**
     * Creates a new audit log entry in the database.
     */
    public boolean createLog(AuditLog log) {
        String sql = "INSERT INTO Audit_Log (user_id, action, details, ip_address) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            if (log.getUserId() > 0) {
                stmt.setInt(1, log.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getDetails());
            stmt.setString(4, log.getIpAddress());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Retrieves all audit logs (for Admin panel) with pagination support.
     */
    public List<AuditLog> getAllLogs(int limit, int offset) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT al.*, u.username FROM Audit_Log al " +
                     "LEFT JOIN Users u ON al.user_id = u.id " +
                     "ORDER BY al.timestamp DESC LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            rs = stmt.executeQuery();
            while (rs.next()) {
                AuditLog log = new AuditLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getString("ip_address"),
                    rs.getTimestamp("timestamp")
                );
                log.setUsername(rs.getString("username") != null ? rs.getString("username") : "System/Guest");
                list.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Gets count of total logs for admin pagination calculations.
     */
    public int getTotalLogCount() {
        String sql = "SELECT COUNT(*) FROM Audit_Log";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return 0;
    }
}

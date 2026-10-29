package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Document;
import utility.DBConnection;

/**
 * Data Access Object for Documents, Shared_Documents, and favorites.
 */
public class DocumentDAO {

    /**
     * Retrieves a single document by ID.
     */
    public Document getDocumentById(int id) {
        String sql = "SELECT d.*, c.name as category_name, u.username as owner_username " +
                     "FROM Documents d " +
                     "LEFT JOIN Categories c ON d.category_id = c.id " +
                     "LEFT JOIN Users u ON d.owner_id = u.id " +
                     "WHERE d.id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return extractDocumentFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Creates a new Document entry.
     */
    public boolean createDocument(Document doc) {
        String sql = "INSERT INTO Documents (name, file_path, file_size, file_extension, category_id, owner_id, ocr_text, is_favorite) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, doc.getName());
            stmt.setString(2, doc.getFilePath());
            stmt.setLong(3, doc.getFileSize());
            stmt.setString(4, doc.getFileExtension());
            if (doc.getCategoryId() > 0) {
                stmt.setInt(5, doc.getCategoryId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, doc.getOwnerId());
            stmt.setString(7, doc.getOcrText());
            stmt.setBoolean(8, doc.isFavorite());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Updates document metadata (e.g. renaming or moving categories).
     */
    public boolean updateDocument(Document doc) {
        String sql = "UPDATE Documents SET name = ?, category_id = ?, is_favorite = ?, ocr_text = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, doc.getName());
            if (doc.getCategoryId() > 0) {
                stmt.setInt(2, doc.getCategoryId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setBoolean(3, doc.isFavorite());
            stmt.setString(4, doc.getOcrText());
            stmt.setInt(5, doc.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Deletes a document record.
     */
    public boolean deleteDocument(int id) {
        String sql = "DELETE FROM Documents WHERE id = ?";
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
     * Gets all system documents (for Admin dashboard).
     */
    public List<Document> getAllDocuments() {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as category_name, u.username as owner_username " +
                     "FROM Documents d " +
                     "LEFT JOIN Categories c ON d.category_id = c.id " +
                     "LEFT JOIN Users u ON d.owner_id = u.id " +
                     "ORDER BY d.upload_date DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(extractDocumentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves all documents belonging to a specific owner.
     */
    public List<Document> getDocumentsByOwner(int ownerId) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as category_name, u.username as owner_username " +
                     "FROM Documents d " +
                     "LEFT JOIN Categories c ON d.category_id = c.id " +
                     "LEFT JOIN Users u ON d.owner_id = u.id " +
                     "WHERE d.owner_id = ? " +
                     "ORDER BY d.upload_date DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ownerId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractDocumentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves recent uploads for a user dashboard.
     */
    public List<Document> getRecentUploads(int ownerId, int limit) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as category_name, u.username as owner_username " +
                     "FROM Documents d " +
                     "LEFT JOIN Categories c ON d.category_id = c.id " +
                     "LEFT JOIN Users u ON d.owner_id = u.id " +
                     "WHERE d.owner_id = ? " +
                     "ORDER BY d.upload_date DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ownerId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractDocumentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Retrieves favorite documents for a user.
     */
    public List<Document> getFavoriteDocuments(int ownerId) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as category_name, u.username as owner_username " +
                     "FROM Documents d " +
                     "LEFT JOIN Categories c ON d.category_id = c.id " +
                     "LEFT JOIN Users u ON d.owner_id = u.id " +
                     "WHERE d.owner_id = ? AND d.is_favorite = TRUE " +
                     "ORDER BY d.upload_date DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, ownerId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractDocumentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Toggles a document's favorite status.
     */
    public boolean setFavorite(int docId, boolean fav) {
        String sql = "UPDATE Documents SET is_favorite = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, fav);
            stmt.setInt(2, docId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    // DOCUMENT SHARING LOGIC

    /**
     * Shares a document with another user.
     */
    public boolean shareDocument(int docId, int sharedBy, int sharedWith) {
        String sql = "INSERT INTO Shared_Documents (document_id, shared_by_user_id, shared_with_user_id) VALUES (?, ?, ?)" +
                     " ON DUPLICATE KEY UPDATE shared_at = CURRENT_TIMESTAMP";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, docId);
            stmt.setInt(2, sharedBy);
            stmt.setInt(3, sharedWith);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Retrieves all documents shared with a user.
     */
    public List<Document> getSharedDocuments(int userId) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT d.*, c.name as category_name, u.username as owner_username " +
                     "FROM Shared_Documents sd " +
                     "JOIN Documents d ON sd.document_id = d.id " +
                     "LEFT JOIN Categories c ON d.category_id = c.id " +
                     "LEFT JOIN Users u ON d.owner_id = u.id " +
                     "WHERE sd.shared_with_user_id = ? " +
                     "ORDER BY sd.shared_at DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractDocumentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    /**
     * Checks if a user has access to a shared document.
     */
    public boolean isDocumentSharedWithUser(int docId, int userId) {
        String sql = "SELECT 1 FROM Shared_Documents WHERE document_id = ? AND shared_with_user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, docId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return false;
    }

    /**
     * Revokes sharing a document with a user.
     */
    public boolean revokeShare(int docId, int sharedWith) {
        String sql = "DELETE FROM Shared_Documents WHERE document_id = ? AND shared_with_user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, docId);
            stmt.setInt(2, sharedWith);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(stmt, conn);
        }
        return false;
    }

    // SEARCH & FILTER UTILITY

    /**
     * Performs a comprehensive search based on multiple criteria.
     * Prevents SQL Injection through carefully structured PreparedStatements.
     */
    public List<Document> searchDocuments(String searchTerm, String categoryIdStr, String dateStr, String ownerUsername, int userId, String userRole) {
        List<Document> list = new ArrayList<>();
        
        // Base Query
        StringBuilder sql = new StringBuilder(
            "SELECT d.*, c.name as category_name, u.username as owner_username " +
            "FROM Documents d " +
            "LEFT JOIN Categories c ON d.category_id = c.id " +
            "LEFT JOIN Users u ON d.owner_id = u.id " +
            "WHERE 1=1 "
        );

        // Role-based restrictions: Users can only see their own files OR files shared with them
        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            sql.append("AND (d.owner_id = ? OR d.id IN (SELECT document_id FROM Shared_Documents WHERE shared_with_user_id = ?)) ");
        }

        // Apply filters dynamically
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND (d.name LIKE ? OR d.ocr_text LIKE ?) ");
        }
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            sql.append("AND d.category_id = ? ");
        }
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append("AND DATE(d.upload_date) = ? ");
        }
        if (ownerUsername != null && !ownerUsername.trim().isEmpty()) {
            sql.append("AND u.username LIKE ? ");
        }

        sql.append("ORDER BY d.upload_date DESC");

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql.toString());

            int paramIndex = 1;

            // Bind security variables for general User
            if (!"ADMIN".equalsIgnoreCase(userRole)) {
                stmt.setInt(paramIndex++, userId);
                stmt.setInt(paramIndex++, userId);
            }

            // Bind filters
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.trim() + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
                stmt.setInt(paramIndex++, Integer.parseInt(categoryIdStr));
            }
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                stmt.setDate(paramIndex++, Date.valueOf(dateStr));
            }
            if (ownerUsername != null && !ownerUsername.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + ownerUsername.trim() + "%");
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractDocumentFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeResources(rs, stmt, conn);
        }
        return list;
    }

    private Document extractDocumentFromResultSet(ResultSet rs) throws SQLException {
        Document doc = new Document(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("file_path"),
            rs.getLong("file_size"),
            rs.getString("file_extension"),
            rs.getInt("category_id"),
            rs.getInt("owner_id"),
            rs.getString("ocr_text"),
            rs.getBoolean("is_favorite"),
            rs.getTimestamp("upload_date")
        );
        doc.setCategoryName(rs.getString("category_name"));
        doc.setOwnerUsername(rs.getString("owner_username"));
        return doc;
    }
}

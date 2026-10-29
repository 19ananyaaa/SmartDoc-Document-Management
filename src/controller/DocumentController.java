package controller;

import dao.AuditLogDAO;
import dao.CategoryDAO;
import dao.DocumentDAO;
import dao.NotificationDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import model.AuditLog;
import model.Document;
import model.Notification;
import model.User;
import utility.OCRUtility;
import utility.SecurityUtility;

/**
 * Controller servlet for Document Actions (Upload, Download, Delete, Search, Share, Move, Favorite, Rename).
 * Mapped to /document. Handles multipart config file streams.
 * Compatible with Tomcat 10 (jakarta.servlet namespace).
 */
@WebServlet("/document")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB max file size
    maxRequestSize = 1024 * 1024 * 50    // 50MB max request size
)
public class DocumentController extends HttpServlet {

    private static final String UPLOAD_DIR_NAME = "uploads";
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "png", "jpg", "jpeg");

    private DocumentDAO docDAO;
    private UserDAO userDAO;
    private CategoryDAO catDAO;
    private AuditLogDAO auditDAO;
    private NotificationDAO notifDAO;

    @Override
    public void init() throws ServletException {
        docDAO = new DocumentDAO();
        userDAO = new UserDAO();
        catDAO = new CategoryDAO();
        auditDAO = new AuditLogDAO();
        notifDAO = new NotificationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if ("download".equalsIgnoreCase(action)) {
            handleDownload(request, response, loggedInUser);
        } else if ("search".equalsIgnoreCase(action)) {
            handleSearch(request, response, loggedInUser);
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if ("upload".equalsIgnoreCase(action)) {
            handleUpload(request, response, loggedInUser);
        } else if ("delete".equalsIgnoreCase(action)) {
            handleDelete(request, response, loggedInUser);
        } else if ("favorite".equalsIgnoreCase(action)) {
            handleFavorite(request, response, loggedInUser);
        } else if ("rename".equalsIgnoreCase(action)) {
            handleRename(request, response, loggedInUser);
        } else if ("move".equalsIgnoreCase(action)) {
            handleMove(request, response, loggedInUser);
        } else if ("share".equalsIgnoreCase(action)) {
            handleShare(request, response, loggedInUser);
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    private void handleUpload(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        String categoryIdStr = request.getParameter("categoryId");
        int categoryId = (categoryIdStr != null && !categoryIdStr.isEmpty()) ? Integer.parseInt(categoryIdStr) : 0;
        
        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            sessionRedirect(request, response, "error", "No file selected for upload.");
            return;
        }

        long fileSize = filePart.getSize();
        String originalFileName = getSubmittedFileName(filePart);
        
        // Extract Extension
        String fileExt = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExt = originalFileName.substring(dotIndex + 1).toLowerCase();
        }

        // Validate Extension
        if (!ALLOWED_EXTENSIONS.contains(fileExt)) {
            sessionRedirect(request, response, "error", "Invalid file extension. Allowed: PDF, DOCX, PNG, JPG, JPEG.");
            return;
        }

        // Validate Storage Limit
        long currentStorageUsed = userDAO.getUsedStorage(user.getId());
        if (currentStorageUsed + fileSize > user.getStorageLimit()) {
            sessionRedirect(request, response, "error", "Upload failed: Your storage limit (" + 
                            user.getStorageLimit() / (1024 * 1024) + "MB) has been exceeded.");
            
            // Emit warning notification
            Notification notif = new Notification(0, user.getId(), 
                "Storage quota exceeded. Failed to upload: " + originalFileName, false, null);
            notifDAO.createNotification(notif);
            return;
        }

        // Security check: Sanitize file name to prevent directory traversal in uploads
        String sanitizedName = new File(originalFileName).getName();
        sanitizedName = sanitizedName.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Resolve absolute folder path safely inside WEB WebContent
        String appPath = request.getServletContext().getRealPath("");
        String uploadPath = appPath + File.separator + UPLOAD_DIR_NAME;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Define unique disk file destination to prevent system overwrite
        String uniqueDiskName = System.currentTimeMillis() + "_" + sanitizedName;
        File diskFile = new File(uploadDir, uniqueDiskName);

        // Duplicate Check (Checking user database space)
        List<Document> userDocs = docDAO.getDocumentsByOwner(user.getId());
        for (Document d : userDocs) {
            if (d.getName().equalsIgnoreCase(sanitizedName)) {
                sessionRedirect(request, response, "error", "A document with the name '" + sanitizedName + "' already exists.");
                return;
            }
        }

        // Save File to local Web Directory
        filePart.write(diskFile.getAbsolutePath());

        // Perform OCR capture
        String extractedText = OCRUtility.extractText(diskFile);

        // Build database entry object
        Document doc = new Document(0, sanitizedName, UPLOAD_DIR_NAME + "/" + uniqueDiskName, 
                                    fileSize, fileExt, categoryId, user.getId(), extractedText, false, null);

        if (docDAO.createDocument(doc)) {
            AuditLog log = new AuditLog(0, user.getId(), "UPLOAD", 
                "Uploaded document: " + sanitizedName + " (Size: " + doc.getFormattedFileSize() + ")", 
                request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            Notification notif = new Notification(0, user.getId(), 
                "Document '" + sanitizedName + "' uploaded successfully.", false, null);
            notifDAO.createNotification(notif);

            sessionRedirect(request, response, "success", "Document uploaded successfully!");
        } else {
            // Delete disk file if DB write fails
            if (diskFile.exists()) diskFile.delete();
            sessionRedirect(request, response, "error", "Failed to register document in DB.");
        }
    }

    private void handleDownload(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing document parameter ID.");
            return;
        }

        int docId = Integer.parseInt(idStr);
        Document doc = docDAO.getDocumentById(docId);

        if (doc == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Document record not found.");
            return;
        }

        // Security check: Only allow download if owner or document shared with user
        boolean isOwner = doc.getOwnerId() == user.getId();
        boolean isShared = docDAO.isDocumentSharedWithUser(docId, user.getId());
        
        if (!isOwner && !isShared && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Unauthorised access to target file.");
            return;
        }

        // Safely resolve file path on disk (Directory Traversal Prevention)
        String appPath = request.getServletContext().getRealPath("");
        File file = new File(appPath + File.separator + doc.getFilePath());

        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Physical file not found on system.");
            return;
        }

        // Set response metadata headers
        String mimeType = request.getServletContext().getMimeType(file.getAbsolutePath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);
        response.setContentLength((int) file.length());
        
        // Encode file name safely for downloads header
        String encodedFileName = URLEncoder.encode(doc.getName(), StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        // Stream file
        try (FileInputStream inStream = new FileInputStream(file);
             OutputStream outStream = response.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }

        // Log operation
        AuditLog log = new AuditLog(0, user.getId(), "DOWNLOAD", 
            "Downloaded file: " + doc.getName(), request.getRemoteAddr(), null);
        auditDAO.createLog(log);
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        int docId = Integer.parseInt(request.getParameter("id"));
        Document doc = docDAO.getDocumentById(docId);

        if (doc == null) {
            sessionRedirect(request, response, "error", "Document not found.");
            return;
        }

        // Auth validation (Must be file owner)
        if (doc.getOwnerId() != user.getId() && !user.isAdmin()) {
            sessionRedirect(request, response, "error", "Access Denied: You do not own this document.");
            return;
        }

        // Delete physical file from disk
        String appPath = request.getServletContext().getRealPath("");
        File file = new File(appPath + File.separator + doc.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        // Delete database record
        if (docDAO.deleteDocument(docId)) {
            AuditLog log = new AuditLog(0, user.getId(), "DELETE", 
                "Deleted document: " + doc.getName(), request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            sessionRedirect(request, response, "success", "Document deleted successfully.");
        } else {
            sessionRedirect(request, response, "error", "Failed to delete database entry.");
        }
    }

    private void handleFavorite(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        int docId = Integer.parseInt(request.getParameter("id"));
        boolean favorite = Boolean.parseBoolean(request.getParameter("fav"));
        
        Document doc = docDAO.getDocumentById(docId);
        if (doc != null && (doc.getOwnerId() == user.getId() || user.isAdmin())) {
            docDAO.setFavorite(docId, favorite);
            response.getWriter().write("success");
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("unauthorized");
        }
    }

    private void handleRename(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        int docId = Integer.parseInt(request.getParameter("id"));
        String newName = request.getParameter("name");
        newName = SecurityUtility.escapeHtml(newName);

        Document doc = docDAO.getDocumentById(docId);
        if (doc == null || (doc.getOwnerId() != user.getId() && !user.isAdmin())) {
            sessionRedirect(request, response, "error", "Unauthorized or invalid document.");
            return;
        }

        if (newName == null || newName.trim().isEmpty()) {
            sessionRedirect(request, response, "error", "Filename cannot be empty.");
            return;
        }

        // Retain extension when renaming
        String ext = doc.getFileExtension();
        if (!newName.toLowerCase().endsWith("." + ext)) {
            newName = newName + "." + ext;
        }

        String oldName = doc.getName();
        doc.setName(newName);
        
        if (docDAO.updateDocument(doc)) {
            AuditLog log = new AuditLog(0, user.getId(), "RENAME", 
                "Renamed document '" + oldName + "' to '" + newName + "'", request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            sessionRedirect(request, response, "success", "Document renamed successfully!");
        } else {
            sessionRedirect(request, response, "error", "Failed to rename document.");
        }
    }

    private void handleMove(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        int docId = Integer.parseInt(request.getParameter("id"));
        int categoryId = Integer.parseInt(request.getParameter("categoryId"));

        Document doc = docDAO.getDocumentById(docId);
        if (doc == null || (doc.getOwnerId() != user.getId() && !user.isAdmin())) {
            sessionRedirect(request, response, "error", "Unauthorized or invalid document.");
            return;
        }

        doc.setCategoryId(categoryId);
        if (docDAO.updateDocument(doc)) {
            sessionRedirect(request, response, "success", "Document category updated!");
        } else {
            sessionRedirect(request, response, "error", "Failed to update category.");
        }
    }

    private void handleShare(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        int docId = Integer.parseInt(request.getParameter("id"));
        String shareUsername = request.getParameter("shareWithUsername");
        shareUsername = SecurityUtility.escapeHtml(shareUsername);

        Document doc = docDAO.getDocumentById(docId);
        if (doc == null || (doc.getOwnerId() != user.getId() && !user.isAdmin())) {
            sessionRedirect(request, response, "error", "Unauthorized or invalid document.");
            return;
        }

        User recipient = userDAO.getUserByUsername(shareUsername);
        if (recipient == null) {
            sessionRedirect(request, response, "error", "User '" + shareUsername + "' does not exist.");
            return;
        }

        if (recipient.getId() == user.getId()) {
            sessionRedirect(request, response, "error", "You cannot share a document with yourself.");
            return;
        }

        if (docDAO.shareDocument(docId, user.getId(), recipient.getId())) {
            // Log Share
            AuditLog log = new AuditLog(0, user.getId(), "SHARE_DOCUMENT", 
                "Shared file '" + doc.getName() + "' with user '" + shareUsername + "'", request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            // Add notification alert for recipient
            Notification notif = new Notification(0, recipient.getId(), 
                user.getUsername() + " shared a document with you: " + doc.getName(), false, null);
            notifDAO.createNotification(notif);

            sessionRedirect(request, response, "success", "Document shared successfully with " + shareUsername);
        } else {
            sessionRedirect(request, response, "error", "Document is already shared with " + shareUsername);
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        
        String query = request.getParameter("query");
        String categoryIdStr = request.getParameter("categoryId");
        String dateStr = request.getParameter("uploadDate");
        String ownerName = request.getParameter("owner");

        query = SecurityUtility.escapeHtml(query);
        categoryIdStr = SecurityUtility.escapeHtml(categoryIdStr);
        dateStr = SecurityUtility.escapeHtml(dateStr);
        ownerName = SecurityUtility.escapeHtml(ownerName);

        List<Document> searchResults = docDAO.searchDocuments(query, categoryIdStr, dateStr, ownerName, user.getId(), user.getRole());
        request.setAttribute("searchResults", searchResults);
        request.setAttribute("categories", catDAO.getAllCategories());
        
        // Pass original search keys back to UI
        request.setAttribute("searchQuery", query);
        request.setAttribute("selectedCategory", categoryIdStr);
        request.setAttribute("selectedDate", dateStr);
        request.setAttribute("selectedOwner", ownerName);

        request.getRequestDispatcher("/search.jsp").forward(request, response);
    }

    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // Get file name only
            }
        }
        return "unknown";
    }

    private void sessionRedirect(HttpServletRequest request, HttpServletResponse response, String paramName, String message) 
            throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute(paramName, message);
        response.sendRedirect(request.getHeader("Referer") != null ? request.getHeader("Referer") : request.getContextPath() + "/dashboard");
    }
}

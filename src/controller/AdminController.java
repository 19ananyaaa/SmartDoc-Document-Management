package controller;

import dao.AuditLogDAO;
import dao.CategoryDAO;
import dao.DocumentDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.AuditLog;
import model.Category;
import model.Document;
import model.User;
import utility.ReportUtility;
import utility.SecurityUtility;

/**
 * Controller servlet for Admin Dashboard operations.
 * Mapped to /admin.
 * Compatible with Tomcat 10 (jakarta.servlet namespace).
 */
@WebServlet("/admin")
public class AdminController extends HttpServlet {

    private UserDAO userDAO;
    private DocumentDAO docDAO;
    private CategoryDAO catDAO;
    private AuditLogDAO auditDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        docDAO = new DocumentDAO();
        catDAO = new CategoryDAO();
        auditDAO = new AuditLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin rights required.");
            return;
        }

        String action = request.getParameter("action");
        if ("report".equalsIgnoreCase(action)) {
            handleReportDownload(request, response);
        } else {
            loadDashboard(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin rights required.");
            return;
        }

        String action = request.getParameter("action");
        if ("toggleStatus".equalsIgnoreCase(action)) {
            handleToggleStatus(request, response, loggedInUser);
        } else if ("deleteUser".equalsIgnoreCase(action)) {
            handleDeleteUser(request, response, loggedInUser);
        } else if ("addCategory".equalsIgnoreCase(action)) {
            handleAddCategory(request, response, loggedInUser);
        } else if ("deleteCategory".equalsIgnoreCase(action)) {
            handleDeleteCategory(request, response, loggedInUser);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin");
        }
    }

    private void loadDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Fetch System Stats
        List<User> usersList = userDAO.getAllUsers();
        List<Document> allDocs = docDAO.getAllDocuments();
        List<Category> categories = catDAO.getAllCategories();
        
        // Paginated Audit Logs
        int page = 1;
        int limit = 15;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            page = Integer.parseInt(pageStr);
        }
        int offset = (page - 1) * limit;
        List<AuditLog> auditLogs = auditDAO.getAllLogs(limit, offset);
        int totalLogs = auditDAO.getTotalLogCount();
        int totalPages = (int) Math.ceil((double) totalLogs / limit);

        // System Storage Metrics
        long totalStorageUsed = 0;
        for (Document d : allDocs) {
            totalStorageUsed += d.getFileSize();
        }

        request.setAttribute("usersList", usersList);
        request.setAttribute("allDocs", allDocs);
        request.setAttribute("categories", categories);
        request.setAttribute("auditLogs", auditLogs);
        request.setAttribute("totalStorageUsed", totalStorageUsed);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("/admin_dashboard.jsp").forward(request, response);
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        
        int targetUserId = Integer.parseInt(request.getParameter("userId"));
        User user = userDAO.getUserById(targetUserId);

        if (user != null) {
            if (user.getId() == admin.getId()) {
                sessionAlert(request, "error", "You cannot deactivate your own admin account.");
            } else {
                String newStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "INACTIVE" : "ACTIVE";
                user.setStatus(newStatus);
                if (userDAO.updateUser(user)) {
                    AuditLog log = new AuditLog(0, admin.getId(), "ADMIN_TOGGLE_USER", 
                        "Changed user status for '" + user.getUsername() + "' to " + newStatus, 
                        request.getRemoteAddr(), null);
                    auditDAO.createLog(log);

                    sessionAlert(request, "success", "User status updated to " + newStatus);
                } else {
                    sessionAlert(request, "error", "Failed to update status.");
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin");
    }

    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        
        int targetUserId = Integer.parseInt(request.getParameter("userId"));
        User user = userDAO.getUserById(targetUserId);

        if (user != null) {
            if (user.getId() == admin.getId()) {
                sessionAlert(request, "error", "You cannot delete your own admin account.");
            } else {
                if (userDAO.deleteUser(targetUserId)) {
                    AuditLog log = new AuditLog(0, admin.getId(), "ADMIN_DELETE_USER", 
                        "Deleted user account: " + user.getUsername() + " (" + user.getEmail() + ")", 
                        request.getRemoteAddr(), null);
                    auditDAO.createLog(log);

                    sessionAlert(request, "success", "User account deleted successfully.");
                } else {
                    sessionAlert(request, "error", "Failed to delete user account.");
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin");
    }

    private void handleAddCategory(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        
        String name = request.getParameter("name");
        name = SecurityUtility.escapeHtml(name);

        if (name == null || name.trim().isEmpty()) {
            sessionAlert(request, "error", "Category name cannot be empty.");
        } else {
            Category cat = new Category(0, name, null);
            if (catDAO.createCategory(cat)) {
                AuditLog log = new AuditLog(0, admin.getId(), "ADMIN_ADD_CATEGORY", 
                    "Added new document category: " + name, request.getRemoteAddr(), null);
                auditDAO.createLog(log);

                sessionAlert(request, "success", "Category '" + name + "' added successfully.");
            } else {
                sessionAlert(request, "error", "Category already exists.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin");
    }

    private void handleDeleteCategory(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        
        int catId = Integer.parseInt(request.getParameter("categoryId"));
        Category cat = catDAO.getCategoryById(catId);

        if (cat != null) {
            if (catDAO.deleteCategory(catId)) {
                AuditLog log = new AuditLog(0, admin.getId(), "ADMIN_DELETE_CATEGORY", 
                    "Deleted category: " + cat.getName(), request.getRemoteAddr(), null);
                auditDAO.createLog(log);

                sessionAlert(request, "success", "Category deleted successfully.");
            } else {
                sessionAlert(request, "error", "Failed to delete category.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin");
    }

    private void handleReportDownload(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String type = request.getParameter("type"); // "user", "storage", "upload"
        String format = request.getParameter("format"); // "pdf", "csv"
        
        if (type == null || format == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing report parameters.");
            return;
        }

        // Configure download content type
        if ("csv".equalsIgnoreCase(format)) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + type + "_report_" + System.currentTimeMillis() + ".csv");
        } else {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + type + "_report_" + System.currentTimeMillis() + ".pdf");
        }

        try {
            if ("user".equalsIgnoreCase(type)) {
                List<User> list = userDAO.getAllUsers();
                ReportUtility.generateUserReport(list, response.getOutputStream(), format);
            } else if ("storage".equalsIgnoreCase(type)) {
                List<User> list = userDAO.getAllUsers();
                ReportUtility.generateStorageReport(list, userDAO, response.getOutputStream(), format);
            } else if ("upload".equalsIgnoreCase(type)) {
                List<Document> list = docDAO.getAllDocuments();
                ReportUtility.generateUploadReport(list, response.getOutputStream(), format);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid report type.");
            }
        } catch (Exception e) {
            System.err.println("Failed to compile report stream: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to compile report.");
        }
    }

    private void sessionAlert(HttpServletRequest request, String paramName, String message) {
        HttpSession session = request.getSession();
        session.setAttribute(paramName, message);
    }
}

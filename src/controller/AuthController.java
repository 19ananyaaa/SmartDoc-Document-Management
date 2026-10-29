package controller;

import dao.AuditLogDAO;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Random;
import model.AuditLog;
import model.User;
import utility.SecurityUtility;

/**
 * Controller servlet for User Authentication (Login, Register, Logout, Forgot Password, Reset Password).
 * Mapped to /auth.
 * Compatible with Tomcat 10 (jakarta.servlet namespace).
 */
@WebServlet("/auth")
public class AuthController extends HttpServlet {

    private UserDAO userDAO;
    private AuditLogDAO auditDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        auditDAO = new AuditLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equalsIgnoreCase(action)) {
            handleLogout(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("login".equalsIgnoreCase(action)) {
            handleLogin(request, response);
        } else if ("register".equalsIgnoreCase(action)) {
            handleRegister(request, response);
        } else if ("forgot_password".equalsIgnoreCase(action)) {
            handleForgotPassword(request, response);
        } else if ("reset_password".equalsIgnoreCase(action)) {
            handleResetPassword(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        // Server-side validation
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Username and Password cannot be empty.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.getUserByUsername(username);
        
        // Match user credentials and status
        if (user != null && SecurityUtility.verifyPassword(password, user.getPassword())) {
            if (!user.isActive()) {
                request.setAttribute("error", "Your account has been deactivated. Please contact support.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            // Create secure session
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);

            // Re-generate CSRF token on login
            session.setAttribute("csrf_token", SecurityUtility.generateCsrfToken());

            // Handle Remember Me Cookie
            if ("on".equals(rememberMe)) {
                Cookie userCookie = new Cookie("remember_user", user.getUsername());
                userCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days validity
                userCookie.setHttpOnly(true);
                userCookie.setPath(request.getContextPath());
                response.addCookie(userCookie);
            } else {
                // Delete previous cookie if checkbox is unchecked
                Cookie killCookie = new Cookie("remember_user", "");
                killCookie.setMaxAge(0);
                killCookie.setPath(request.getContextPath());
                response.addCookie(killCookie);
            }

            // Audit log
            AuditLog log = new AuditLog(0, user.getId(), "LOGIN", "Successful user login", request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            // Redirect based on role
            if (user.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
        } else {
            // Failed audit log (system scope ID: -1)
            AuditLog log = new AuditLog(0, 0, "FAILED_LOGIN", "Failed login attempt for username: " + SecurityUtility.escapeHtml(username), request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            request.setAttribute("error", "Invalid username or password.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Safe Escaping to prevent XSS
        username = SecurityUtility.escapeHtml(username);
        email = SecurityUtility.escapeHtml(email);

        // Validation
        if (!SecurityUtility.isValidUsername(username)) {
            request.setAttribute("error", "Username must be alphanumeric and 3-20 characters long.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!SecurityUtility.isValidEmail(email)) {
            request.setAttribute("error", "Please provide a valid email address.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (password == null || password.length() < 6) {
            request.setAttribute("error", "Password must be at least 6 characters long.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Duplicate checks
        if (userDAO.getUserByUsername(username) != null) {
            request.setAttribute("error", "Username is already taken.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (userDAO.getUserByEmail(email) != null) {
            request.setAttribute("error", "Email is already registered.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Save User
        String hashedPassword = SecurityUtility.hashPassword(password);
        User newUser = new User(0, username, email, hashedPassword, "USER", "ACTIVE", 104857600L, null); // 100MB

        if (userDAO.createUser(newUser)) {
            User registered = userDAO.getUserByUsername(username);
            AuditLog log = new AuditLog(0, registered.getId(), "REGISTRATION", "Account registered successfully", request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            request.setAttribute("success", "Registration successful! You can now log in.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "An error occurred during registration. Please try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        email = SecurityUtility.escapeHtml(email);

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Email address is required.");
            request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
            return;
        }

        User user = userDAO.getUserByEmail(email);
        if (user != null) {
            // Generate 6 digit OTP
            Random rand = new Random();
            String otp = String.format("%06d", rand.nextInt(1000000));
            Timestamp expiry = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000)); // 5 minutes

            userDAO.saveOTP(email, otp, expiry);

            // SIMULATING EMAIL NOTIFICATION (Write OTP to Audit Logs so the user/admin can retrieve it for local testing!)
            AuditLog log = new AuditLog(0, user.getId(), "FORGOT_PASSWORD_OTP", 
                "Password reset OTP generated. SIMULATED OTP CODE IS: " + otp, request.getRemoteAddr(), null);
            auditDAO.createLog(log);

            request.setAttribute("email", email);
            request.setAttribute("success", "A simulated verification code has been dispatched. (Check security audit logs or enter the code: " + otp + ")");
            request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "No account associated with that email.");
            request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String otp = request.getParameter("otp");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        email = SecurityUtility.escapeHtml(email);
        otp = SecurityUtility.escapeHtml(otp);

        if (password == null || password.length() < 6) {
            request.setAttribute("email", email);
            request.setAttribute("error", "Password must be at least 6 characters long.");
            request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("email", email);
            request.setAttribute("error", "Passwords do not match.");
            request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
            return;
        }

        // Verify OTP code
        if (userDAO.verifyOTP(email, otp)) {
            String newHashedPassword = SecurityUtility.hashPassword(password);
            if (userDAO.updatePassword(email, newHashedPassword)) {
                userDAO.deleteOTP(email);
                
                User user = userDAO.getUserByEmail(email);
                AuditLog log = new AuditLog(0, user.getId(), "PASSWORD_RESET", "Password reset successfully using OTP", request.getRemoteAddr(), null);
                auditDAO.createLog(log);

                request.setAttribute("success", "Password successfully reset! Please log in.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } else {
                request.setAttribute("email", email);
                request.setAttribute("error", "Failed to update password. Try again.");
                request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("email", email);
            request.setAttribute("error", "Invalid or expired verification code.");
            request.getRequestDispatcher("/forgot_password.jsp").forward(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                AuditLog log = new AuditLog(0, user.getId(), "LOGOUT", "User logged out successfully", request.getRemoteAddr(), null);
                auditDAO.createLog(log);
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}

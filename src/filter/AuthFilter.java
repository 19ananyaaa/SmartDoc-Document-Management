package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.User;

/**
 * Filter for Authentication and Authorization check.
 * Redirects unauthorized users to the login page and protects Admin resources.
 * Compatible with Tomcat 10 (jakarta.servlet namespace).
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Allow static assets, error pages, and auth JSPs/Servlets
        boolean isStaticAsset = path.startsWith("/css/") || path.startsWith("/js/") || 
                               path.startsWith("/images/") || path.startsWith("/uploads/");
        boolean isAuthPage = path.equals("/login.jsp") || path.equals("/register.jsp") || 
                             path.equals("/forgot_password.jsp") || path.equals("/auth");
        boolean isErrorPage = path.equals("/error_404.jsp");

        User loggedInUser = null;
        if (session != null) {
            loggedInUser = (User) session.getAttribute("user");
        }

        if (isStaticAsset || isAuthPage || isErrorPage) {
            // Already authenticated users redirect away from login/register to dashboard
            if (loggedInUser != null && isAuthPage && !path.equals("/auth")) {
                if (loggedInUser.isAdmin()) {
                    httpResponse.sendRedirect(contextPath + "/admin");
                } else {
                    httpResponse.sendRedirect(contextPath + "/dashboard");
                }
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login if user session is missing
        if (loggedInUser == null) {
            httpResponse.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        // User is authenticated. Enforce Admin authorization on administrative paths.
        boolean isAdminPath = path.startsWith("/admin") || path.equals("/admin_dashboard.jsp");
        if (isAdminPath && !loggedInUser.isAdmin()) {
            // Forward/redirect standard users trying to access Admin pages to 404/403
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrative rights required.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}

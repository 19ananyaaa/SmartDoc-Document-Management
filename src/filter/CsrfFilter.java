package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import utility.SecurityUtility;

/**
 * Filter to protect state-changing requests (POST, PUT, DELETE) against 
 * Cross-Site Request Forgery (CSRF) attacks.
 * Compatible with Tomcat 10 (jakarta.servlet namespace).
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true); // Ensure session exists

        // Generate CSRF token if not already in session
        String sessionToken = (String) session.getAttribute("csrf_token");
        if (sessionToken == null) {
            sessionToken = SecurityUtility.generateCsrfToken();
            session.setAttribute("csrf_token", sessionToken);
        }

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        // Skip CSRF validation for static resources
        boolean isStaticAsset = path.startsWith("/css/") || path.startsWith("/js/") || 
                               path.startsWith("/images/") || path.startsWith("/uploads/");
        
        if (isStaticAsset) {
            chain.doFilter(request, response);
            return;
        }

        // Validate token on modifying methods (POST, PUT, DELETE)
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            // Retrieve token from form parameters or request headers
            String requestToken = httpRequest.getParameter("csrfToken");
            if (requestToken == null) {
                requestToken = httpRequest.getHeader("X-CSRF-TOKEN");
            }

            // Verify tokens
            if (requestToken == null || !requestToken.equals(sessionToken)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token. Request rejected.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}

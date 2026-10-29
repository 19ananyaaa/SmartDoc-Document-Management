package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to append modern security headers to all HTTP responses.
 * Helps prevent Clickjacking, XSS, and MIME-sniffing exploits.
 * Compatible with Tomcat 10 (jakarta.servlet namespace).
 */
@WebFilter("/*")
public class SecurityHeaderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            // Prevent site from being embedded inside iframe tags (Clickjacking Protection)
            httpResponse.setHeader("X-Frame-Options", "DENY");
            
            // Stop browser from analyzing content types (MIME Sniffing Protection)
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            
            // Enable XSS protection filter in modern browsers
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            
            // Enforce basic Content Security Policy (CSP)
            httpResponse.setHeader("Content-Security-Policy", 
                "default-src 'self'; " +
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                "img-src 'self' data:; " +
                "frame-ancestors 'none';");
            
            // Configure referrer leakages
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Cache control headers for security (prevent browser from storing sensitive documents in cache)
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}

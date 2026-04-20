package org.openmrs.module.ipd.web.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security filter to block path traversal attacks.
 * This filter runs before Spring's dispatcher to prevent path traversal patterns
 * from reaching the controllers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PathTraversalFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestUri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        
        // Check for path traversal patterns in URI
        if (containsPathTraversal(requestUri)) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path - path traversal detected");
            return;
        }
        
        // Check for path traversal in query string
        if (queryString != null && containsPathTraversal(queryString)) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid query parameter - path traversal detected");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * Checks if the input contains path traversal patterns
     */
    private boolean containsPathTraversal(String input) {
        if (input == null) {
            return false;
        }
        
        // Decode URL encoding first
        String decoded;
        try {
            decoded = java.net.URLDecoder.decode(input, "UTF-8");
        } catch (Exception e) {
            decoded = input;
        }
        
        // Check for various path traversal patterns
        return decoded.contains("..") ||           // Unix/Linux traversal
               decoded.contains("..\\") ||          // Windows traversal  
               decoded.contains("%2e%2e") ||        // URL encoded ..
               decoded.contains("%252e%252e") ||    // Double URL encoded ..
               decoded.contains("....//") ||       // Double dot bypass
               decoded.contains("....\\") ||        // Windows double dot bypass
               decoded.contains("../") ||          // Simple traversal
               decoded.contains("..\\") ||         // Windows simple traversal
               decoded.contains("0x2e0x2e") ||     // Hex encoded
               decoded.contains("\\..\\");          // Windows parent reference
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }
    
    @Override
    public void destroy() {
        // No cleanup needed
    }
}

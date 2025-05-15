package com.app.health.tracking.HealthTracking.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AccessLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger("AccessLogger");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String timestamp = LocalDateTime.now().format(formatter);
        String method = wrappedRequest.getMethod();
        String url = wrappedRequest.getRequestURI();
        String clientIp = wrappedRequest.getRemoteAddr();
        String authHeader = wrappedRequest.getHeader("Authorization");
        String user = "Anonymous";

        // Extract user from the Authorization token if available
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // We'll set the user in the SecurityContext in the TokenValidationFilter,
            // so we can retrieve it later. For now, we'll log the token presence.
            user = "Token: " + token; // Simplified; we'll refine this in the next step
        }

        // Log the incoming request
        logger.info("Request [{}] - {} {} from IP: {} by User: {}", timestamp, method, url, clientIp, user);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Log the response status after the request is processed
            int status = wrappedResponse.getStatus();
            logger.info("Response [{}] - {} {} -> Status: {} for User: {}", timestamp, method, url, status, user);
            wrappedResponse.copyBodyToResponse(); // Ensure response body is written back
        }
    }
}

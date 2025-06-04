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
        String user = (String) wrappedRequest.getAttribute("username");
        String token = wrappedRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // Extract the token
        } else {
            token = "No token";
        }

        if (user == null) {
            user = "Anonymous";
        }

        // Log the incoming request with token
        logger.info("Request [{}] - {} {} from IP: {} by User: {} with Token: {}", timestamp, method, url, clientIp, user, token);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Log the response status after the request is processed
            int status = wrappedResponse.getStatus();
            logger.info("Response [{}] - {} {} -> Status: {} for User: {} with Token: {}", timestamp, method, url, status, user, token);
            wrappedResponse.copyBodyToResponse();
        }
    }
}
package com.app.health.tracking.HealthTracking.jwt;

import com.app.health.tracking.HealthTracking.model.Users;
import com.app.health.tracking.HealthTracking.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenValidationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register") ||
                path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Missing or invalid Authorization header for request to: {}", path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            logger.error("Invalid or expired JWT token for request to: {}", path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        Users user = usersRepository.findByUsername(username);
        if (user == null) {
            logger.error("User not found for token in request to: {}", path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        UserDetails userDetails = new User(user.getUsername(), user.getPassword(),
                Collections.singletonList(() -> "ROLE_" + user.getRole().name()));
        SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));

        request.setAttribute("userId", user.getId());
        request.setAttribute("role", user.getRole().name());
        request.setAttribute("username", user.getUsername());

        logger.debug("Successfully authenticated user: {} with role: {} for request to: {}", user.getUsername(), user.getRole(), path);
        filterChain.doFilter(request, response);
    }
}
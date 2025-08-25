package com.ProjectAnkit.EcoTracker.config;

import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import javax.crypto.spec.SecretKeySpec;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Handle CORS preflight request
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        // Skip JWT check for auth endpoints and Swagger UI
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/v3/api-docs") || 
            path.startsWith("/swagger-ui") ||
            path.equals("/swagger-ui.html")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            String token = header.substring(7);
            Jws<Claims> claimsJws = parseJwt(token);
            Claims claims = claimsJws.getBody();

            String email = claims.getSubject();
            if (email == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: No subject (email) in token");
                return;
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + e.getMessage());
        }
    }
    
    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"status\":%d,\"error\":\"%s\"}", status, message);
        response.getWriter().write(jsonResponse);
    }
    
    private Jws<Claims> parseJwt(String token) {
        try {
            // First try to decode the secret as Base64
            byte[] keyBytes;
            try {
                keyBytes = Base64.getDecoder().decode(jwtSecret);
                if (keyBytes.length < 32) { // Minimum key length for HS512
                    throw new IllegalArgumentException("Decoded key is too short for HS512");
                }
            } catch (IllegalArgumentException e) {
                // If Base64 decode fails, use the secret as is
                keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            }
            
            java.security.Key key = new SecretKeySpec(keyBytes, "HmacSHA512");
            
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT: " + e.getMessage(), e);
        }
    }
}
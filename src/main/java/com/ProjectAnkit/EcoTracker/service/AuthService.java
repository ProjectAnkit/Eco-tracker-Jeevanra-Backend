package com.ProjectAnkit.EcoTracker.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String jwtSecret;

    public AuthService(UserRepository userRepository, 
                      @Value("${jwt.secret}") String jwtSecret,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecret = jwtSecret;
    }

    public String register(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required");
        }
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setLocation(""); // Default empty location
        user.setPoints(0);
        
        return generateJwt(userRepository.save(user));
    }

    public String login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required");
        }
        
        User existingUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));
            
        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        return generateJwt(existingUser);
    }

    private String generateJwt(User user) {
        try {
            // Use the secret directly as bytes for HMAC-SHA256
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            
            // Ensure minimum key length for HMAC-SHA256
            if (keyBytes.length < 32) {
                // Pad the key if it's too short
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
                keyBytes = paddedKey;
            }
            
            java.security.Key key = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
            
            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("id", user.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }
}
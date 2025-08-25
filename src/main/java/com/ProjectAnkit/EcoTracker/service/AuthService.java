package com.ProjectAnkit.EcoTracker.service;

import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

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
            
            java.security.Key key = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA512");
            
            return Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("id", user.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token: " + e.getMessage(), e);
        }
    }
}
package com.ProjectAnkit.EcoTracker.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping
    public Map<String, String> updateProfile(@RequestBody Map<String, String> body, @RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // Update user fields
        if (body.containsKey("name")) user.setName(body.get("name"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("location")) user.setLocation(body.get("location"));
        if (body.containsKey("bio")) user.setBio(body.get("bio"));
        
        userRepository.save(user);
        return Map.of("message", "Profile updated successfully");
    }

    @GetMapping
    public Map<String, Object> getProfile(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("name", user.getName());
        profile.put("avatar", user.getAvatar());
        profile.put("location", user.getLocation());
        profile.put("bio", user.getBio());
        profile.put("points", user.getPoints());
        profile.put("co2Saved", user.getCo2Saved());
        
        return profile;
    }
}
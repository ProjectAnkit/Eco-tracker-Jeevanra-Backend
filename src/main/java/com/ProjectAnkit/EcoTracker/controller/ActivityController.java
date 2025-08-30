package com.ProjectAnkit.EcoTracker.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ProjectAnkit.EcoTracker.entity.Activity;
import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.ActivityRepository;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;
import com.ProjectAnkit.EcoTracker.service.EmissionCalculatorService;

@RestController
@RequestMapping("/api/track")
public class ActivityController {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final EmissionCalculatorService calculatorService;

    public ActivityController(ActivityRepository activityRepository, UserRepository userRepository, EmissionCalculatorService calculatorService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.calculatorService = calculatorService;
    }

    @PostMapping
    public ResponseEntity<?> trackActivity(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email is required in the request body"));
        }
        
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "User not found with email: " + email));
        }

        try {
            Activity activity = new Activity();
            activity.setUser(user);
            activity.setType((String) body.get("type"));
            activity.setDetails("{\"units\": " + body.get("units") + "}");
            double units = ((Number) body.get("units")).doubleValue();
            double emissions = calculatorService.calculate(activity.getType(), units);
            activity.setEmissionsKg(emissions);
            activity.setTimestamp(LocalDateTime.now());
            Activity savedActivity = activityRepository.save(activity);
            
            if (savedActivity.getId() == null) {
                return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to save activity"));
            }
            
            // Update user's total emissions and points based on this activity
            try {
                double newTotalEmissions = (user.getCo2Saved() == 0 ? 0 : user.getCo2Saved()) + emissions;
                user.setCo2Saved(newTotalEmissions); // Note: Renaming suggestion in comments below
                
                // New points system: Reward low-emission choices with more points.
                // Greenness = 1 / factor (higher for low-emission categories)
                // Points = greenness * units * 10 (scaled, rounded, min 1)
                // This encourages choosing/using low-emission options (e.g., train over car, plant over meat)
                double factor = emissions / units; // Derive factor (assumes units > 0)
                double greenness = (factor > 0) ? 1.0 / factor : 0.0;
                int pointsEarned = (int) Math.max(1, Math.round(greenness * units * 10));
                user.setPoints(user.getPoints() + pointsEarned);
                userRepository.save(user);
            } catch (Exception e) { /* ignore partial update errors */ }
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Activity tracked", 
                    "emissions", savedActivity.getEmissionsKg(),
                    "activityId", savedActivity.getId(),
                    "userTotalEmissions", user.getCo2Saved(), // Note: Renaming suggestion in comments
                    "userPoints", user.getPoints()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error tracking activity: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent")
    public List<Activity> getRecentActivities(
            @RequestParam String email,
            @RequestParam(defaultValue = "3") int limit) {
        
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        
        List<Activity> activities = activityRepository.findRecentActivitiesWithLimit(user.getId(), limit);
        return activities != null ? activities : new ArrayList<>();
    }
    
    @GetMapping("/all")
    public List<Activity> getAllActivities(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        
        List<Activity> activities = activityRepository.findRecentActivities(user.getId());
        return activities != null ? activities : new ArrayList<>();
    }
}
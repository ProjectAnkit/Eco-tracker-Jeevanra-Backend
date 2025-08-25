package com.ProjectAnkit.EcoTracker.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        try {
            Activity activity = new Activity();
            activity.setUser(user);
            activity.setType((String) body.get("type"));
            activity.setDetails("{\"units\": " + body.get("units") + "}");
            activity.setEmissionsKg(calculatorService.calculate(activity.getType(), ((Number) body.get("units")).doubleValue()));
            activity.setTimestamp(LocalDateTime.now());
            Activity savedActivity = activityRepository.save(activity);
            
            if (savedActivity.getId() == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to save activity"));
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Activity tracked", 
                "emissions", savedActivity.getEmissionsKg(),
                "activityId", savedActivity.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error tracking activity: " + e.getMessage()));
        }
    }
    
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentActivities(
            @RequestParam(defaultValue = "3") int limit) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        try {
            List<Activity> activities = activityRepository.findRecentActivitiesWithLimit(user.getId(), limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching activities: " + e.getMessage()));
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllActivities() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        try {
            List<Activity> activities = activityRepository.findRecentActivities(user.getId());
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching activities: " + e.getMessage()));
        }
    }
}
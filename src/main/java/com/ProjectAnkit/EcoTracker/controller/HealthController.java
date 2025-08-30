package com.ProjectAnkit.EcoTracker.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "EcoTracker API is running"));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "timestamp", System.currentTimeMillis(),
            "message", "Test endpoint working correctly"
        ));
    }

    @GetMapping("/test-activities")
    public ResponseEntity<Map<String, Object>> testActivities() {
        // Test endpoint to verify activity serialization works
        Map<String, Object> testActivity = new HashMap<>();
        testActivity.put("id", 1L);
        testActivity.put("type", "commute_car");
        testActivity.put("emissionsKg", 2.5);
        testActivity.put("timestamp", "2024-01-01T10:00:00");
        
        List<Map<String, Object>> activities = new ArrayList<>();
        activities.add(testActivity);
        
        Map<String, Object> response = new HashMap<>();
        response.put("activities", activities);
        response.put("count", 1);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}

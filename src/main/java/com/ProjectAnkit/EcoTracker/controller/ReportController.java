package com.ProjectAnkit.EcoTracker.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProjectAnkit.EcoTracker.entity.Activity;
import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.ActivityRepository;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ReportController(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getWeeklyReport() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        List<Activity> activities = activityRepository.findWeeklyActivities(user.getId());
        double total = activities.stream().mapToDouble(Activity::getEmissionsKg).sum();
        double[] weekly = new double[7];
        for (int i = 0; i < 7; i++) {
            int day = i;
            weekly[i] = activities.stream()
                    .filter(a -> a.getTimestamp().getDayOfWeek().getValue() - 1 == day)
                    .mapToDouble(Activity::getEmissionsKg)
                    .sum();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("weekly", weekly);
        return ResponseEntity.ok(response);
    }
}
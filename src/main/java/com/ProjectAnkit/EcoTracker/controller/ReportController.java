package com.ProjectAnkit.EcoTracker.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public Map<String, Object> getWeeklyReport(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<Activity> activities = activityRepository.findWeeklyActivities(user.getId());
        if (activities == null) {
            activities = new ArrayList<>();
        }
        
        double total = activities.stream().mapToDouble(Activity::getEmissionsKg).sum();
        // Removed user update: Lifetime totals/points should accumulate per activity (in ActivityController),
        // not be overwritten here with weekly values. This was likely a bug.
        // If you need to recompute lifetime from all activities (e.g., for consistency),
        // query all activities and sum, but that's inefficient; rely on incremental updates instead.
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
        return response;
    }
}
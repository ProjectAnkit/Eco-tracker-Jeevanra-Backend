package com.ProjectAnkit.EcoTracker.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ProjectAnkit.EcoTracker.entity.Challenge;
import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.service.ChallengeService;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public List<Challenge> getChallenges() {
        return challengeService.getAllChallenges();
    }

    @PostMapping
    public Challenge createChallenge(@RequestParam String name, @RequestParam double goal) {
        return challengeService.createChallenge(name, goal);
    }

    @GetMapping("/search")
    public List<Challenge> searchChallenges(@RequestParam String query) {
        return challengeService.searchChallenges(query);
    }

    @PostMapping("/{id}/join")
    public Challenge joinChallenge(@PathVariable Long id, @RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        return challengeService.joinChallenge(id, email);
    }

    @PostMapping("/{id}/leave")
    public Challenge leaveChallenge(@PathVariable Long id, @RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        return challengeService.leaveChallenge(id, email);
    }

    @GetMapping("/{challengeId}/leaderboard")
    public List<User> getLeaderboard(@PathVariable Long challengeId) {
        // Return users sorted by CO2 saved desc to include name/avatar/points/co2Saved
        return challengeService.getLeaderboard(challengeId);
    }

    @GetMapping("/{challengeId}/ranking")
    public Map<String, Integer> getRanking(@RequestParam String email, @PathVariable Long challengeId) {
        System.out.println("🔍 Getting ranking for email: " + email + ", challengeId: " + challengeId);
        try {
            int rank = challengeService.getRanking(email, challengeId);
            System.out.println("✅ User rank: " + rank);
            return Map.of("rank", rank);
        } catch (Exception e) {
            System.err.println("❌ Error getting ranking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
package com.ProjectAnkit.EcoTracker.controller;

import com.ProjectAnkit.EcoTracker.entity.Challenge;
import com.ProjectAnkit.EcoTracker.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinChallenge(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Challenge challenge = challengeService.joinChallenge(id, email);
            return ResponseEntity.ok(challenge);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
package com.ProjectAnkit.EcoTracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ProjectAnkit.EcoTracker.entity.Challenge;
import com.ProjectAnkit.EcoTracker.entity.User;
import com.ProjectAnkit.EcoTracker.repository.ChallengeRepository;
import com.ProjectAnkit.EcoTracker.repository.UserRepository;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public ChallengeService(ChallengeRepository challengeRepository, UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public Challenge joinChallenge(Long challengeId, String email) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        if (challenge.getParticipants().contains(user)) {
            throw new RuntimeException("User already joined the challenge");
        }

        challenge.getParticipants().add(user);
        return challengeRepository.save(challenge);
    }
}
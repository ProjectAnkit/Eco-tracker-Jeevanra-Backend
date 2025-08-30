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

    public Challenge createChallenge(String name, double goal) {
        Challenge challenge = new Challenge();
        challenge.setName(name);
        challenge.setGoal(goal);
        challenge.setParticipants(new java.util.ArrayList<>());
        return challengeRepository.save(challenge);
    }

    public List<Challenge> searchChallenges(String query) {
        return challengeRepository.findByNameContainingIgnoreCase(query);
    }

    public Challenge joinChallenge(Long challengeId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (challenge.getParticipants().contains(user)) {
            throw new RuntimeException("User already joined the challenge");
        }

        challenge.getParticipants().add(user);
        user.getChallenges().add(challenge);
        return challengeRepository.save(challenge);
    }

    public Challenge leaveChallenge(Long challengeId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!challenge.getParticipants().contains(user)) {
            throw new RuntimeException("User is not participating in this challenge");
        }

        challenge.getParticipants().remove(user);
        user.getChallenges().remove(challenge);
        return challengeRepository.save(challenge);
    }

    public List<User> getLeaderboard(Long challengeId) {
        return userRepository.findByChallengeIdOrderByCo2SavedDesc(challengeId);
    }

    public int getRanking(String email, Long challengeId) {
        List<User> leaderboard = getLeaderboard(challengeId);
        for (int i = 0; i < leaderboard.size(); i++) {
            User user = leaderboard.get(i);
            if (user.getEmail().equals(email)) {
                return i + 1;
            }
        }
        throw new RuntimeException("User not found in this challenge");
    }
}
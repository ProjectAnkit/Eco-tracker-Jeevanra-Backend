package com.ProjectAnkit.EcoTracker.repository;

import com.ProjectAnkit.EcoTracker.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
package com.ProjectAnkit.EcoTracker.repository;

import com.ProjectAnkit.EcoTracker.entity.Challenge;
import com.ProjectAnkit.EcoTracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByNameContainingIgnoreCase(String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Challenge c JOIN c.participants p WHERE p = :user")
    boolean isUserParticipatingInAnyChallenge(@Param("user") User user);
}
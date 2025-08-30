package com.ProjectAnkit.EcoTracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ProjectAnkit.EcoTracker.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // New: Users in a specific challenge, ordered by co2Saved DESC
    @Query("SELECT u FROM User u JOIN u.challenges c WHERE c.id = :challengeId ORDER BY u.co2Saved DESC")
    List<User> findByChallengeIdOrderByCo2SavedDesc(@Param("challengeId") Long challengeId);

}
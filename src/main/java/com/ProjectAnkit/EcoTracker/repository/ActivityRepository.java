package com.ProjectAnkit.EcoTracker.repository;

import com.ProjectAnkit.EcoTracker.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    @Query(value = "SELECT * FROM activity a WHERE a.user_id = :userId AND a.timestamp >= CURRENT_DATE - INTERVAL '7' DAY", nativeQuery = true)
    List<Activity> findWeeklyActivities(Long userId);
    
    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    List<Activity> findRecentActivities(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId ORDER BY a.timestamp DESC LIMIT :limit")
    List<Activity> findRecentActivitiesWithLimit(@Param("userId") Long userId, @Param("limit") int limit);
}

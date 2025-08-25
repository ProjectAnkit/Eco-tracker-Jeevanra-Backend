package com.ProjectAnkit.EcoTracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Entity
@Table(name = "activity")
@Data
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private String type; // e.g., commute_car
    @Column(columnDefinition = "TEXT")
    private String details; // JSON for flexibility
    private double emissionsKg;
    private LocalDateTime timestamp;
}
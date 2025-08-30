package com.ProjectAnkit.EcoTracker.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    private String password; // Hashed
    private String name;
    @Column(columnDefinition = "TEXT")
    private String avatar;
    private String location;
    @Column(columnDefinition = "TEXT")
    private String bio;
    private int points = 0;
    @Column(name = "co2_saved")
    private double co2Saved = 0;
    @ManyToMany(mappedBy = "participants")
    @JsonBackReference
    private List<Challenge> challenges = new ArrayList<>();
}
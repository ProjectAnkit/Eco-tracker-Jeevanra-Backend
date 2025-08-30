package com.ProjectAnkit.EcoTracker.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmissionCalculatorService {
    private final Map<String, Double> factors = Map.of(
        "commute_car", 0.192,       // kg CO2 per km (average gasoline car)
        "commute_motorcycle", 0.103, // kg CO2 per km
        "commute_bus", 0.104,       // kg CO2 per km (diesel bus)
        "commute_train", 0.041,     // kg CO2 per km (electric train)
        "air_travel", 0.255,        // kg CO2 per km (economy class)
        "energy_kwh", 0.475,        // kg CO2 per kWh (coal-heavy grid)
        "food_meat", 7.0,           // kg CO2 per kg (beef)
        "food_plant", 0.5           // kg CO2 per kg (vegetables)
    );

    public double calculate(String category, double units) {
        return factors.getOrDefault(category, 0.0) * units;
    }

    // Added getter for factors map (useful for other services/controllers if needed)
    public Map<String, Double> getFactors() {
        return factors;
    }
}
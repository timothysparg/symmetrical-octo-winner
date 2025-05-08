package com.example.weather.hongkong.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Represents the response from the rainfall API.
 */
@Data
public class RainfallResponse {
    
    /**
     * The observation time in ISO-8601 format (e.g., 2025-05-08T01:45:00+08:00)
     */
    @JsonProperty("obsTime")
    private String observationTime;

    /**
     * The list of hourly rainfall data for each station
     */
    private List<RainfallStation> hourlyRainfall;
}
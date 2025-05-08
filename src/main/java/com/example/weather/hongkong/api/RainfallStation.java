package com.example.weather.hongkong.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a rainfall station in the API response.
 */
@Data
public class RainfallStation {
    
    /**
     * The ID of the automatic weather station
     */
    @JsonProperty("automaticWeatherStationID")
    private String stationId;
    
    /**
     * The name of the automatic weather station
     */
    @JsonProperty("automaticWeatherStation")
    private String stationName;
    
    /**
     * The rainfall value. Can be a number or "*" (which means trace amount, treated as 0)
     */
    @JsonProperty("value")
    private String value;
}
package com.example.weather.hongkong;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Document(collection = "rainfall_data")
public class RainfallData {
    @Id
    private String id;

    @JsonProperty("automaticWeatherStationID")
    @Indexed(unique = true)
    private String stationId;

    @JsonProperty("automaticWeatherStation")
    private String stationName;

    private double rainfallAmount;

    private LocalDateTime recordedAt;

    private LocalDateTime lastUpdated;

    private Long version;

    // Flags to track record status
    @Transient
    private boolean isNew = false;

    @Transient
    private boolean isUpdated = false;

    private String value;
}

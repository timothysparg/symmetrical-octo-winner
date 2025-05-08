package com.example.weather.hongkong;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RainfallData {
    private String stationId;

    private String stationName;

    private Double rainfallAmount;

    private LocalDateTime recordedAt;

    private LocalDateTime lastUpdated;

    private Long version;

    // Flags to track record status
    private transient boolean isNew = false;
    private transient boolean isUpdated = false;
}

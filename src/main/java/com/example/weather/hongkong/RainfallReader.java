package com.example.weather.hongkong;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.weather.hongkong.api.RainfallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class RainfallReader implements ItemReader<RainfallData> {

    private static final Logger logger = LoggerFactory.getLogger(RainfallReader.class);
    private final RestClient restClient;
    private final String apiPath;
    private List<RainfallData> rainfallDataList;
    private final AtomicInteger counter = new AtomicInteger();

    public RainfallReader(RestClient restClient) {
        this.restClient = restClient;
        this.apiPath = "/hourlyRainfall.php?lang=en";
        logger.info("RainfallReader initialized with API path: {}", apiPath);
    }

    @Override
    public RainfallData read() {
        if (rainfallDataList == null) {
            fetchRainfallData();
        }

        int index = counter.getAndIncrement();
        if (index < rainfallDataList.size()) {
            return rainfallDataList.get(index);
        }

        // Reset for next job run
        rainfallDataList = null;
        counter.set(0);
        return null; // End of data
    }

    private void fetchRainfallData() {
        logger.info("Fetching rainfall data from API path: {}", apiPath);
        rainfallDataList = new ArrayList<>();

        try {
            RainfallResponse rainfallResponse = restClient.get()
                .uri(apiPath)
                .retrieve()
                .body(RainfallResponse.class);

            logger.info("API response received successfully");
            logger.debug("API response body: {}", rainfallResponse);

            // Parse ISO-8601 format (2025-05-08T01:45:00+08:00)
            LocalDateTime recordedAt = LocalDateTime.parse(rainfallResponse.getObservationTime(), DateTimeFormatter.ISO_DATE_TIME);
            logger.info("Last update time: {}", rainfallResponse.getObservationTime());
            logger.debug("Parsed time: {}", recordedAt);

            List<RainfallData> stations = rainfallResponse.getHourlyRainfall().stream()
                .map(station -> {
                    RainfallData data = new RainfallData();
                    data.setStationId(station.getStationId());
                    data.setStationName(station.getStationName());
                    data.setValue(station.getValue());
                    data.setRecordedAt(recordedAt);
                    data.setLastUpdated(LocalDateTime.now());
                    return data;
                })
                .toList();

            logger.info("Found {} rainfall records", stations.size());
            rainfallDataList.addAll(stations);
            logger.info("Processed {} rainfall data records", stations.size());
        } catch (Exception e) {
            logger.error("Error fetching or parsing API response: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching or parsing API response", e);
        }
    }
}

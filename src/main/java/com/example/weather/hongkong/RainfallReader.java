package com.example.weather.hongkong;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.weather.hongkong.api.RainfallResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
class RainfallReader implements ItemReader<RainfallData> {

    private final RestClient restClient;
    private final String apiPath;
    private List<RainfallData> rainfallDataList;
    private final AtomicInteger counter = new AtomicInteger();

    public RainfallReader(RestClient restClient) {
        this.restClient = restClient;
        this.apiPath = "/hourlyRainfall.php?lang=en";
        log.info("RainfallReader initialized with API path: {}", apiPath);
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
        log.info("Fetching rainfall data from API path: {}", apiPath);
        rainfallDataList = new ArrayList<>();

        try {
            RainfallResponse rainfallResponse = restClient.get()
                .uri(apiPath)
                .retrieve()
                .body(RainfallResponse.class);

            log.info("API response received successfully");
            log.debug("API response body: {}", rainfallResponse);

            // Parse ISO-8601 format (2025-05-08T01:45:00+08:00)
            LocalDateTime recordedAt = LocalDateTime.parse(rainfallResponse.getObservationTime(), DateTimeFormatter.ISO_DATE_TIME);
            log.info("Last update time: {}", rainfallResponse.getObservationTime());
            log.debug("Parsed time: {}", recordedAt);

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

            log.info("Found {} rainfall records", stations.size());
            rainfallDataList.addAll(stations);
            log.info("Processed {} rainfall data records", stations.size());
        } catch (Exception e) {
            log.error("Error fetching or parsing API response: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching or parsing API response", e);
        }
    }
}

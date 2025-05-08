package com.example.weather.hongkong;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemReader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
class RainfallReader implements ItemReader<RainfallData> {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private List<RainfallData> rainfallDataList;
    private final AtomicInteger counter = new AtomicInteger();
    private final ObjectMapper objectMapper;

    public RainfallReader(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiUrl = "https://data.weather.gov.hk/weatherAPI/opendata/hourlyRainfall.php?lang=en";
        this.objectMapper = objectMapper;
        System.out.println("RainfallReader initialized with API URL: " + apiUrl);
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
        System.out.println("Fetching rainfall data from API: " + apiUrl);
        rainfallDataList = new ArrayList<>();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            System.out.println("API response status: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("API response body: " + response.getBody());

                JsonNode rootNode = objectMapper.readTree(response.getBody());

                // Extract the rainfall data
                String lastUpdateTime = rootNode.get("obsTime").asText();
                System.out.println("Last update time: " + lastUpdateTime);

                // Parse ISO-8601 format (2025-05-08T01:45:00+08:00)
                LocalDateTime recordedAt = LocalDateTime.parse(lastUpdateTime, DateTimeFormatter.ISO_DATE_TIME);
                System.out.println("Parsed time: " + recordedAt);

                // Process rainfall data
                JsonNode hourlyRainfallArray = rootNode.get("hourlyRainfall");
                System.out.println("Found " + hourlyRainfallArray.size() + " rainfall records");

                int count = 0;
                for (JsonNode stationData : hourlyRainfallArray) {
                    String stationId = stationData.get("automaticWeatherStationID").asText();
                    String stationName = stationData.get("automaticWeatherStation").asText();

                    RainfallData rainfallData = new RainfallData();
                    rainfallData.setStationId(stationId);
                    rainfallData.setStationName(stationName);

                    // Check if rainfall data is available
                    JsonNode valueNode = stationData.get("value");
                    if (valueNode != null && !valueNode.isNull()) {
                        String rainValue = valueNode.asText();
                        Double rainfallAmount = "*".equals(rainValue) ? 0.0 : Double.parseDouble(rainValue);
                        rainfallData.setRainfallAmount(rainfallAmount);
                    } else {
                        rainfallData.setRainfallAmount(0.0);
                    }

                    rainfallData.setRecordedAt(recordedAt);
                    rainfallData.setLastUpdated(LocalDateTime.now());

                    rainfallDataList.add(rainfallData);
                    count++;

                    System.out.println("Processed station: " + stationId + ", " + stationName + 
                                      ", rainfall: " + rainfallData.getRainfallAmount());
                }

                System.out.println("Processed " + count + " rainfall data records");
            } else {
                System.out.println("API returned non-success status code: " + response.getStatusCode());
                throw new RuntimeException("API returned non-success status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error fetching or parsing API response: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error fetching or parsing API response", e);
        }
    }
}

package com.example.weather.hongkong;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
class FileSystemRepository {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemRepository.class);
    private final ObjectMapper objectMapper;
    private final Path downstreamDir;
    private final Path upstreamDir;

    public FileSystemRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.downstreamDir = Paths.get("work", "down", "hk");
        this.upstreamDir = Paths.get("work", "upstream", "hk");
        
        // Create directories if they don't exist
        try {
            Files.createDirectories(downstreamDir);
            Files.createDirectories(upstreamDir);
        } catch (IOException e) {
            logger.error("Failed to create directories", e);
            throw new RuntimeException("Failed to create directories", e);
        }
    }

    public Optional<RainfallData> findByStationId(String stationId) {
        Path filePath = downstreamDir.resolve(stationId + ".json");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try {
            RainfallData data = objectMapper.readValue(filePath.toFile(), RainfallData.class);
            return Optional.of(data);
        } catch (IOException e) {
            logger.error("Failed to read file: {}", filePath, e);
            return Optional.empty();
        }
    }

    public void save(RainfallData data) {
        Path filePath = downstreamDir.resolve(data.getStationId() + ".json");
        try {
            objectMapper.writeValue(filePath.toFile(), data);
        } catch (IOException e) {
            logger.error("Failed to write file: {}", filePath, e);
            throw new RuntimeException("Failed to write file", e);
        }

        // If data is new or updated, also write to upstream directory
        if (data.isNew() || data.isUpdated()) {
            Path upstreamFilePath = upstreamDir.resolve(data.getStationId() + ".json");
            try {
                objectMapper.writeValue(upstreamFilePath.toFile(), data);
            } catch (IOException e) {
                logger.error("Failed to write upstream file: {}", upstreamFilePath, e);
                throw new RuntimeException("Failed to write upstream file", e);
            }
        }
    }

    public void saveAll(List<? extends RainfallData> items) {
        for (RainfallData data : items) {
            save(data);
        }
    }
}
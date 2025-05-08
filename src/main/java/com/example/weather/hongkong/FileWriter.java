package com.example.weather.hongkong;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
class FileWriter implements ItemWriter<RainfallData> {

    private static final Logger logger = LoggerFactory.getLogger(FileWriter.class);
    private final FileSystemRepository repository;

    public FileWriter(FileSystemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends RainfallData> chunk) {
        for (RainfallData data : chunk.getItems()) {
            data.setLastUpdated(LocalDateTime.now());

            if (data.isNew()) {
                logger.info("Adding new rainfall data for station: {}", data.getStationId());
            } else if (data.isUpdated()) {
                logger.info("Updating rainfall data for station: {}", data.getStationId());
            }
        }
        repository.saveAll(chunk.getItems());
    }
}
package com.example.weather.hongkong;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
class ChangeDetectionProcessor implements ItemProcessor<RainfallData, RainfallData> {

    private static final Logger log = LoggerFactory.getLogger(ChangeDetectionProcessor.class);
    private final FileSystemRepository repository;

    public ChangeDetectionProcessor(FileSystemRepository repository) {
        this.repository = repository;
    }

    @Override
    public RainfallData process(RainfallData incomingData) {
        Optional<RainfallData> existingData = repository.findByStationId(incomingData.getStationId());

        if (existingData.isEmpty()) {
            // New data
            incomingData.setNew(true);
            log.info("Detected new rainfall data for station: {}", incomingData.getStationId());
            return incomingData;
        } else {
            RainfallData existing = existingData.get();

            // Check if the rainfall amount has changed significantly (more than 0.1mm)
            boolean hasChanged = Math.abs(existing.getRainfallAmount() - incomingData.getRainfallAmount()) > 0.1;

            if (hasChanged) {
                // Keep ID and update other fields
                incomingData.setUpdated(true);
                incomingData.setVersion(existing.getVersion());
                log.info("Detected updated rainfall data for station: {}, Old: {}, New: {}",
                    incomingData.getStationId(), existing.getRainfallAmount(), incomingData.getRainfallAmount());
                return incomingData;
            }

            // No significant changes, filter out
            log.debug("No significant change for station: {}", incomingData.getStationId());
            return null;
        }
    }
}

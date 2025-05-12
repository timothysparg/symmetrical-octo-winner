package com.example.weather.hongkong;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RainfallDataRepository extends MongoRepository<RainfallData, String> {
    
    /**
     * Find a RainfallData document by its stationId
     * 
     * @param stationId the station ID to search for
     * @return an Optional containing the RainfallData if found, or empty if not found
     */
    Optional<RainfallData> findByStationId(String stationId);
}
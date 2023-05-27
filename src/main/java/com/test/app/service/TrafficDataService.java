package com.test.app.service;

import com.test.app.domain.TrafficData;
import com.test.app.repository.TrafficDataRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link TrafficData}.
 */
@Service
public class TrafficDataService {

    private final Logger log = LoggerFactory.getLogger(TrafficDataService.class);

    private final TrafficDataRepository trafficDataRepository;

    public TrafficDataService(TrafficDataRepository trafficDataRepository) {
        this.trafficDataRepository = trafficDataRepository;
    }

    /**
     * Save a trafficData.
     *
     * @param trafficData the entity to save.
     * @return the persisted entity.
     */
    public Mono<TrafficData> save(TrafficData trafficData) {
        log.debug("Request to save TrafficData : {}", trafficData);
        return trafficDataRepository.save(trafficData);
    }

    /**
     * Update a trafficData.
     *
     * @param trafficData the entity to save.
     * @return the persisted entity.
     */
    public Mono<TrafficData> update(TrafficData trafficData) {
        log.debug("Request to update TrafficData : {}", trafficData);
        return trafficDataRepository.save(trafficData);
    }

    /**
     * Partially update a trafficData.
     *
     * @param trafficData the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<TrafficData> partialUpdate(TrafficData trafficData) {
        log.debug("Request to partially update TrafficData : {}", trafficData);

        return trafficDataRepository
            .findById(trafficData.getId())
            .map(existingTrafficData -> {
                if (trafficData.getRank() != null) {
                    existingTrafficData.setRank(trafficData.getRank());
                }
                if (trafficData.getImpressions() != null) {
                    existingTrafficData.setImpressions(trafficData.getImpressions());
                }
                if (trafficData.getClicks() != null) {
                    existingTrafficData.setClicks(trafficData.getClicks());
                }
                if (trafficData.getDate() != null) {
                    existingTrafficData.setDate(trafficData.getDate());
                }

                return existingTrafficData;
            })
            .flatMap(trafficDataRepository::save);
    }

    /**
     * Get all the trafficData.
     *
     * @return the list of entities.
     */
    public Flux<TrafficData> findAll() {
        log.debug("Request to get all TrafficData");
        return trafficDataRepository.findAll();
    }

    /**
     * Returns the number of trafficData available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return trafficDataRepository.count();
    }

    /**
     * Get one trafficData by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<TrafficData> findOne(String id) {
        log.debug("Request to get TrafficData : {}", id);
        return trafficDataRepository.findById(id);
    }

    /**
     * Delete the trafficData by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete TrafficData : {}", id);
        return trafficDataRepository.deleteById(id);
    }
}

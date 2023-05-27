package com.test.app.repository;

import com.test.app.domain.TrafficData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the TrafficData entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TrafficDataRepository extends ReactiveMongoRepository<TrafficData, String> {}

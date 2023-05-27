package com.test.app.repository;

import com.test.app.domain.CronScheduler;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the CronScheduler entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CronSchedulerRepository extends ReactiveMongoRepository<CronScheduler, String> {}

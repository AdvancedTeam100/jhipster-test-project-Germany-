package com.test.app.service;

import com.test.app.domain.CronScheduler;
import com.test.app.repository.CronSchedulerRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link CronScheduler}.
 */
@Service
public class CronSchedulerService {

    private final Logger log = LoggerFactory.getLogger(CronSchedulerService.class);

    private final CronSchedulerRepository cronSchedulerRepository;

    public CronSchedulerService(CronSchedulerRepository cronSchedulerRepository) {
        this.cronSchedulerRepository = cronSchedulerRepository;
    }

    /**
     * Save a cronScheduler.
     *
     * @param cronScheduler the entity to save.
     * @return the persisted entity.
     */
    public Mono<CronScheduler> save(CronScheduler cronScheduler) {
        log.debug("Request to save CronScheduler : {}", cronScheduler);
        return cronSchedulerRepository.save(cronScheduler);
    }

    /**
     * Update a cronScheduler.
     *
     * @param cronScheduler the entity to save.
     * @return the persisted entity.
     */
    public Mono<CronScheduler> update(CronScheduler cronScheduler) {
        log.debug("Request to update CronScheduler : {}", cronScheduler);
        return cronSchedulerRepository.save(cronScheduler);
    }

    /**
     * Partially update a cronScheduler.
     *
     * @param cronScheduler the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<CronScheduler> partialUpdate(CronScheduler cronScheduler) {
        log.debug("Request to partially update CronScheduler : {}", cronScheduler);

        return cronSchedulerRepository
            .findById(cronScheduler.getId())
            .map(existingCronScheduler -> {
                if (cronScheduler.getName() != null) {
                    existingCronScheduler.setName(cronScheduler.getName());
                }
                if (cronScheduler.getCronExpression() != null) {
                    existingCronScheduler.setCronExpression(cronScheduler.getCronExpression());
                }

                return existingCronScheduler;
            })
            .flatMap(cronSchedulerRepository::save);
    }

    /**
     * Get all the cronSchedulers.
     *
     * @return the list of entities.
     */
    public Flux<CronScheduler> findAll() {
        log.debug("Request to get all CronSchedulers");
        return cronSchedulerRepository.findAll();
    }

    /**
     * Returns the number of cronSchedulers available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return cronSchedulerRepository.count();
    }

    /**
     * Get one cronScheduler by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<CronScheduler> findOne(String id) {
        log.debug("Request to get CronScheduler : {}", id);
        return cronSchedulerRepository.findById(id);
    }

    /**
     * Delete the cronScheduler by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete CronScheduler : {}", id);
        return cronSchedulerRepository.deleteById(id);
    }
}

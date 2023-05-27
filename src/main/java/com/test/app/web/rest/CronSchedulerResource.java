package com.test.app.web.rest;

import com.test.app.domain.CronScheduler;
import com.test.app.repository.CronSchedulerRepository;
import com.test.app.service.CronSchedulerService;
import com.test.app.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.test.app.domain.CronScheduler}.
 */
@RestController
@RequestMapping("/api")
public class CronSchedulerResource {

    private final Logger log = LoggerFactory.getLogger(CronSchedulerResource.class);

    private static final String ENTITY_NAME = "testCronScheduler";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CronSchedulerService cronSchedulerService;

    private final CronSchedulerRepository cronSchedulerRepository;

    public CronSchedulerResource(CronSchedulerService cronSchedulerService, CronSchedulerRepository cronSchedulerRepository) {
        this.cronSchedulerService = cronSchedulerService;
        this.cronSchedulerRepository = cronSchedulerRepository;
    }

    /**
     * {@code POST  /cron-schedulers} : Create a new cronScheduler.
     *
     * @param cronScheduler the cronScheduler to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cronScheduler, or with status {@code 400 (Bad Request)} if the cronScheduler has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/cron-schedulers")
    public Mono<ResponseEntity<CronScheduler>> createCronScheduler(@Valid @RequestBody CronScheduler cronScheduler)
        throws URISyntaxException {
        log.debug("REST request to save CronScheduler : {}", cronScheduler);
        if (cronScheduler.getId() != null) {
            throw new BadRequestAlertException("A new cronScheduler cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return cronSchedulerService
            .save(cronScheduler)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/cron-schedulers/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /cron-schedulers/:id} : Updates an existing cronScheduler.
     *
     * @param id the id of the cronScheduler to save.
     * @param cronScheduler the cronScheduler to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cronScheduler,
     * or with status {@code 400 (Bad Request)} if the cronScheduler is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cronScheduler couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/cron-schedulers/{id}")
    public Mono<ResponseEntity<CronScheduler>> updateCronScheduler(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody CronScheduler cronScheduler
    ) throws URISyntaxException {
        log.debug("REST request to update CronScheduler : {}, {}", id, cronScheduler);
        if (cronScheduler.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cronScheduler.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return cronSchedulerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return cronSchedulerService
                    .update(cronScheduler)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /cron-schedulers/:id} : Partial updates given fields of an existing cronScheduler, field will ignore if it is null
     *
     * @param id the id of the cronScheduler to save.
     * @param cronScheduler the cronScheduler to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cronScheduler,
     * or with status {@code 400 (Bad Request)} if the cronScheduler is not valid,
     * or with status {@code 404 (Not Found)} if the cronScheduler is not found,
     * or with status {@code 500 (Internal Server Error)} if the cronScheduler couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/cron-schedulers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<CronScheduler>> partialUpdateCronScheduler(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody CronScheduler cronScheduler
    ) throws URISyntaxException {
        log.debug("REST request to partial update CronScheduler partially : {}, {}", id, cronScheduler);
        if (cronScheduler.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cronScheduler.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return cronSchedulerRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<CronScheduler> result = cronSchedulerService.partialUpdate(cronScheduler);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, res.getId()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /cron-schedulers} : get all the cronSchedulers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cronSchedulers in body.
     */
    @GetMapping("/cron-schedulers")
    public Mono<List<CronScheduler>> getAllCronSchedulers() {
        log.debug("REST request to get all CronSchedulers");
        return cronSchedulerService.findAll().collectList();
    }

    /**
     * {@code GET  /cron-schedulers} : get all the cronSchedulers as a stream.
     * @return the {@link Flux} of cronSchedulers.
     */
    @GetMapping(value = "/cron-schedulers", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<CronScheduler> getAllCronSchedulersAsStream() {
        log.debug("REST request to get all CronSchedulers as a stream");
        return cronSchedulerService.findAll();
    }

    /**
     * {@code GET  /cron-schedulers/:id} : get the "id" cronScheduler.
     *
     * @param id the id of the cronScheduler to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cronScheduler, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/cron-schedulers/{id}")
    public Mono<ResponseEntity<CronScheduler>> getCronScheduler(@PathVariable String id) {
        log.debug("REST request to get CronScheduler : {}", id);
        Mono<CronScheduler> cronScheduler = cronSchedulerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cronScheduler);
    }

    /**
     * {@code DELETE  /cron-schedulers/:id} : delete the "id" cronScheduler.
     *
     * @param id the id of the cronScheduler to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/cron-schedulers/{id}")
    public Mono<ResponseEntity<Void>> deleteCronScheduler(@PathVariable String id) {
        log.debug("REST request to delete CronScheduler : {}", id);
        return cronSchedulerService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id))
                        .build()
                )
            );
    }
}

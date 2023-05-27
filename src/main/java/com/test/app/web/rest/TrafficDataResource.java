package com.test.app.web.rest;

import com.test.app.domain.TrafficData;
import com.test.app.repository.TrafficDataRepository;
import com.test.app.service.TrafficDataService;
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
 * REST controller for managing {@link com.test.app.domain.TrafficData}.
 */
@RestController
@RequestMapping("/api")
public class TrafficDataResource {

    private final Logger log = LoggerFactory.getLogger(TrafficDataResource.class);

    private static final String ENTITY_NAME = "testTrafficData";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TrafficDataService trafficDataService;

    private final TrafficDataRepository trafficDataRepository;

    public TrafficDataResource(TrafficDataService trafficDataService, TrafficDataRepository trafficDataRepository) {
        this.trafficDataService = trafficDataService;
        this.trafficDataRepository = trafficDataRepository;
    }

    /**
     * {@code POST  /traffic-data} : Create a new trafficData.
     *
     * @param trafficData the trafficData to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new trafficData, or with status {@code 400 (Bad Request)} if the trafficData has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/traffic-data")
    public Mono<ResponseEntity<TrafficData>> createTrafficData(@Valid @RequestBody TrafficData trafficData) throws URISyntaxException {
        log.debug("REST request to save TrafficData : {}", trafficData);
        if (trafficData.getId() != null) {
            throw new BadRequestAlertException("A new trafficData cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return trafficDataService
            .save(trafficData)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/traffic-data/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /traffic-data/:id} : Updates an existing trafficData.
     *
     * @param id the id of the trafficData to save.
     * @param trafficData the trafficData to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated trafficData,
     * or with status {@code 400 (Bad Request)} if the trafficData is not valid,
     * or with status {@code 500 (Internal Server Error)} if the trafficData couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/traffic-data/{id}")
    public Mono<ResponseEntity<TrafficData>> updateTrafficData(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody TrafficData trafficData
    ) throws URISyntaxException {
        log.debug("REST request to update TrafficData : {}, {}", id, trafficData);
        if (trafficData.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, trafficData.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return trafficDataRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return trafficDataService
                    .update(trafficData)
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
     * {@code PATCH  /traffic-data/:id} : Partial updates given fields of an existing trafficData, field will ignore if it is null
     *
     * @param id the id of the trafficData to save.
     * @param trafficData the trafficData to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated trafficData,
     * or with status {@code 400 (Bad Request)} if the trafficData is not valid,
     * or with status {@code 404 (Not Found)} if the trafficData is not found,
     * or with status {@code 500 (Internal Server Error)} if the trafficData couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/traffic-data/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<TrafficData>> partialUpdateTrafficData(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody TrafficData trafficData
    ) throws URISyntaxException {
        log.debug("REST request to partial update TrafficData partially : {}, {}", id, trafficData);
        if (trafficData.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, trafficData.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return trafficDataRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<TrafficData> result = trafficDataService.partialUpdate(trafficData);

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
     * {@code GET  /traffic-data} : get all the trafficData.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of trafficData in body.
     */
    @GetMapping("/traffic-data")
    public Mono<List<TrafficData>> getAllTrafficData() {
        log.debug("REST request to get all TrafficData");
        return trafficDataService.findAll().collectList();
    }

    /**
     * {@code GET  /traffic-data} : get all the trafficData as a stream.
     * @return the {@link Flux} of trafficData.
     */
    @GetMapping(value = "/traffic-data", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<TrafficData> getAllTrafficDataAsStream() {
        log.debug("REST request to get all TrafficData as a stream");
        return trafficDataService.findAll();
    }

    /**
     * {@code GET  /traffic-data/:id} : get the "id" trafficData.
     *
     * @param id the id of the trafficData to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the trafficData, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/traffic-data/{id}")
    public Mono<ResponseEntity<TrafficData>> getTrafficData(@PathVariable String id) {
        log.debug("REST request to get TrafficData : {}", id);
        Mono<TrafficData> trafficData = trafficDataService.findOne(id);
        return ResponseUtil.wrapOrNotFound(trafficData);
    }

    /**
     * {@code DELETE  /traffic-data/:id} : delete the "id" trafficData.
     *
     * @param id the id of the trafficData to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/traffic-data/{id}")
    public Mono<ResponseEntity<Void>> deleteTrafficData(@PathVariable String id) {
        log.debug("REST request to delete TrafficData : {}", id);
        return trafficDataService
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

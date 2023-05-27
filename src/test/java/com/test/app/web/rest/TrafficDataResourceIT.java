package com.test.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.test.app.IntegrationTest;
import com.test.app.domain.TrafficData;
import com.test.app.repository.TrafficDataRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link TrafficDataResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class TrafficDataResourceIT {

    private static final String DEFAULT_RANK = "AAAAAAAAAA";
    private static final String UPDATED_RANK = "BBBBBBBBBB";

    private static final Integer DEFAULT_IMPRESSIONS = 1;
    private static final Integer UPDATED_IMPRESSIONS = 2;

    private static final Integer DEFAULT_CLICKS = 1;
    private static final Integer UPDATED_CLICKS = 2;

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String ENTITY_API_URL = "/api/traffic-data";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private TrafficDataRepository trafficDataRepository;

    @Autowired
    private WebTestClient webTestClient;

    private TrafficData trafficData;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TrafficData createEntity() {
        TrafficData trafficData = new TrafficData()
            .rank(DEFAULT_RANK)
            .impressions(DEFAULT_IMPRESSIONS)
            .clicks(DEFAULT_CLICKS)
            .date(DEFAULT_DATE);
        return trafficData;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TrafficData createUpdatedEntity() {
        TrafficData trafficData = new TrafficData()
            .rank(UPDATED_RANK)
            .impressions(UPDATED_IMPRESSIONS)
            .clicks(UPDATED_CLICKS)
            .date(UPDATED_DATE);
        return trafficData;
    }

    @BeforeEach
    public void initTest() {
        trafficDataRepository.deleteAll().block();
        trafficData = createEntity();
    }

    @Test
    void createTrafficData() throws Exception {
        int databaseSizeBeforeCreate = trafficDataRepository.findAll().collectList().block().size();
        // Create the TrafficData
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeCreate + 1);
        TrafficData testTrafficData = trafficDataList.get(trafficDataList.size() - 1);
        assertThat(testTrafficData.getRank()).isEqualTo(DEFAULT_RANK);
        assertThat(testTrafficData.getImpressions()).isEqualTo(DEFAULT_IMPRESSIONS);
        assertThat(testTrafficData.getClicks()).isEqualTo(DEFAULT_CLICKS);
        assertThat(testTrafficData.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    void createTrafficDataWithExistingId() throws Exception {
        // Create the TrafficData with an existing ID
        trafficData.setId("existing_id");

        int databaseSizeBeforeCreate = trafficDataRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkClicksIsRequired() throws Exception {
        int databaseSizeBeforeTest = trafficDataRepository.findAll().collectList().block().size();
        // set the field null
        trafficData.setClicks(null);

        // Create the TrafficData, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllTrafficDataAsStream() {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        List<TrafficData> trafficDataList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(TrafficData.class)
            .getResponseBody()
            .filter(trafficData::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(trafficDataList).isNotNull();
        assertThat(trafficDataList).hasSize(1);
        TrafficData testTrafficData = trafficDataList.get(0);
        assertThat(testTrafficData.getRank()).isEqualTo(DEFAULT_RANK);
        assertThat(testTrafficData.getImpressions()).isEqualTo(DEFAULT_IMPRESSIONS);
        assertThat(testTrafficData.getClicks()).isEqualTo(DEFAULT_CLICKS);
        assertThat(testTrafficData.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    void getAllTrafficData() {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        // Get all the trafficDataList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(trafficData.getId()))
            .jsonPath("$.[*].rank")
            .value(hasItem(DEFAULT_RANK))
            .jsonPath("$.[*].impressions")
            .value(hasItem(DEFAULT_IMPRESSIONS))
            .jsonPath("$.[*].clicks")
            .value(hasItem(DEFAULT_CLICKS))
            .jsonPath("$.[*].date")
            .value(hasItem(DEFAULT_DATE.toString()));
    }

    @Test
    void getTrafficData() {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        // Get the trafficData
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, trafficData.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(trafficData.getId()))
            .jsonPath("$.rank")
            .value(is(DEFAULT_RANK))
            .jsonPath("$.impressions")
            .value(is(DEFAULT_IMPRESSIONS))
            .jsonPath("$.clicks")
            .value(is(DEFAULT_CLICKS))
            .jsonPath("$.date")
            .value(is(DEFAULT_DATE.toString()));
    }

    @Test
    void getNonExistingTrafficData() {
        // Get the trafficData
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingTrafficData() throws Exception {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();

        // Update the trafficData
        TrafficData updatedTrafficData = trafficDataRepository.findById(trafficData.getId()).block();
        updatedTrafficData.rank(UPDATED_RANK).impressions(UPDATED_IMPRESSIONS).clicks(UPDATED_CLICKS).date(UPDATED_DATE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedTrafficData.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedTrafficData))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
        TrafficData testTrafficData = trafficDataList.get(trafficDataList.size() - 1);
        assertThat(testTrafficData.getRank()).isEqualTo(UPDATED_RANK);
        assertThat(testTrafficData.getImpressions()).isEqualTo(UPDATED_IMPRESSIONS);
        assertThat(testTrafficData.getClicks()).isEqualTo(UPDATED_CLICKS);
        assertThat(testTrafficData.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    void putNonExistingTrafficData() throws Exception {
        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();
        trafficData.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, trafficData.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchTrafficData() throws Exception {
        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();
        trafficData.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamTrafficData() throws Exception {
        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();
        trafficData.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateTrafficDataWithPatch() throws Exception {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();

        // Update the trafficData using partial update
        TrafficData partialUpdatedTrafficData = new TrafficData();
        partialUpdatedTrafficData.setId(trafficData.getId());

        partialUpdatedTrafficData.rank(UPDATED_RANK).impressions(UPDATED_IMPRESSIONS).clicks(UPDATED_CLICKS);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTrafficData.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTrafficData))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
        TrafficData testTrafficData = trafficDataList.get(trafficDataList.size() - 1);
        assertThat(testTrafficData.getRank()).isEqualTo(UPDATED_RANK);
        assertThat(testTrafficData.getImpressions()).isEqualTo(UPDATED_IMPRESSIONS);
        assertThat(testTrafficData.getClicks()).isEqualTo(UPDATED_CLICKS);
        assertThat(testTrafficData.getDate()).isEqualTo(DEFAULT_DATE);
    }

    @Test
    void fullUpdateTrafficDataWithPatch() throws Exception {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();

        // Update the trafficData using partial update
        TrafficData partialUpdatedTrafficData = new TrafficData();
        partialUpdatedTrafficData.setId(trafficData.getId());

        partialUpdatedTrafficData.rank(UPDATED_RANK).impressions(UPDATED_IMPRESSIONS).clicks(UPDATED_CLICKS).date(UPDATED_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTrafficData.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTrafficData))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
        TrafficData testTrafficData = trafficDataList.get(trafficDataList.size() - 1);
        assertThat(testTrafficData.getRank()).isEqualTo(UPDATED_RANK);
        assertThat(testTrafficData.getImpressions()).isEqualTo(UPDATED_IMPRESSIONS);
        assertThat(testTrafficData.getClicks()).isEqualTo(UPDATED_CLICKS);
        assertThat(testTrafficData.getDate()).isEqualTo(UPDATED_DATE);
    }

    @Test
    void patchNonExistingTrafficData() throws Exception {
        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();
        trafficData.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, trafficData.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchTrafficData() throws Exception {
        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();
        trafficData.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamTrafficData() throws Exception {
        int databaseSizeBeforeUpdate = trafficDataRepository.findAll().collectList().block().size();
        trafficData.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(trafficData))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TrafficData in the database
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteTrafficData() {
        // Initialize the database
        trafficDataRepository.save(trafficData).block();

        int databaseSizeBeforeDelete = trafficDataRepository.findAll().collectList().block().size();

        // Delete the trafficData
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, trafficData.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<TrafficData> trafficDataList = trafficDataRepository.findAll().collectList().block();
        assertThat(trafficDataList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

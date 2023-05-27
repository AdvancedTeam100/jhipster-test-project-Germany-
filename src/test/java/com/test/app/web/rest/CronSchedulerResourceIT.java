package com.test.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.test.app.IntegrationTest;
import com.test.app.domain.CronScheduler;
import com.test.app.repository.CronSchedulerRepository;
import java.time.Duration;
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
 * Integration tests for the {@link CronSchedulerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CronSchedulerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CRON_EXPRESSION = "AAAAAAAAAA";
    private static final String UPDATED_CRON_EXPRESSION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/cron-schedulers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private CronSchedulerRepository cronSchedulerRepository;

    @Autowired
    private WebTestClient webTestClient;

    private CronScheduler cronScheduler;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CronScheduler createEntity() {
        CronScheduler cronScheduler = new CronScheduler().name(DEFAULT_NAME).cronExpression(DEFAULT_CRON_EXPRESSION);
        return cronScheduler;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CronScheduler createUpdatedEntity() {
        CronScheduler cronScheduler = new CronScheduler().name(UPDATED_NAME).cronExpression(UPDATED_CRON_EXPRESSION);
        return cronScheduler;
    }

    @BeforeEach
    public void initTest() {
        cronSchedulerRepository.deleteAll().block();
        cronScheduler = createEntity();
    }

    @Test
    void createCronScheduler() throws Exception {
        int databaseSizeBeforeCreate = cronSchedulerRepository.findAll().collectList().block().size();
        // Create the CronScheduler
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeCreate + 1);
        CronScheduler testCronScheduler = cronSchedulerList.get(cronSchedulerList.size() - 1);
        assertThat(testCronScheduler.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCronScheduler.getCronExpression()).isEqualTo(DEFAULT_CRON_EXPRESSION);
    }

    @Test
    void createCronSchedulerWithExistingId() throws Exception {
        // Create the CronScheduler with an existing ID
        cronScheduler.setId("existing_id");

        int databaseSizeBeforeCreate = cronSchedulerRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = cronSchedulerRepository.findAll().collectList().block().size();
        // set the field null
        cronScheduler.setName(null);

        // Create the CronScheduler, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllCronSchedulersAsStream() {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        List<CronScheduler> cronSchedulerList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(CronScheduler.class)
            .getResponseBody()
            .filter(cronScheduler::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(cronSchedulerList).isNotNull();
        assertThat(cronSchedulerList).hasSize(1);
        CronScheduler testCronScheduler = cronSchedulerList.get(0);
        assertThat(testCronScheduler.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCronScheduler.getCronExpression()).isEqualTo(DEFAULT_CRON_EXPRESSION);
    }

    @Test
    void getAllCronSchedulers() {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        // Get all the cronSchedulerList
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
            .value(hasItem(cronScheduler.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].cronExpression")
            .value(hasItem(DEFAULT_CRON_EXPRESSION));
    }

    @Test
    void getCronScheduler() {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        // Get the cronScheduler
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, cronScheduler.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(cronScheduler.getId()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.cronExpression")
            .value(is(DEFAULT_CRON_EXPRESSION));
    }

    @Test
    void getNonExistingCronScheduler() {
        // Get the cronScheduler
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingCronScheduler() throws Exception {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();

        // Update the cronScheduler
        CronScheduler updatedCronScheduler = cronSchedulerRepository.findById(cronScheduler.getId()).block();
        updatedCronScheduler.name(UPDATED_NAME).cronExpression(UPDATED_CRON_EXPRESSION);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedCronScheduler.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedCronScheduler))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
        CronScheduler testCronScheduler = cronSchedulerList.get(cronSchedulerList.size() - 1);
        assertThat(testCronScheduler.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCronScheduler.getCronExpression()).isEqualTo(UPDATED_CRON_EXPRESSION);
    }

    @Test
    void putNonExistingCronScheduler() throws Exception {
        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();
        cronScheduler.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, cronScheduler.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCronScheduler() throws Exception {
        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();
        cronScheduler.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCronScheduler() throws Exception {
        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();
        cronScheduler.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCronSchedulerWithPatch() throws Exception {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();

        // Update the cronScheduler using partial update
        CronScheduler partialUpdatedCronScheduler = new CronScheduler();
        partialUpdatedCronScheduler.setId(cronScheduler.getId());

        partialUpdatedCronScheduler.cronExpression(UPDATED_CRON_EXPRESSION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCronScheduler.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCronScheduler))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
        CronScheduler testCronScheduler = cronSchedulerList.get(cronSchedulerList.size() - 1);
        assertThat(testCronScheduler.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCronScheduler.getCronExpression()).isEqualTo(UPDATED_CRON_EXPRESSION);
    }

    @Test
    void fullUpdateCronSchedulerWithPatch() throws Exception {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();

        // Update the cronScheduler using partial update
        CronScheduler partialUpdatedCronScheduler = new CronScheduler();
        partialUpdatedCronScheduler.setId(cronScheduler.getId());

        partialUpdatedCronScheduler.name(UPDATED_NAME).cronExpression(UPDATED_CRON_EXPRESSION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCronScheduler.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCronScheduler))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
        CronScheduler testCronScheduler = cronSchedulerList.get(cronSchedulerList.size() - 1);
        assertThat(testCronScheduler.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCronScheduler.getCronExpression()).isEqualTo(UPDATED_CRON_EXPRESSION);
    }

    @Test
    void patchNonExistingCronScheduler() throws Exception {
        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();
        cronScheduler.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, cronScheduler.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCronScheduler() throws Exception {
        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();
        cronScheduler.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCronScheduler() throws Exception {
        int databaseSizeBeforeUpdate = cronSchedulerRepository.findAll().collectList().block().size();
        cronScheduler.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(cronScheduler))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the CronScheduler in the database
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCronScheduler() {
        // Initialize the database
        cronSchedulerRepository.save(cronScheduler).block();

        int databaseSizeBeforeDelete = cronSchedulerRepository.findAll().collectList().block().size();

        // Delete the cronScheduler
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, cronScheduler.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<CronScheduler> cronSchedulerList = cronSchedulerRepository.findAll().collectList().block();
        assertThat(cronSchedulerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

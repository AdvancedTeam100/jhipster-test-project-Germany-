package com.test.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.test.app.IntegrationTest;
import com.test.app.domain.User;
import com.test.app.repository.UserRepository;
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
 * Integration tests for the {@link UserResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class UserResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_PASSWORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSWORD = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/users";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;

    private User user;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static User createEntity() {
        User user = new User().name(DEFAULT_NAME).email(DEFAULT_EMAIL).password(DEFAULT_PASSWORD);
        return user;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static User createUpdatedEntity() {
        User user = new User().name(UPDATED_NAME).email(UPDATED_EMAIL).password(UPDATED_PASSWORD);
        return user;
    }

    @BeforeEach
    public void initTest() {
        userRepository.deleteAll().block();
        user = createEntity();
    }

    @Test
    void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().collectList().block().size();
        // Create the User
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getPassword()).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test
    void createUserWithExistingId() throws Exception {
        // Create the User with an existing ID
        user.setId("existing_id");

        int databaseSizeBeforeCreate = userRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = userRepository.findAll().collectList().block().size();
        // set the field null
        user.setName(null);

        // Create the User, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllUsersAsStream() {
        // Initialize the database
        userRepository.save(user).block();

        List<User> userList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(User.class)
            .getResponseBody()
            .filter(user::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(userList).isNotNull();
        assertThat(userList).hasSize(1);
        User testUser = userList.get(0);
        assertThat(testUser.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getPassword()).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test
    void getAllUsers() {
        // Initialize the database
        userRepository.save(user).block();

        // Get all the userList
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
            .value(hasItem(user.getId()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].password")
            .value(hasItem(DEFAULT_PASSWORD));
    }

    @Test
    void getUser() {
        // Initialize the database
        userRepository.save(user).block();

        // Get the user
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, user.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(user.getId()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL))
            .jsonPath("$.password")
            .value(is(DEFAULT_PASSWORD));
    }

    @Test
    void getNonExistingUser() {
        // Get the user
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingUser() throws Exception {
        // Initialize the database
        userRepository.save(user).block();

        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).block();
        updatedUser.name(UPDATED_NAME).email(UPDATED_EMAIL).password(UPDATED_PASSWORD);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedUser))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getPassword()).isEqualTo(UPDATED_PASSWORD);
    }

    @Test
    void putNonExistingUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();
        user.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateUserWithPatch() throws Exception {
        // Initialize the database
        userRepository.save(user).block();

        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();

        // Update the user using partial update
        User partialUpdatedUser = new User();
        partialUpdatedUser.setId(user.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUser.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedUser))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getPassword()).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test
    void fullUpdateUserWithPatch() throws Exception {
        // Initialize the database
        userRepository.save(user).block();

        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();

        // Update the user using partial update
        User partialUpdatedUser = new User();
        partialUpdatedUser.setId(user.getId());

        partialUpdatedUser.name(UPDATED_NAME).email(UPDATED_EMAIL).password(UPDATED_PASSWORD);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedUser.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedUser))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testUser.getPassword()).isEqualTo(UPDATED_PASSWORD);
    }

    @Test
    void patchNonExistingUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();
        user.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, user.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamUser() throws Exception {
        int databaseSizeBeforeUpdate = userRepository.findAll().collectList().block().size();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(user))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the User in the database
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteUser() {
        // Initialize the database
        userRepository.save(user).block();

        int databaseSizeBeforeDelete = userRepository.findAll().collectList().block().size();

        // Delete the user
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, user.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<User> userList = userRepository.findAll().collectList().block();
        assertThat(userList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

package com.test.app.service;

import com.test.app.domain.User;
import com.test.app.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link User}.
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Save a user.
     *
     * @param user the entity to save.
     * @return the persisted entity.
     */
    public Mono<User> save(User user) {
        log.debug("Request to save User : {}", user);
        return userRepository.save(user);
    }

    /**
     * Update a user.
     *
     * @param user the entity to save.
     * @return the persisted entity.
     */
    public Mono<User> update(User user) {
        log.debug("Request to update User : {}", user);
        return userRepository.save(user);
    }

    /**
     * Partially update a user.
     *
     * @param user the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<User> partialUpdate(User user) {
        log.debug("Request to partially update User : {}", user);

        return userRepository
            .findById(user.getId())
            .map(existingUser -> {
                if (user.getName() != null) {
                    existingUser.setName(user.getName());
                }
                if (user.getEmail() != null) {
                    existingUser.setEmail(user.getEmail());
                }
                if (user.getPassword() != null) {
                    existingUser.setPassword(user.getPassword());
                }

                return existingUser;
            })
            .flatMap(userRepository::save);
    }

    /**
     * Get all the users.
     *
     * @return the list of entities.
     */
    public Flux<User> findAll() {
        log.debug("Request to get all Users");
        return userRepository.findAll();
    }

    /**
     * Returns the number of users available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return userRepository.count();
    }

    /**
     * Get one user by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Mono<User> findOne(String id) {
        log.debug("Request to get User : {}", id);
        return userRepository.findById(id);
    }

    /**
     * Delete the user by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(String id) {
        log.debug("Request to delete User : {}", id);
        return userRepository.deleteById(id);
    }
}

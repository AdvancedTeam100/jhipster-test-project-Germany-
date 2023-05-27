package com.test.app.repository;

import com.test.app.domain.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the User entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {}

package com.attendease.backend.repository.users;

import com.attendease.backend.model.users.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<Users, String> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Users> findAllByAccountStatus(String accountStatus);
}

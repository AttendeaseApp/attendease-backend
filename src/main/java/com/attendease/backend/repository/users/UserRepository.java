package com.attendease.backend.repository.users;

import com.attendease.backend.domain.users.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<Users, String> {
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Users> findAllByAccountStatus(String accountStatus);
}

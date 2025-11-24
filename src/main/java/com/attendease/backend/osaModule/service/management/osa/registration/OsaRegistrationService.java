package com.attendease.backend.osaModule.service.management.osa.registration;

import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsaRegistrationService {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public String registerNewOsaAccount(Users user) {
        usersRepository
            .findByEmail(user.getEmail())
            .ifPresent(existingUser -> {
                log.warn("WARNING: An account with this email already exists.");
                throw new IllegalArgumentException("An account with this email already exists.");
            });

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users savedUser = usersRepository.save(user);
        log.info("Successfully registered new OSA account with id: {}", savedUser.getUserId());

        return "Added OSA with id: " + savedUser.getUserId();
    }
}

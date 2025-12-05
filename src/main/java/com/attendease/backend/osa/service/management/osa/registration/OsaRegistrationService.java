package com.attendease.backend.osaModule.service.management.osa.registration;

import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.users.OSA.Registration.Request.OsaRegistrationRequest;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.validation.UserValidator;
import jakarta.validation.Valid;
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
    private final UserValidator userValidator;

    public String registerNewOsaAccount(OsaRegistrationRequest request) {
        userValidator.validateFirstName(request.getFirstName(), "First name");
        userValidator.validateLastName(request.getLastName(), "Last name");
        userValidator.validatePassword(request.getPassword());
        userValidator.validateEmail(request.getEmail());
        userValidator.validateContactNumber(request.getContactNumber());
        usersRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    log.warn("WARNING: An account with this email already exists.");
                    throw new IllegalArgumentException("An account with this email already exists.");
                });

        Users newUser = Users.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .userType(UserType.OSA)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Users savedUser = usersRepository.save(newUser);
        log.info("Successfully registered new OSA account with id: {}", savedUser.getUserId());

        return "Added OSA with id: " + savedUser.getUserId();
    }
}

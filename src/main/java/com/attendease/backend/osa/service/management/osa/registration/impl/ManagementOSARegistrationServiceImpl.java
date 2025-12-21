package com.attendease.backend.osa.service.management.osa.registration.impl;

import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.user.account.osa.registration.UserAccountOsaRegistrationRequest;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.management.osa.registration.ManagementOSARegistrationService;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagementOSARegistrationServiceImpl implements ManagementOSARegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    public String registerNewOsaAccount(UserAccountOsaRegistrationRequest request) {
        userValidator.validateFirstName(request.getFirstName(), "First name");
        userValidator.validateLastName(request.getLastName(), "Last name");
        userValidator.validatePassword(request.getPassword());
        userValidator.validateEmail(request.getEmail());
        userValidator.validateContactNumber(request.getContactNumber());
        userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    log.warn("WARNING: An account with this email already exists.");
                    throw new IllegalArgumentException("An account with this email already exists.");
                });

        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .userType(UserType.OSA)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Successfully registered new osa account with id: {}", savedUser.getUserId());

        return "Added osa with id: " + savedUser.getUserId();
    }
}

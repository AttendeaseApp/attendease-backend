package com.attendease.backend.authentication.osa.service;

import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.security.JwtTokenizationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OsaAuthenticationService {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizationUtil jwtTokenizationUtil;

    public OsaAuthenticationService(UserRepository usersRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtTokenizationUtil jwtTokenizationUtil) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizationUtil = jwtTokenizationUtil;
    }

    public String registerNewOsaAccount(Users user) {
        usersRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            log.warn("WARNING: An account with this email already exists.");
            throw new IllegalArgumentException("An account with this email already exists.");
        });

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users savedUser = usersRepository.save(user);
        log.info("Successfully registered new OSA account with id: {}", savedUser.getUserId());

        return "Added OSA with id: " + savedUser.getUserId();
    }

    public String loginOsa(String email, String password) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());
    }
}

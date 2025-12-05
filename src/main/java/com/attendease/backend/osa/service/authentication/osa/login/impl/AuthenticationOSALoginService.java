package com.attendease.backend.osa.service.authentication.osa.login.impl;

import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.security.JwtTokenizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationOSALoginService implements com.attendease.backend.osa.service.authentication.osa.login.AuthenticationOSALoginService {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizationUtil jwtTokenizationUtil;

    @Override
    public String loginOSA(String email, String password) {
        Users user = usersRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());
    }
}

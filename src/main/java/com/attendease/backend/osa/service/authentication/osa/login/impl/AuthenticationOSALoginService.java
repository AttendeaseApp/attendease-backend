package com.attendease.backend.osa.service.authentication.osa.login.impl;

import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationOSALoginService implements com.attendease.backend.osa.service.authentication.osa.login.AuthenticationOSALoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String loginOSA(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getUserType());
    }
}

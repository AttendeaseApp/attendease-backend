package com.attendease.backend.osaModule.service.authentication.login;

import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.security.JwtTokenizationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OsaLoginService {

    private final UserRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizationUtil jwtTokenizationUtil;

    public String loginOsa(String email, String password) {
        Users user = usersRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());
    }
}

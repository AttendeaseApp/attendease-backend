package com.attendease.backend.student.service.authentication.login.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.student.login.LoginResponse;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.security.JwtTokenProvider;
import java.util.Optional;

import com.attendease.backend.student.service.authentication.login.AuthenticationLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationLoginServiceImpl implements AuthenticationLoginService {

    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BiometricsRepository biometricsRepository;

    @Override
    public LoginResponse loginStudent(String studentNumber, String password) {
        try {
            Students student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Invalid student number or password"));

            User user = student.getUser();

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return LoginResponse.builder()
                        .success(false)
                        .requiresFacialRegistration(false)
                        .message("Invalid student number or password")
                        .build();
            }

            Optional<BiometricData> biometricData = biometricsRepository.findByStudentNumber(studentNumber);
            boolean requiresFacialRegistration = biometricData.isEmpty();

            String token = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getUserType());

            return LoginResponse.builder()
                    .success(true)
                    .token(token)
                    .studentNumber(studentNumber)
                    .requiresFacialRegistration(requiresFacialRegistration)
                    .message(requiresFacialRegistration
                            ? "login successful. Please complete facial registration."
                            : "login successful.")
                    .build();

        } catch (IllegalArgumentException e) {
            return LoginResponse.builder()
                    .success(false)
                    .requiresFacialRegistration(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            return LoginResponse.builder()
                    .success(false)
                    .requiresFacialRegistration(false)
                    .message("An unexpected error occurred. Please try again.")
                    .build();
        }
    }
}

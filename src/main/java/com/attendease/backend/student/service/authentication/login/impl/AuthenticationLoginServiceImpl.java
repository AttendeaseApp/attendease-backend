package com.attendease.backend.student.service.authentication.login.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
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
    public LoginResult loginStudent(String studentNumber, String password) {
        try {
            Students student = studentRepository.findByStudentNumber(studentNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid student number. Please try again."));

            User user = student.getUser();

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return new LoginResult("Invalid password, try again with different one.", null);
            }

            Optional<BiometricData> biometricData = biometricsRepository.findByStudentNumber(studentNumber);
            boolean requiresFacialRegistration = biometricData.isEmpty();

            String token = jwtTokenProvider.generateStudentToken(
                    user.getUserId(),
                    student.getStudentNumber(),
                    user.getUserType(),
                    requiresFacialRegistration
            );

            String message = requiresFacialRegistration ?
                    "Login successful. Please complete facial registration." : "Login successful.";

            return new LoginResult(message, token);
        } catch (IllegalArgumentException e) {
            return new LoginResult(e.getMessage(), null);
        } catch (Exception e) {
            return new LoginResult("An unexpected error occurred while logging in. Please try again.", null);
        }
    }

    public record LoginResult(String message, String token) {}
}

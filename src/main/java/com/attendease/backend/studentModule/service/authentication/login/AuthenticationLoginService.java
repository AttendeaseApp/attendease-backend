package com.attendease.backend.studentModule.service.authentication.login;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.students.Login.Response.LoginResponse;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.security.JwtTokenizationUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationLoginService {

    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final JwtTokenizationUtil jwtTokenizationUtil;
    private final BiometricsRepository biometricsRepository;

    public LoginResponse loginStudent(String studentNumber, String password) {
        try {
            Students student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Invalid student number or password"));

            Users user = student.getUser();

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return LoginResponse.builder()
                        .success(false)
                        .requiresFacialRegistration(false)
                        .message("Invalid student number or password")
                        .build();
            }

            Optional<BiometricData> biometricData = biometricsRepository.findByStudentNumber(studentNumber);
            boolean requiresFacialRegistration = biometricData.isEmpty();

            String token = jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());

            return LoginResponse.builder()
                    .success(true)
                    .token(token)
                    .studentNumber(studentNumber)
                    .requiresFacialRegistration(requiresFacialRegistration)
                    .message(requiresFacialRegistration
                            ? "Login successful. Please complete facial registration."
                            : "Login successful.")
                    .build();

        } catch (IllegalArgumentException e) {
            return LoginResponse.builder()
                    .success(false)
                    .requiresFacialRegistration(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected login error for studentNumber {}: {}", studentNumber, e.getMessage(), e);
            return LoginResponse.builder()
                    .success(false)
                    .requiresFacialRegistration(false)
                    .message("An unexpected error occurred. Please try again.")
                    .build();
        }
    }
}

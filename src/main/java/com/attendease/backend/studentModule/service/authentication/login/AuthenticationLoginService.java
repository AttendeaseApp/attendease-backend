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
        Students student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Invalid student number or password"));

        Users user = student.getUser();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid student number or password");
        }

        Optional<BiometricData> biometricData = biometricsRepository.findByStudentNumber(studentNumber);
        boolean requiresFacialRegistration = biometricData.isEmpty();

        String token = jwtTokenizationUtil.generateToken(user.getUserId(), user.getEmail(), user.getUserType());

        log.info("Student login successful. StudentNumber: {}, Requires Facial Registration: {}", studentNumber, requiresFacialRegistration);

        return LoginResponse.builder()
            .token(token)
            .requiresFacialRegistration(requiresFacialRegistration)
            .message(requiresFacialRegistration ? "Login successful. Please complete facial registration." : "Login successful.")
            .studentNumber(studentNumber)
            .build();
    }
}

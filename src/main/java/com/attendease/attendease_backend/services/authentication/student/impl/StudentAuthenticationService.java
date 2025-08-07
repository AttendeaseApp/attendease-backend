package com.attendease.attendease_backend.services.authentication.student.impl;

import com.attendease.attendease_backend.data.enums.AccountStatus;
import com.attendease.attendease_backend.data.enums.UserType;
import com.attendease.attendease_backend.data.student.Student;
import com.attendease.attendease_backend.data.user.User;
import com.attendease.attendease_backend.repository.authentication.student.StudentAuthenticationRepository;
import com.attendease.attendease_backend.services.authentication.student.StudentAuthenticationInterface;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;


@Service
@Slf4j
public class StudentAuthenticationService implements StudentAuthenticationInterface {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;
    private final StudentAuthenticationRepository studentAuthenticationRepository;

    public StudentAuthenticationService(Firestore firestore, FirebaseAuth firebaseAuth, PasswordEncoder passwordEncoder, StudentAuthenticationRepository studentAuthenticationRepository) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
        this.studentAuthenticationRepository = studentAuthenticationRepository;
    }


    @Override
    public String registerNewStudentAccount(Student student) throws Exception {
        try {
            if (studentAuthenticationRepository.existsByStudentNumber(student.getStudentNumber())) {
                throw new IllegalStateException("Student with number " + student.getStudentNumber() + " already exists");
            }

            User user = createUserFromStudent(student);
            studentAuthenticationRepository.saveWithTransaction(student, user);
            return "Student registered successfully";
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to register student: " + e.getMessage(), e);
        }
    }

    @Override
    public String loginStudent(Student loginRequest) throws ExecutionException, InterruptedException {
        QuerySnapshot studentSnapshot = firestore.collection("students")
                .whereEqualTo("studentNumber", loginRequest.getStudentNumber())
                .limit(1)
                .get()
                .get();

        if (studentSnapshot.isEmpty()) {
            log.warn("Login failed: Student with number {} not found.", loginRequest.getStudentNumber());
            throw new IllegalArgumentException("Invalid student number or password.");
        }

        Student student = studentSnapshot.getDocuments().getFirst().toObject(Student.class);

        DocumentReference userRef = student.getUserRefId();
        User user = userRef.get().get().toObject(User.class);
        if (user == null) {
            log.error("Failed to retrieve linked User document for student number {}.", loginRequest.getStudentNumber());
            throw new RuntimeException("An internal error occurred during login.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for student {}.", student.getStudentNumber());
            throw new IllegalArgumentException("Invalid student number or password.");
        }

        try {
            String customToken = firebaseAuth.createCustomToken(user.getUserId());
            log.info("Custom token generated successfully for student {}.", student.getStudentNumber());
            return customToken;
        } catch (FirebaseAuthException e) {
            log.error("Failed to create custom token for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Authentication failed, please try again later.");
        }
    }

    // helper methods
    private User createUserFromStudent(Student student) {
        User user = new User();
        String hashedPassword = passwordEncoder.encode(student.getPassword());
        user.setPassword(hashedPassword);

        user.setFirstName(student.getFirstName());
        user.setMiddleName(student.getMiddleName());
        user.setLastName(student.getLastName());
        user.setBirthdate(student.getBirthdate());
        user.setAddress(student.getAddress());
        user.setContactNumber(student.getContactNumber());
        user.setEmail(student.getEmail());

        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(UserType.SYSTEM);

        return user;
    }

}

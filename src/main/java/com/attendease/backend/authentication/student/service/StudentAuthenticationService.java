package com.attendease.backend.authentication.student.service;

import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.authentication.student.repository.StudentAuthenticationRepository;
import com.google.cloud.firestore.DocumentReference;
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
public class StudentAuthenticationService {

    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;
    private final StudentAuthenticationRepository studentRepository;

    public StudentAuthenticationService(Firestore firestore, FirebaseAuth firebaseAuth, PasswordEncoder passwordEncoder, StudentAuthenticationRepository studentRepository) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
    }

    public String registerNewStudentAccount(Students student) throws Exception {
        try {
            if (studentRepository.existsByStudentNumber(student.getStudentNumber())) {
                throw new IllegalStateException("Students with number " + student.getStudentNumber() + " already exists");
            }

            Users user = createUserFromStudent(student);
            studentRepository.saveWithTransaction(student, user);
            return "Students registered successfully";
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Failed to register students: " + e.getMessage(), e);
        }
    }

    public String loginStudent(Students loginRequest) throws ExecutionException, InterruptedException {
        QuerySnapshot studentSnapshot = firestore.collection("students")
                .whereEqualTo("studentNumber", loginRequest.getStudentNumber()).limit(1).get().get();

        if (studentSnapshot.isEmpty()) {
            log.warn("Login failed: Students with number {} not found.", loginRequest.getStudentNumber());
            throw new IllegalArgumentException("Invalid students number or password.");
        }

        Students student = studentSnapshot.getDocuments().getFirst().toObject(Students.class);

        DocumentReference userRef = student.getUserRefId();
        Users user = userRef.get().get().toObject(Users.class);
        if (user == null) {
            log.error("Failed to retrieve linked Users document for students number {}.", loginRequest.getStudentNumber());
            throw new RuntimeException("An internal error occurred during login.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for students {}.", student.getStudentNumber());
            throw new IllegalArgumentException("Invalid students number or password.");
        }

        try {
            String customToken = firebaseAuth.createCustomToken(user.getUserId());
            log.info("Custom token generated successfully for students {}.", student.getStudentNumber());
            return customToken;
        } catch (FirebaseAuthException e) {
            log.error("Failed to create custom token for users {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Authentication failed, please try again later.");
        }
    }

    // helper methods
    private Users createUserFromStudent(Students student) {
        Users user = new Users();
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

package com.attendease.attendease_backend.repository.authentication.student.impl;

import com.attendease.attendease_backend.data.enums.AccountStatus;
import com.attendease.attendease_backend.data.enums.UserType;
import com.attendease.attendease_backend.data.student.Student;
import com.attendease.attendease_backend.data.user.User;
import com.attendease.attendease_backend.repository.authentication.student.StudentAuthenticationRepository;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class StudentAuthenticationRepositoryImpl implements StudentAuthenticationRepository {

    private final Firestore firestore;

    public StudentAuthenticationRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Student findByCredentials(String studentNumber, String password) {
        return null;
    }

    @Override
    public void saveWithTransaction(Student student, User user) {
        try {
            ApiFuture<Student> transaction = firestore.runTransaction(transactionContext -> {
                DocumentReference studentRef = firestore.collection("students").document(student.getStudentNumber());
                DocumentReference userRef = firestore.collection("users").document();

                DocumentSnapshot studentSnapshot = transactionContext.get(studentRef).get();
                if (studentSnapshot.exists()) {
                    throw new IllegalStateException("Student with student number " + student.getStudentNumber() + " already exists.");
                }

                student.setUserId(userRef.getId());
                student.setUserRefId(userRef);

                student.setUserType(UserType.STUDENT);
                student.setAccountStatus(AccountStatus.ACTIVE);
                student.setUpdatedBy(UserType.SYSTEM);

                user.setUserId(userRef.getId());

                transactionContext.set(studentRef, student);
                transactionContext.set(userRef, user);

                log.info("Transaction completed: Student {} and User {} created successfully", student.getStudentNumber(), userRef.getId());

                return student;
            });

            transaction.get();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Transaction failed while creating student {}: {}", student.getStudentNumber(), e.getMessage(), e);
            if (e.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) e.getCause();
            }
            throw new RuntimeException("Failed to create student: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> findByStudentNumber(String studentNumber) {
        try {
            DocumentSnapshot document = firestore.collection("students")
                    .document(studentNumber)
                    .get()
                    .get();

            if (document.exists()) {
                Student student = document.toObject(Student.class);
                assert student != null;
                return Optional.of(student);
            }

            return Optional.empty();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find student by number {}: {}", studentNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve student", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    public boolean existsByStudentNumber(String studentNumber) {
        return false;
    }
}

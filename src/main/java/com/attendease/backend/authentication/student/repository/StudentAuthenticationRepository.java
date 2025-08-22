package com.attendease.backend.authentication.student.repository;

import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.userManagement.dto.UserWithStudentInfo;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Refactored StudentAuthenticationRepository that works with separate Users and Students entities.
 * Uses composition model instead of inheritance.
 */
@Slf4j
@Repository
public class StudentAuthenticationRepository {

    private final Firestore firestore;

    public StudentAuthenticationRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Checks if a student with the given student number exists.
     * @param studentNumber The student number to check.
     * @return true if a student with the student number exists, false otherwise.
     */
    public boolean existsByStudentNumber(String studentNumber) throws ExecutionException, InterruptedException {
        return !firestore.collection("students")
                .whereEqualTo("studentNumber", studentNumber)
                .limit(1)
                .get()
                .get()
                .isEmpty();
    }

    /**
     * Checks if a user with the given email exists.
     * @param email The email to check.
     * @return true if a user with the email exists, false otherwise.
     */
    public boolean existsByEmail(String email) throws ExecutionException, InterruptedException {
        return !firestore.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("userType", "STUDENT")
                .limit(1)
                .get()
                .get()
                .isEmpty();
    }

    /**
     * Saves a student to the students collection.
     * @param student The student to save.
     */
    public void saveStudent(Students student) throws ExecutionException, InterruptedException {
        if (student.getStudentNumber() == null || student.getStudentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required");
        }

        firestore.collection("students")
                .document(student.getStudentNumber())
                .set(student)
                .get();

        log.info("Student saved successfully: {}", student.getStudentNumber());
    }

    /**
     * Saves a user to the users collection.
     * @param user The user to save.
     */
    public void saveUser(Users user) throws ExecutionException, InterruptedException {
        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        firestore.collection("users")
                .document(user.getUserId())
                .set(user)
                .get();

        log.info("User saved successfully: {}", user.getUserId());
    }

    /**
     * Updates an existing user.
     * @param user The user to update.
     */
    public void updateUser(Users user) throws ExecutionException, InterruptedException {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for update");
        }

        firestore.collection("users")
                .document(user.getUserId())
                .set(user)
                .get();

        log.info("User updated successfully: {}", user.getUserId());
    }

    /**
     * Saves both student and user entities in a single transaction.
     * @param student The student entity to save.
     * @param user The user entity to save.
     * @return UserWithStudentInfo containing the saved entities.
     */
    public UserWithStudentInfo saveWithTransaction(Students student, Users user)
            throws ExecutionException, InterruptedException {

        ApiFuture<UserWithStudentInfo> transactionFuture = firestore.runTransaction(transaction -> {
            DocumentReference userRef = firestore.collection("users").document(user.getUserId());
            DocumentReference studentRef = firestore.collection("students").document(student.getStudentNumber());

            DocumentSnapshot existingStudent = transaction.get(studentRef).get();
            if (existingStudent.exists()) {
                throw new IllegalStateException("Student with student number " + student.getStudentNumber() + " already exists");
            }

            DocumentSnapshot existingUser = transaction.get(userRef).get();
            if (existingUser.exists()) {
                throw new IllegalStateException("User with ID " + user.getUserId() + " already exists");
            }

            // set user reference in student
            student.setUserRefId(userRef);

            transaction.set(userRef, user);
            transaction.set(studentRef, student);

            log.info("Transaction completed: Student {} and User {} created successfully", student.getStudentNumber(), user.getUserId());
            return new UserWithStudentInfo(user, student);
        });

        try {
            return transactionFuture.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) e.getCause();
            }
            log.error("Transaction failed while creating student {}: {}", student.getStudentNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create student: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Transaction interrupted while creating student {}: {}", student.getStudentNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create student: Operation was interrupted", e);
        }
    }

    /**
     * Retrieves all students with their associated user information.
     * @return List of UserWithStudentInfo containing all students and their user data.
     */
    public List<UserWithStudentInfo> retrieveAllStudentsWithUserInfo() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> studentsFuture = firestore.collection("students").get();
        List<QueryDocumentSnapshot> studentDocs = studentsFuture.get().getDocuments();

        if (studentDocs.isEmpty()) {
            log.info("No students found in database");
            return new ArrayList<>();
        }

        ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users")
                .whereEqualTo("userType", "STUDENT")
                .get();
        List<QueryDocumentSnapshot> userDocs = usersFuture.get().getDocuments();

        Map<String, Users> userMap = userDocs.stream()
                .map(doc -> doc.toObject(Users.class))
                .collect(Collectors.toMap(Users::getUserId, user -> user));

        List<UserWithStudentInfo> results = new ArrayList<>();

        for (QueryDocumentSnapshot studentDoc : studentDocs) {
            Students student = studentDoc.toObject(Students.class);
            student.logFields();

            Users user = null;
            if (student.getUserRefId() != null) {
                String userId = extractUserIdFromPath(student.getUserRefId().getPath());
                user = userMap.get(userId);
            }

            if (user != null) {
                results.add(new UserWithStudentInfo(user, student));
                log.debug("Student {} => {} with user info", studentDoc.getId(), student);
            } else {
                log.warn("Student {} has no associated user information", student.getStudentNumber());
            }
        }

        log.info("Retrieved {} students with user information", results.size());
        return results;
    }

    /**
     * Retrieves all students (without user information for backward compatibility).
     * @return List of Students.
     */
    public List<Students> retrieveAllStudents() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> studentList = firestore.collection("students").get();
        List<QueryDocumentSnapshot> documents = studentList.get().getDocuments();
        List<Students> students = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            Students student = document.toObject(Students.class);
            student.logFields();
            students.add(student);
            log.debug("Student {} => {}", document.getId(), student);
        }

        log.info("Retrieved {} students", students.size());
        return students;
    }

    /**
     * Finds a student by student number (without user information).
     * @param studentNumber The student number to search for.
     * @return Optional containing the student if found.
     */
    public Optional<Students> findByStudentNumber(String studentNumber) {
        try {
            DocumentSnapshot document = firestore.collection("students")
                    .document(studentNumber)
                    .get()
                    .get();

            if (document.exists()) {
                Students student = document.toObject(Students.class);
                log.debug("Found student by number {}: {}", studentNumber, student);
                assert student != null;
                return Optional.of(student);
            }

            log.debug("No student found with number: {}", studentNumber);
            return Optional.empty();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find student by number {}: {}", studentNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve student", e);
        }
    }

    /**
     * Finds a student by student number with associated user information.
     * @param studentNumber The student number to search for.
     * @return Optional containing UserWithStudentInfo if found.
     */
    public Optional<UserWithStudentInfo> findByStudentNumberWithUserInfo(String studentNumber) {
        try {
            DocumentSnapshot studentDoc = firestore.collection("students")
                    .document(studentNumber)
                    .get()
                    .get();

            if (!studentDoc.exists()) {
                log.debug("No student found with number: {}", studentNumber);
                return Optional.empty();
            }

            Students student = studentDoc.toObject(Students.class);
            if (student == null || student.getUserRefId() == null) {
                log.warn("Student {} exists but has no user reference", studentNumber);
                return Optional.empty();
            }

            DocumentSnapshot userDoc = student.getUserRefId().get().get();
            if (!userDoc.exists()) {
                log.warn("Student {} references non-existent user", studentNumber);
                return Optional.empty();
            }

            Users user = userDoc.toObject(Users.class);
            if (user == null) {
                log.warn("Failed to deserialize user for student {}", studentNumber);
                return Optional.empty();
            }

            log.debug("Found student with user info: {} (User ID: {})", studentNumber, user.getUserId());
            return Optional.of(new UserWithStudentInfo(user, student));

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find student with user info by number {}: {}", studentNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve student with user information", e);
        }
    }

//    /**
//     * Finds a user by user ID.
//     * @param userId The user ID to search for.
//     * @return Optional containing the user if found.
//     */
//    public Optional<Users> findUserById(String userId) throws ExecutionException, InterruptedException {
//        try {
//            DocumentSnapshot document = firestore.collection("users")
//                    .document(userId)
//                    .get()
//                    .get();
//
//            if (document.exists()) {
//                Users user = document.toObject(Users.class);
//                log.debug("Found user by ID {}: {}", userId, user);
//                assert user != null;
//                return Optional.of(user);
//            }
//
//            log.debug("No user found with ID: {}", userId);
//            return Optional.empty();
//
//        } catch (InterruptedException | ExecutionException e) {
//            log.error("Failed to find user by ID {}: {}", userId, e.getMessage(), e);
//            throw new RuntimeException("Failed to retrieve user", e);
//        }
//    }

    // Helper methods

    private String extractUserIdFromPath(String path) {
        String[] parts = path.split("/");
        return parts.length > 1 ? parts[parts.length - 1] : "";
    }

}
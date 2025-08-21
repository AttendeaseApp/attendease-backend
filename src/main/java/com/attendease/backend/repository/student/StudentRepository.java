package com.attendease.backend.repository.student;

import com.attendease.backend.data.model.biometrics.BiometricData;
import com.attendease.backend.data.model.enums.AccountStatus;
import com.attendease.backend.data.model.enums.UserType;
import com.attendease.backend.data.model.students.Students;
import com.attendease.backend.data.model.users.Users;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
public class StudentRepository {

    private final Firestore firestore;

    public StudentRepository(Firestore firestore) {
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
                .get()
                .get()
                .isEmpty();
    }

    /**
     * Saves a student to the students collection.
     * @param student The student to save.
     */
    public void saveStudent(Students student) throws ExecutionException, InterruptedException {
        firestore.collection("students")
                .document(student.getStudentNumber())
                .set(student)
                .get();
    }

    public void saveWithTransaction(Students student, Users user) {
        try {
            ApiFuture<Students> transaction = firestore.runTransaction(transactionContext -> {
                DocumentReference studentRef = firestore.collection("students").document(student.getStudentNumber());
                DocumentReference userRef = firestore.collection("users").document();

                DocumentSnapshot studentSnapshot = transactionContext.get(studentRef).get();
                if (studentSnapshot.exists()) {
                    throw new IllegalStateException("Students with students number " + student.getStudentNumber() + " already exists.");
                }

                student.setUserId(userRef.getId());
                student.setUserRefId(userRef);

                student.setUserType(UserType.STUDENT);
                student.setAccountStatus(AccountStatus.ACTIVE);
                student.setUpdatedBy(UserType.SYSTEM);

                user.setUserId(userRef.getId());

                transactionContext.set(studentRef, student);
                transactionContext.set(userRef, user);

                log.info("Transaction completed: Students {} and Users {} created successfully", student.getStudentNumber(), userRef.getId());

                return student;
            });

            transaction.get();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Transaction failed while creating students {}: {}", student.getStudentNumber(), e.getMessage(), e);
            if (e.getCause() instanceof IllegalStateException) {
                throw (IllegalStateException) e.getCause();
            }
            throw new RuntimeException("Failed to create students: " + e.getMessage(), e);
        }
    }


    // data retrievals

    public List<Students> retrieveAllStudents() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> studentList = firestore.collection("students").get();
        List<QueryDocumentSnapshot> documents = studentList.get().getDocuments();
        List<Students> students = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            Students student = document.toObject(Students.class);
            student.logFields();
            students.add(student);
            log.info("{} => {}", document.getId(), student);
        }
        return students;
    }

    public Optional<Students> findByStudentNumber(String studentNumber) {
        try {
            DocumentSnapshot document = firestore.collection("students").document(studentNumber).get().get();

            if (document.exists()) {
                Students student = document.toObject(Students.class);
                assert student != null;
                return Optional.of(student);
            }
            return Optional.empty();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to find students by number {}: {}", studentNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve students", e);
        }
    }
}

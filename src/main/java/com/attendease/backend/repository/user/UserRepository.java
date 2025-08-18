package com.attendease.backend.repository.user;

import com.attendease.backend.data.dto.users.UpdateUserInfoDTO;
import com.attendease.backend.data.dto.users.UserSearchDTO;
import com.attendease.backend.data.model.enums.AccountStatus;
import com.attendease.backend.data.model.enums.UserType;
import com.attendease.backend.data.model.students.Courses;
import com.attendease.backend.data.model.students.Students;
import com.attendease.backend.data.model.users.Users;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UserRepository {

    private final Firestore firestore;

    public UserRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void saveUser(Users user) throws ExecutionException, InterruptedException {
        firestore.collection("users")
                 .document(user.getUserId())
                 .set(user)
                 .get();
    }

    /**
     * retrieves all users including OSA AND STUDENTS
     * */
    public List<Users> retrieveAllUsers() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> userList = firestore.collection("users").get();
        List<QueryDocumentSnapshot> userDocuments = userList.get().getDocuments();
        List<Users> users = new ArrayList<>();

        ApiFuture<QuerySnapshot> studentList = firestore.collection("students").get();
        List<QueryDocumentSnapshot> studentDocuments = studentList.get().getDocuments();
        List<Students> students = new ArrayList<>();
        
        for (QueryDocumentSnapshot document : studentDocuments) {
            Students student = document.toObject(Students.class);
            student.logFields();
            students.add(student);
            log.info("Student {} => {}", document.getId(), student);
        }

        for (QueryDocumentSnapshot userDoc : userDocuments) {
            Users user = userDoc.toObject(Users.class);
            log.info("User {} => {}", userDoc.getId(), user);
            Users finalUser = user;
            Students matchingStudent = students.stream().filter(student -> {
                        DocumentReference userRefId = student.getUserRefId();
                        return userRefId != null && userRefId.getPath().endsWith("/users/" + finalUser.getUserId());}).findFirst().orElse(null);
            if (matchingStudent != null) {
                user = matchingStudent;
            }
            users.add(user);
        }
        log.info("Retrieved {} users with students", users.size());
        return users;
    }

    /**
     * updates user information
     * */
    public Users updateUser(String userId, UpdateUserInfoDTO updateDTO) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection("users").document(userId);
        ApiFuture<DocumentSnapshot> userFuture = userRef.get();
        DocumentSnapshot userSnapshot = userFuture.get();

        if (!userSnapshot.exists()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Users user = userSnapshot.toObject(Users.class);
        log.info("Retrieved user {}: {}", userId, user);

        if (user == null) {
            throw new RuntimeException("User object could not be parsed for ID: " + userId);
        }

        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getMiddleName() != null) {
            user.setMiddleName(updateDTO.getMiddleName());
        }
        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getBirthdate() != null) {
            user.setBirthdate(updateDTO.getBirthdate());
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getContactNumber() != null) {
            user.setContactNumber(updateDTO.getContactNumber());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getAccountStatus() != null) {
            user.setAccountStatus(updateDTO.getAccountStatus());
        }

        // update user document
        userRef.set(user).get();
        log.info("Updated user {} with general fields", userId);

        if (user.getUserType() == UserType.STUDENT) {
            ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students").whereEqualTo("userRefId", userRef).get();
            QuerySnapshot studentQuery = studentQueryFuture.get();

            if (studentQuery.isEmpty()) {
                throw new RuntimeException("Student record not found for user ID: " + userId);
            }

            DocumentSnapshot studentSnapshot = studentQuery.getDocuments().getFirst();
            Students student = studentSnapshot.toObject(Students.class);
            student.logFields();

            if (updateDTO.getStudentNumber() != null) student.setStudentNumber(updateDTO.getStudentNumber());
            if (updateDTO.getSection() != null) student.setSection(updateDTO.getSection());
            if (updateDTO.getYearLevel() != null) student.setYearLevel(updateDTO.getYearLevel());

            if (updateDTO.getCourseRefId() != null) {
                DocumentReference courseRef = firestore.document(updateDTO.getCourseRefId());
                ApiFuture<DocumentSnapshot> courseFuture = courseRef.get();
                DocumentSnapshot courseSnapshot = courseFuture.get();

                if (!courseSnapshot.exists()) {
                    throw new RuntimeException("Course not found with path: " + updateDTO.getCourseRefId());
                }

                Courses course = courseSnapshot.toObject(Courses.class);
                student.setCourseRefId(courseRef);
                assert course != null;
                if (course.getClusterRefId() != null) {
                    student.setClusterRefId(course.getClusterRefId());
                    log.info("Automatically updated clusterRefId to {} based on course {}",
                            course.getClusterRefId().getPath(), updateDTO.getCourseRefId());
                } else {
                    log.warn("Course {} does not have a clusterRefId", updateDTO.getCourseRefId());
                    student.setClusterRefId(null);
                }
            }

            studentSnapshot.getReference().set(student).get();
            log.info("Updated student for user {} with student-specific fields", userId);

            return student;
        }
        return user;
    }


    // user deactivation
    public Users deactivateUser(String userId) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection("users").document(userId);
        ApiFuture<DocumentSnapshot> userFuture = userRef.get();
        DocumentSnapshot userSnapshot = userFuture.get();

        if (!userSnapshot.exists()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Users user = userSnapshot.toObject(Users.class);
        assert user != null;
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRef.set(user).get();
        log.info("Deactivated user {}", userId);

        if (user.getUserType() == UserType.STUDENT) {
            ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students")
                    .whereEqualTo("userRefId", userRef)
                    .get();
            QuerySnapshot studentQuery = studentQueryFuture.get();
            if (!studentQuery.isEmpty()) {
                Students student = studentQuery.getDocuments().getFirst().toObject(Students.class);
                student.logFields();
                return student;
            }
        }
        return user;
    }

    // deactivate multiple users
    public List<Users> deactivateUsers(List<String> userIds) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        List<Users> updatedUsers = new ArrayList<>();

        for (String userId : userIds) {
            DocumentReference userRef = firestore.collection("users").document(userId);
            ApiFuture<DocumentSnapshot> userFuture = userRef.get();
            DocumentSnapshot userSnapshot = userFuture.get();

            if (!userSnapshot.exists()) {
                log.warn("User not found with ID: {}, skipping deactivation", userId);
                continue;
            }

            Users user = userSnapshot.toObject(Users.class);
            assert user != null;
            user.setAccountStatus(AccountStatus.INACTIVE);
            batch.set(userRef, user);
            updatedUsers.add(user);

            log.info("Added user {} to batch for deactivation", userId);
        }

        batch.commit().get();
        log.info("Deactivated {} users in batch", updatedUsers.size());
        return updatedUsers;
    }

    // user account reactivate
    public Users reactivateUser(String userId) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection("users").document(userId);
        ApiFuture<DocumentSnapshot> userFuture = userRef.get();
        DocumentSnapshot userSnapshot = userFuture.get();

        if (!userSnapshot.exists()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Users user = userSnapshot.toObject(Users.class);
        assert user != null;
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRef.set(user).get();
        log.info("Reactivated user {}", userId);

        if (user.getUserType() == UserType.STUDENT) {
            ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students")
                    .whereEqualTo("userRefId", userRef)
                    .get();
            QuerySnapshot studentQuery = studentQueryFuture.get();
            if (!studentQuery.isEmpty()) {
                Students student = studentQuery.getDocuments().getFirst().toObject(Students.class);
                student.logFields();
                return student;
            }
        }
        return user;
    }

    /*
     * Searches for users based on the provided search criteria in UserSearchDTO.
     * @param searchDTO The search criteria including search term, user type, account status, and student-specific filters.
     * @return List of Users matching the search criteria.
     * @throws ExecutionException if Firestore query execution fails.
     * @throws InterruptedException if the query is interrupted.
     */
    public List<Users> searchUsers(UserSearchDTO searchDTO) throws ExecutionException, InterruptedException {
        List<Users> users = new ArrayList<>();
        Query query = firestore.collection("users");

        if (searchDTO.getUserType() != null) {
            query = query.whereEqualTo("userType", searchDTO.getUserType().toString());
            log.info("Filtering by userType: {}", searchDTO.getUserType());
        }
        if (searchDTO.getAccountStatus() != null) {
            query = query.whereEqualTo("accountStatus", searchDTO.getAccountStatus().toString());
            log.info("Filtering by accountStatus: {}", searchDTO.getAccountStatus());
        }

        // Fetch user documents
        ApiFuture<QuerySnapshot> queryFuture = query.get();
        List<QueryDocumentSnapshot> userDocs = queryFuture.get().getDocuments();
        log.info("Found {} user documents after initial query", userDocs.size());

        // Fetch all students for student-specific filtering
        ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students").get();
        List<Students> students = studentQueryFuture.get().getDocuments()
                .stream()
                .map(doc -> doc.toObject(Students.class))
                .toList();
        log.info("Found {} student documents", students.size());

        for (QueryDocumentSnapshot userDoc : userDocs) {
            Users user = userDoc.toObject(Users.class);
            boolean matches = true;

            // Full-text search (case-insensitive)
            if (searchDTO.getSearchTerm() != null && !searchDTO.getSearchTerm().isEmpty()) {
                String searchTerm = searchDTO.getSearchTerm().toLowerCase();
                boolean textMatch = (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchTerm)) ||
                        (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchTerm)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm));

                // Check studentNumber for students
                if (!textMatch && user.getUserType() == UserType.STUDENT) {
                    Students student = students.stream()
                            .filter(s -> s.getUserRefId() != null && s.getUserRefId().getPath().endsWith("/users/" + user.getUserId()))
                            .findFirst()
                            .orElse(null);
                    textMatch = student != null && student.getStudentNumber() != null &&
                            student.getStudentNumber().toLowerCase().contains(searchTerm);
                }
                matches = textMatch;
                log.debug("User {} text search match: {}", user.getUserId(), textMatch);
            }

            // Apply student-specific filters
            if (user.getUserType() == UserType.STUDENT && matches) {
                Students student = students.stream()
                        .filter(s -> s.getUserRefId() != null && s.getUserRefId().getPath().endsWith("/users/" + user.getUserId()))
                        .findFirst()
                        .orElse(null);

                if (student != null) {
                    if (searchDTO.getSection() != null && !searchDTO.getSection().equals(student.getSection())) {
                        matches = false;
                        log.debug("User {} excluded: section mismatch (expected: {}, actual: {})",
                                user.getUserId(), searchDTO.getSection(), student.getSection());
                    }
                    if (searchDTO.getYearLevel() != null && !searchDTO.getYearLevel().equals(student.getYearLevel())) {
                        matches = false;
                        log.debug("User {} excluded: yearLevel mismatch (expected: {}, actual: {})",
                                user.getUserId(), searchDTO.getYearLevel(), student.getYearLevel());
                    }
                    if (searchDTO.getCourseRefId() != null && (student.getCourseRefId() == null ||
                            !searchDTO.getCourseRefId().equals(student.getCourseRefId().getPath()))) {
                        matches = false;
                        log.debug("User {} excluded: courseRefId mismatch (expected: {}, actual: {})",
                                user.getUserId(), searchDTO.getCourseRefId(), student.getCourseRefId() != null ? student.getCourseRefId().getPath() : null);
                    }
                } else {
                    matches = false; // No student record found
                    log.debug("User {} excluded: no matching student record", user.getUserId());
                }
            }

            if (matches) {
                users.add(user); // Add the Users object, not Students
                log.debug("User {} included in results", user.getUserId());
            }
        }

        log.info("Found {} users matching search criteria", users.size());
        return users;
    }
}

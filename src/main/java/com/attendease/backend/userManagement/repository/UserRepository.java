package com.attendease.backend.userManagement.repository;

import com.attendease.backend.userManagement.dto.UpdateUserInfoDTO;
import com.attendease.backend.userManagement.dto.UserSearchDTO;
import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.attendease.backend.model.students.Courses;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.attendease.backend.userManagement.dto.UserWithStudentInfo;

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
     * Retrieves all users with their associated student data
     */
    public List<UserWithStudentInfo> retrieveAllUsersWithStudentInfo() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users").get();
        List<QueryDocumentSnapshot> userDocuments = usersFuture.get().getDocuments();

        ApiFuture<QuerySnapshot> studentsFuture = firestore.collection("students").get();
        List<QueryDocumentSnapshot> studentDocuments = studentsFuture.get().getDocuments();

        Map<String, Students> studentMap = studentDocuments.stream().map(doc -> {Students student = doc.toObject(Students.class);
            student.logFields();
            return student;
        }).filter(student -> student.getUserRefId() != null).collect(Collectors.toMap(student -> extractUserIdFromPath(student.getUserRefId().getPath()), student -> student));

        List<UserWithStudentInfo> result = new ArrayList<>();

        for (QueryDocumentSnapshot userDoc : userDocuments) {
            Users user = userDoc.toObject(Users.class);
            Students studentInfo = studentMap.get(user.getUserId());

            result.add(new UserWithStudentInfo(user, studentInfo));
            log.debug("User {} => {} with student info: {}", userDoc.getId(), user, studentInfo != null);
        }

        log.info("Retrieved {} users with student information", result.size());
        return result;
    }

    /**
     * Updates user information
     */
    public UserWithStudentInfo updateUser(String userId, UpdateUserInfoDTO updateDTO) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection("users").document(userId);

        ApiFuture<UserWithStudentInfo> transactionFuture = firestore.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef).get();
            if (!userSnapshot.exists()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            Users user = userSnapshot.toObject(Users.class);
            if (user == null) {
                throw new RuntimeException("User object could not be parsed for ID: " + userId);
            }

            updateUserFields(user, updateDTO);
            transaction.set(userRef, user);

            Students studentInfo = null;

            if (user.getUserType() == UserType.STUDENT) {
                ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students").whereEqualTo("userRefId", userRef).get();

                try {
                    QuerySnapshot studentQuery = studentQueryFuture.get();
                    if (!studentQuery.isEmpty()) {
                        DocumentSnapshot studentSnapshot = studentQuery.getDocuments().getFirst();
                        studentInfo = studentSnapshot.toObject(Students.class);

                        updateStudentFields(studentInfo, updateDTO);

                        if (updateDTO.getCourseRefId() != null) {
                            updateCourseReference(studentInfo, updateDTO.getCourseRefId(), transaction);
                        }

                        transaction.set(studentSnapshot.getReference(), studentInfo);
                        log.info("Updated student for user {} with student-specific fields", userId);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to update student information", e);
                }
            }

            log.info("Updated user {} with general fields", userId);
            return new UserWithStudentInfo(user, studentInfo);
        });

        return transactionFuture.get();
    }

    /**
     * Deactivates a user and returns combined user-student information
     */
    public UserWithStudentInfo deactivateUser(String userId) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection("users").document(userId);

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef).get();
            if (!userSnapshot.exists()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            Users user = userSnapshot.toObject(Users.class);
            if (user == null) {
                throw new RuntimeException("User object could not be parsed for ID: " + userId);
            }

            user.setAccountStatus(AccountStatus.INACTIVE);
            transaction.set(userRef, user);

            Students studentInfo = null;
            if (user.getUserType() == UserType.STUDENT) {
                ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students")
                        .whereEqualTo("userRefId", userRef)
                        .get();
                try {
                    QuerySnapshot studentQuery = studentQueryFuture.get();
                    if (!studentQuery.isEmpty()) {
                        studentInfo = studentQuery.getDocuments().getFirst().toObject(Students.class);
                        studentInfo.logFields();
                    }
                } catch (Exception e) {
                    log.warn("Could not retrieve student information for user {}", userId, e);
                }
            }

            log.info("Deactivated user {}", userId);
            return new UserWithStudentInfo(user, studentInfo);
        }).get();
    }

    /**
     * Batch deactivate multiple users
     */
    public List<UserWithStudentInfo> deactivateUsers(List<String> userIds)
            throws ExecutionException, InterruptedException {

        WriteBatch batch = firestore.batch();
        List<UserWithStudentInfo> updatedUsers = new ArrayList<>();

        List<DocumentReference> userRefs = userIds.stream()
                .map(userId -> firestore.collection("users").document(userId))
                .toList();

        for (DocumentReference userRef : userRefs) {
            ApiFuture<DocumentSnapshot> userFuture = userRef.get();
            DocumentSnapshot userSnapshot = userFuture.get();

            if (!userSnapshot.exists()) {
                log.warn("User not found with ID: {}, skipping deactivation", userRef.getId());
                continue;
            }

            Users user = userSnapshot.toObject(Users.class);
            if (user != null) {
                user.setAccountStatus(AccountStatus.INACTIVE);
                batch.set(userRef, user);

                updatedUsers.add(new UserWithStudentInfo(user, null));
                log.info("Added user {} to batch for deactivation", user.getUserId());
            }
        }

        batch.commit().get();
        log.info("Deactivated {} users in batch", updatedUsers.size());
        return updatedUsers;
    }

    /**
     * Reactivates a user
     */
    public UserWithStudentInfo reactivateUser(String userId) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection("users").document(userId);

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef).get();
            if (!userSnapshot.exists()) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            Users user = userSnapshot.toObject(Users.class);
            if (user == null) {
                throw new RuntimeException("User object could not be parsed for ID: " + userId);
            }

            user.setAccountStatus(AccountStatus.ACTIVE);
            transaction.set(userRef, user);

            Students studentInfo = null;
            if (user.getUserType() == UserType.STUDENT) {
                ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students")
                        .whereEqualTo("userRefId", userRef)
                        .get();
                try {
                    QuerySnapshot studentQuery = studentQueryFuture.get();
                    if (!studentQuery.isEmpty()) {
                        studentInfo = studentQuery.getDocuments().getFirst().toObject(Students.class);
                        studentInfo.logFields();
                    }
                } catch (Exception e) {
                    log.warn("Could not retrieve student information for user {}", userId, e);
                }
            }

            log.info("Reactivated user {}", userId);
            return new UserWithStudentInfo(user, studentInfo);
        }).get();
    }

    /**
     * Enhanced search with better performance and cleaner logic
     */
    public List<UserWithStudentInfo> searchUsers(UserSearchDTO searchDTO)
            throws ExecutionException, InterruptedException {

        Query userQuery = buildUserQuery(searchDTO);

        ApiFuture<QuerySnapshot> userQueryFuture = userQuery.get();
        List<QueryDocumentSnapshot> userDocs = userQueryFuture.get().getDocuments();
        log.info("Found {} user documents after initial query", userDocs.size());

        Map<String, Students> studentMap = null;
        if (needsStudentFiltering(searchDTO)) {
            studentMap = getStudentMap();
            log.info("Loaded {} student records for filtering", studentMap.size());
        }

        List<UserWithStudentInfo> results = new ArrayList<>();

        for (QueryDocumentSnapshot userDoc : userDocs) {
            Users user = userDoc.toObject(Users.class);
            Students studentInfo = null;

            if (user.getUserType() == UserType.STUDENT && studentMap != null) {
                studentInfo = studentMap.get(user.getUserId());
            }

            if (matchesSearchCriteria(user, studentInfo, searchDTO)) {
                results.add(new UserWithStudentInfo(user, studentInfo));
                log.debug("User {} included in results", user.getUserId());
            }
        }

        log.info("Found {} users matching search criteria", results.size());
        return results;
    }

    // Helper methods

    private void updateUserFields(Users user, UpdateUserInfoDTO updateDTO) {
        if (updateDTO.getFirstName() != null) user.setFirstName(updateDTO.getFirstName());
        if (updateDTO.getMiddleName() != null) user.setMiddleName(updateDTO.getMiddleName());
        if (updateDTO.getLastName() != null) user.setLastName(updateDTO.getLastName());
        if (updateDTO.getBirthdate() != null) user.setBirthdate(updateDTO.getBirthdate());
        if (updateDTO.getAddress() != null) user.setAddress(updateDTO.getAddress());
        if (updateDTO.getContactNumber() != null) user.setContactNumber(updateDTO.getContactNumber());
        if (updateDTO.getEmail() != null) user.setEmail(updateDTO.getEmail());
        if (updateDTO.getAccountStatus() != null) user.setAccountStatus(updateDTO.getAccountStatus());
    }

    private void updateStudentFields(Students student, UpdateUserInfoDTO updateDTO) {
        if (updateDTO.getStudentNumber() != null) student.setStudentNumber(updateDTO.getStudentNumber());
        if (updateDTO.getSection() != null) student.setSection(updateDTO.getSection());
        if (updateDTO.getYearLevel() != null) student.setYearLevel(updateDTO.getYearLevel());
    }

    private void updateCourseReference(Students student, String courseRefId, Transaction transaction) throws ExecutionException, InterruptedException {
        DocumentReference courseRef = firestore.document(courseRefId);
        DocumentSnapshot courseSnapshot = transaction.get(courseRef).get();

        if (!courseSnapshot.exists()) {
            throw new RuntimeException("Course not found with path: " + courseRefId);
        }

        Courses course = courseSnapshot.toObject(Courses.class);
        student.setCourseRefId(courseRef);

        if (course != null && course.getClusterRefId() != null) {
            student.setClusterRefId(course.getClusterRefId());
            log.info("Automatically updated clusterRefId to {} based on course {}",course.getClusterRefId().getPath(), courseRefId);
        } else {
            log.warn("Course {} does not have a clusterRefId", courseRefId);
            student.setClusterRefId(null);
        }
    }

    private Query buildUserQuery(UserSearchDTO searchDTO) {
        Query query = firestore.collection("users");

        if (searchDTO.getUserType() != null) {
            query = query.whereEqualTo("userType",searchDTO.getUserType().toString());
            log.info("Filtering by userType: {}",searchDTO.getUserType());
        }
        if (searchDTO.getAccountStatus() != null) {
            query = query.whereEqualTo("accountStatus",searchDTO.getAccountStatus().toString());
            log.info("Filtering by accountStatus: {}",searchDTO.getAccountStatus());
        }

        return query;
    }

    private boolean needsStudentFiltering(UserSearchDTO searchDTO) {
        return searchDTO.getSection() != null || searchDTO.getYearLevel() != null || searchDTO.getCourseRefId() != null || (searchDTO.getSearchTerm() != null && !searchDTO.getSearchTerm().isEmpty());
    }

    private Map<String, Students> getStudentMap() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> studentQueryFuture = firestore.collection("students").get();
        return studentQueryFuture.get().getDocuments()
                .stream()
                .map(doc -> doc.toObject(Students.class))
                .filter(student -> student.getUserRefId() != null)
                .collect(Collectors.toMap(
                        student -> extractUserIdFromPath(student.getUserRefId().getPath()),
                        student -> student));
    }

    private boolean matchesSearchCriteria(Users user, Students studentInfo, UserSearchDTO searchDTO) {
        if (searchDTO.getSearchTerm() != null && !searchDTO.getSearchTerm().isEmpty()) {
            String searchTerm = searchDTO.getSearchTerm().toLowerCase();
            boolean textMatch = matchesTextSearch(user, studentInfo, searchTerm);
            if (!textMatch) return false;
        }

        if (user.getUserType() == UserType.STUDENT && studentInfo != null) {
            if (searchDTO.getSection() != null && !searchDTO.getSection().equals(studentInfo.getSection())) {
                return false;
            }
            if (searchDTO.getYearLevel() != null && !searchDTO.getYearLevel().equals(studentInfo.getYearLevel())) {
                return false;
            }
            return searchDTO.getCourseRefId() == null || (studentInfo.getCourseRefId() != null && searchDTO.getCourseRefId().equals(studentInfo.getCourseRefId().getPath()));
        }

        return true;
    }

    private boolean matchesTextSearch(Users user, Students studentInfo, String searchTerm) {
        boolean textMatch = (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchTerm)) ||
                (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchTerm)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm));

        if (!textMatch && user.getUserType() == UserType.STUDENT && studentInfo != null) {
            textMatch = studentInfo.getStudentNumber() != null && studentInfo.getStudentNumber().toLowerCase().contains(searchTerm);
        }

        return textMatch;
    }

    private String extractUserIdFromPath(String path) {
        String[] parts = path.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }
}
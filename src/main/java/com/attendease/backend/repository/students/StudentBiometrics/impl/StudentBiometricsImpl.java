package com.attendease.backend.repository.students.StudentBiometrics.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.students.StudentBiometrics.StudentBiometrics;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.attendease.backend.repository.students.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StudentBiometricsImpl implements StudentBiometrics {

    private final MongoTemplate mongoTemplate;
    private final StudentRepository studentRepository;

    @Override
    public Long deleteAllStudentsAndAssociatedUserAndFacialData() {
        List<Students> allStudents = studentRepository.findAll();

        if (allStudents.isEmpty()) {
            log.info("There are no students to delete.");
            return 0L;
        }

        List<String> userIdsToDelete = allStudents.stream()
                .map(Students::getUser)
                .filter(Objects::nonNull)
                .map(Users::getUserId)
                .toList();

        List<String> biometricIdsToDelete = allStudents.stream().filter(s -> s.getFacialData() != null).map(s -> s.getFacialData().getFacialId()).toList();

        if (!biometricIdsToDelete.isEmpty()) {
            Query biometricQuery = new Query(Criteria.where("facialId").in(biometricIdsToDelete));
            var biometricResult = mongoTemplate.remove(biometricQuery, BiometricData.class);
            log.info("Deleted {} biometric records.", biometricResult.getDeletedCount());
        }

        if (!userIdsToDelete.isEmpty()) {
            Query userQuery = new Query(Criteria.where("userId").in(userIdsToDelete));
            var userResult = mongoTemplate.remove(userQuery, Users.class);
            log.info("Deleted {} users.", userResult.getDeletedCount());
        }

        Query studentQuery = new Query(Criteria.where("id").in(allStudents.stream().map(Students::getId).toList()));
        var studentResult = mongoTemplate.remove(studentQuery, Students.class);
        long studentsDeleted = studentResult.getDeletedCount();
        log.info("Deleted {} students.", studentsDeleted);

        return studentsDeleted;
    }

}

package com.attendease.backend.osaModule.service.management.user.information.management;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.students.UserStudent.UserStudentResponse;
import com.attendease.backend.domain.users.Information.Management.Request.UpdateUserRequest;
import com.attendease.backend.domain.users.Information.Management.Response.UpdateResultResponse;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentBiometrics.StudentBiometrics;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInformationManagementService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SectionsRepository sectionsRepository;
    private final StudentBiometrics studentBiometrics;
    private final PasswordEncoder passwordEncoder;

    public long deleteAllStudentsAndAssociatedUserAndFacialData() {
        return studentBiometrics.deleteAllStudentsAndAssociatedUserAndFacialData();
    }

    /**
     * Allows OSA to update user information (with optional student-specific fields).
     * User field updates automatically propagate to referenced students via DBRef.
     * Student-specific fields require explicit update to the Students collection.
     *
     * @param userId The ID of the user to update
     * @param request The update request containing optional fields
     * @param updatedByUserId The ID of the user performing the update
     * @return The updated user
     */
    @Transactional
    public UpdateResultResponse osaUpdateUserInfo(String userId, UpdateUserRequest request, String updatedByUserId) throws ChangeSetPersister.NotFoundException {
        Users user = userRepository.findById(userId).orElseThrow(ChangeSetPersister.NotFoundException::new);

        updateUserFields(user, request, updatedByUserId);

        UserStudentResponse studentResponse = null;

        if (user.getUserType() == UserType.STUDENT) {
            studentResponse = handleStudentUpdate(userId, request, user);
        }

        return UpdateResultResponse.builder().user(user).studentResponse(studentResponse).build();
    }

    /**
     * PRIVATE HELPERS
     */

    private void updateUserFields(Users user, UpdateUserRequest request, String updatedByUserId) {
        updateIfPresent(request.getFirstName(), user::setFirstName);
        updateIfPresent(request.getLastName(), user::setLastName);

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        updateIfPresent(request.getContactNumber(), user::setContactNumber);
        updateIfPresent(request.getEmail(), user::setEmail);

        user.setUpdatedBy(updatedByUserId);
        userRepository.save(user);
    }


    private <T> void updateIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private UserStudentResponse handleStudentUpdate(String userId, UpdateUserRequest request, Users updatedUser) throws ChangeSetPersister.NotFoundException {
        Students student = studentRepository.findByUserId(userId).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (request.getStudentNumber() != null && !request.getStudentNumber().equals(student.getStudentNumber())) {
            validateUniqueStudentNumber(request.getStudentNumber());
            student.setStudentNumber(request.getStudentNumber());
        }

        if (request.getSectionId() != null) {
            Sections section = sectionsRepository.findById(request.getSectionId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
            student.setSection(section);
        }

        Students updatedStudent = studentRepository.save(student);
        return buildUserStudentResponse(updatedUser, updatedStudent);
    }

    private void validateUniqueStudentNumber(String newStudentNumber) {
        studentRepository.findByStudentNumber(newStudentNumber).ifPresent(existing -> {throw new IllegalArgumentException(
                    "Student number '" + newStudentNumber + "' is already assigned to another student."
            );
        });
    }

    private UserStudentResponse buildUserStudentResponse(Users user, Students student) {
        Sections section = student.getSection();
        String sectionName = (section != null) ? section.getSectionName() : null;
        String courseName = null;
        String clusterName = null;

        if (section != null) {
            Courses course = section.getCourse();
            Clusters cluster = section.getCourse().getCluster();
            courseName = course.getCourseName();
            clusterName = (cluster != null) ? cluster.getClusterName() : null;
        }

        return UserStudentResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .accountStatus(user.getAccountStatus())
                .userType(user.getUserType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .studentId(student.getId())
                .studentNumber(student.getStudentNumber())
                .section(sectionName)
                .course(courseName)
                .cluster(clusterName)
                .build();
    }
}

package com.attendease.backend.osa.service.management.user.information.impl;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.account.management.users.information.UserAccountManagementUsersInformationRequest;
import com.attendease.backend.domain.user.account.management.users.information.UserAccountManagementUsersInformationResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.management.user.information.ManagementUserInformationService;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentBiometrics.StudentBiometrics;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import java.util.function.Consumer;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementUserInformationServiceImpl implements ManagementUserInformationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SectionsRepository sectionsRepository;
    private final StudentBiometrics studentBiometrics;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    public long deleteAllStudentsAndAssociatedUserAndFacialData() {
        return studentBiometrics.deleteAllStudentsAndAssociatedUserAndFacialData();
    }

    @Transactional
    @Override
    public UserAccountManagementUsersInformationResponse osaUpdateUserInfo(String userId, UserAccountManagementUsersInformationRequest request, String updatedByUserId) throws ChangeSetPersister.NotFoundException {
        User user = userRepository.findById(userId).orElseThrow(ChangeSetPersister.NotFoundException::new);
        updateUserFields(user, request, updatedByUserId);
        UserStudentResponse studentResponse = null;

        if (user.getUserType() == UserType.STUDENT) {
            studentResponse = handleStudentUpdate(userId, request, user);
        }

        return UserAccountManagementUsersInformationResponse.builder().user(user).studentResponse(studentResponse).build();
    }

    /**
     * PRIVATE HELPERS
     */

    private void updateUserFields(User user, UserAccountManagementUsersInformationRequest request, String updatedByUserId) {
        updateIfPresent(request.getFirstName(), user::setFirstName);
        updateIfPresent(request.getLastName(), user::setLastName);

        if (request.getPassword() != null) {
            userValidator.validatePassword(request.getPassword());
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

    private UserStudentResponse handleStudentUpdate(String userId, UserAccountManagementUsersInformationRequest request, User updatedUser) throws ChangeSetPersister.NotFoundException {
        Students student = studentRepository.findByUserId(userId).orElseThrow(ChangeSetPersister.NotFoundException::new);

        if (request.getStudentNumber() != null && !request.getStudentNumber().equals(student.getStudentNumber())) {
            validateUniqueStudentNumber(request.getStudentNumber());
            student.setStudentNumber(request.getStudentNumber());
        }

        if (request.getSectionId() != null) {
            Section section = sectionsRepository.findById(request.getSectionId()).orElseThrow(ChangeSetPersister.NotFoundException::new);
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

    private UserStudentResponse buildUserStudentResponse(User user, Students student) {
        Section section = student.getSection();
        String sectionName = (section != null) ? section.getSectionName() : null;
        String courseName = null;
        String clusterName = null;

        if (section != null) {
            Course course = section.getCourse();
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

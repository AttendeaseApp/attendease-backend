package com.attendease.backend.osaModule.service.management.student.registration;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.students.Registration.Request.StudentRegistrationRequest;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.service.utils.PasswordValidation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class StudentRegistrationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClustersRepository clustersRepository;
    private final CourseRepository courseRepository;
    private final SectionsRepository sectionsRepository;
    private final PasswordValidation passwordValidation;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new student account using separate request DTO
     *
     * @param registrationRequest Contains all student registration data
     * @return Success message
     */
    public String registerNewStudentAccount(StudentRegistrationRequest registrationRequest) {
        validateRegistrationRequest(registrationRequest);

        if (studentRepository.existsByStudentNumber(registrationRequest.getStudentNumber())) {
            throw new IllegalArgumentException("Student number already exists.");
        }

        Users user = createUserFromRegistrationRequest(registrationRequest);
        Students student = createStudentFromRegistrationRequest(registrationRequest);
        student.setUser(user);

        userRepository.save(user);
        studentRepository.save(student);

        log.info("Registered new student account for studentNumber: {}", registrationRequest.getStudentNumber());

        return "Student account registered successfully.";
    }

    /**
     * PRIVATE HELPERS
     */

    private void validateRegistrationRequest(StudentRegistrationRequest request) {
        if (request.getStudentNumber() == null || request.getStudentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        passwordValidation.validatePassword(request.getPassword());
    }

    private Users createUserFromRegistrationRequest(StudentRegistrationRequest request) {
        Users user = new Users();
        user.setUserId(UUID.randomUUID().toString());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setContactNumber(request.getContactNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(String.valueOf(UserType.SYSTEM));
        return user;
    }

    private Students createStudentFromRegistrationRequest(StudentRegistrationRequest request) {
        Students student = new Students();
        student.setStudentNumber(request.getStudentNumber());
        Sections derivedSection = null;
        Courses derivedCourse = null;
        Clusters derivedCluster = null;

        if (request.getSection() != null && !request.getSection().trim().isEmpty()) {
            String sectionValue = request.getSection().trim();
            if (isValidId(sectionValue)) {
                derivedSection = sectionsRepository.findById(sectionValue).orElseThrow(() -> new IllegalArgumentException("Section ID not found: " + sectionValue));
            } else {
                derivedSection = sectionsRepository.findByName(sectionValue).orElseThrow(() -> new IllegalArgumentException("Section name not found: " + sectionValue));
            }
            student.setSection(derivedSection);

            derivedCourse = derivedSection.getCourse();
            if (derivedCourse == null) {
                throw new IllegalArgumentException("Section has no associated course.");
            }
            student.setCourse(derivedCourse);

            derivedCluster = derivedCourse.getCluster();
            if (derivedCluster == null) {
                throw new IllegalArgumentException("Course has no associated cluster.");
            }
            student.setCluster(derivedCluster);
        } else if (request.getCourseRefId() != null) {
            derivedCourse = courseRepository.findById(request.getCourseRefId()).orElseThrow(() -> new IllegalArgumentException("Course ID not found: " + request.getCourseRefId()));
            student.setCourse(derivedCourse);
            derivedCluster = derivedCourse.getCluster();
            if (derivedCluster != null) student.setCluster(derivedCluster);
        } else if (request.getClusterRefId() != null) {
            derivedCluster = clustersRepository.findById(request.getClusterRefId()).orElseThrow(() -> new IllegalArgumentException("Cluster ID not found: " + request.getClusterRefId()));
            student.setCluster(derivedCluster);
        }
        validateAcademicReferencing(student, request);
        return student;
    }

    private void validateAcademicReferencing(Students student, StudentRegistrationRequest request) {
        if (request.getCourseRefId() != null && student.getCourse() != null && !request.getCourseRefId().equals(student.getCourse().getId())) {
            throw new IllegalArgumentException("Provided course ID does not match derived from section.");
        }
        if (request.getClusterRefId() != null && student.getCluster() != null && !request.getClusterRefId().equals(student.getCluster().getClusterId())) {
            throw new IllegalArgumentException("Provided cluster ID does not match derived from section/course.");
        }
    }

    private boolean isValidId(String value) {
        return value != null && value.length() == 24 && value.matches("^[0-9a-fA-F]{24}$");
    }
}

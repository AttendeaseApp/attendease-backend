package com.attendease.backend.osa.service.management.student.registration.impl;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.student.registration.StudentRegistrationRequest;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.osa.service.management.student.registration.ManagementStudentRegistrationService;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ManagementStudentRegistrationServiceImpl implements ManagementStudentRegistrationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SectionsRepository sectionsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    public String registerNewStudentAccount(StudentRegistrationRequest registrationRequest) {
        userValidator.validateFirstName(registrationRequest.getFirstName(), "First name");
        userValidator.validateLastName(registrationRequest.getLastName(), "Last name");
        userValidator.validatePassword(registrationRequest.getPassword());
        userValidator.validateEmail(registrationRequest.getEmail());
        userValidator.validateContactNumber(registrationRequest.getContactNumber());
        userValidator.validateStudentNumber(registrationRequest.getStudentNumber());

        if (studentRepository.existsByStudentNumber(registrationRequest.getStudentNumber())) {
            throw new IllegalArgumentException("Student with this student number already exists.");
        }

        User user = createUserFromRegistrationRequest(registrationRequest);
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

    private User createUserFromRegistrationRequest(StudentRegistrationRequest request) {
        User user = new User();
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
        Sections derivedSection;
        Courses derivedCourse;

        if (request.getSection() != null && !request.getSection().trim().isEmpty()) {
            String sectionValue = request.getSection().trim();
            if (isValidId(sectionValue)) {
                derivedSection = sectionsRepository.findById(sectionValue).orElseThrow(() -> new IllegalArgumentException("Section ID not found: " + sectionValue));
            } else {
                derivedSection = sectionsRepository.findBySectionName(sectionValue).orElseThrow(() -> new IllegalArgumentException("Section name not found: " + sectionValue));
            }
            student.setSection(derivedSection);

            derivedCourse = derivedSection.getCourse();
            if (derivedCourse == null) {
                throw new IllegalArgumentException("Section has no associated course.");
            }
        }
        return student;
    }

    private boolean isValidId(String value) {
        return value != null && value.length() == 24 && value.matches("^[0-9a-fA-F]{24}$");
    }
}

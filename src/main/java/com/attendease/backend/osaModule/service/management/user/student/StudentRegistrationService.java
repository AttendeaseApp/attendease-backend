package com.attendease.backend.osaModule.service.management.user.student;

import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.students.Registration.Request.StudentRegistrationRequest;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
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

    // helper methods

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

    public Students createStudentFromRegistrationRequest(StudentRegistrationRequest request) {
        Students student = new Students();
        student.setStudentNumber(request.getStudentNumber());
        student.setSectionId(request.getSection());
        return student;
    }
}

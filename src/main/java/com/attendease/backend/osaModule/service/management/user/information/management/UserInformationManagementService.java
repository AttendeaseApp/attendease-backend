package com.attendease.backend.osaModule.service.management.user.information.management;

import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Information.Management.UpdateUserRequest;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentBiometrics.StudentBiometrics;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import jakarta.validation.Valid;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInformationManagementService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SectionsRepository sectionsRepository;
    private final StudentBiometrics studentBiometrics;
    private final PasswordEncoder passwordEncoder;

    public long deleteAllStudentsAndAssociatedUserAndFacialData() throws ExecutionException, InterruptedException {
        return studentBiometrics.deleteAllStudentsAndAssociatedUserAndFacialData();
    }

    /**
     * Allows OSA to update user information (with optional student-specific fields).
     * User field updates automatically propagate to referenced students via DBRef.
     * Student-specific fields require explicit update to the Students collection.
     *
     * @param targetUserId The ID of the user to update
     * @param request The update request containing optional fields
     * @param updatedBy The ID of the user performing the update
     * @return The updated user
     */
    public Users osaUpdateUserInfo(String targetUserId, @Valid UpdateUserRequest request, String updatedBy) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required.");
        }
        Users targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        boolean userChanged = false;
        if (request.getFirstName() != null) {
            targetUser.setFirstName(request.getFirstName());
            userChanged = true;
        }
        if (request.getLastName() != null) {
            targetUser.setLastName(request.getLastName());
            userChanged = true;
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(targetUser.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use.");
            }
            targetUser.setEmail(request.getEmail());
            userChanged = true;
        }
        if (request.getContactNumber() != null) {
            validateContactNumber(request.getContactNumber());
            targetUser.setContactNumber(request.getContactNumber());
            userChanged = true;
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            validatePassword(request.getPassword());
            targetUser.setPassword(passwordEncoder.encode(request.getPassword()));
            userChanged = true;
        }
        targetUser.setUpdatedBy(updatedBy);

        Users updatedUser = userChanged ? userRepository.save(targetUser) : targetUser;
        if (targetUser.getUserType() == UserType.STUDENT) {
            boolean hasStudentFields = request.getStudentNumber() != null || request.getSectionId() != null;
            if (hasStudentFields) {
                Students targetStudent = studentRepository.findByUserId(targetUserId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student record not found."));
                boolean studentChanged = false;
                if (request.getStudentNumber() != null && !request.getStudentNumber().trim().isEmpty() && !request.getStudentNumber().trim().equals(targetStudent.getStudentNumber())) {
                    if (studentRepository.existsByStudentNumber(request.getStudentNumber().trim())) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Student number already in use.");
                    }
                    targetStudent.setStudentNumber(request.getStudentNumber().trim());
                    studentChanged = true;
                }
                if (request.getSectionId() != null) {
                    Sections newSection = sectionsRepository.findById(request.getSectionId().trim()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found."));
                    targetStudent.setSection(newSection);
                    studentChanged = true;
                }
                if (studentChanged) {
                    studentRepository.save(targetStudent);
                    log.info("OSA also updated student-specific info for user {}", targetUserId);
                }
            }
        }
        if (userChanged) {
            log.info("OSA successfully updated user info for user {}", targetUserId);
        }
        return updatedUser;
    }

    /**
     * PRIVATE HELPERS
     */
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters");
        }
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        if (!Pattern.compile("[^A-Za-z0-9]").matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }

    private void validateContactNumber(String contactNumber) {
        if (contactNumber != null && !contactNumber.trim().isEmpty()) {
            if (!Pattern.compile("^[+]?[0-9]{12,15}$").matcher(contactNumber.replaceAll("[\\s\\-()]", "")).matches()) {
                throw new IllegalArgumentException("Contact number must be a valid phone number (12-15 digits)");
            }
        }
    }
}

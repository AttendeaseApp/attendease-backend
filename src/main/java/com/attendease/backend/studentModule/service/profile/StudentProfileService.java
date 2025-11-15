package com.attendease.backend.studentModule.service.profile;

import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.service.utils.PasswordValidation;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for managing student personal profile operations,
 * including retrieving profile information and updating passwords.
 *
 * <p>This service interacts with both {@link UserRepository} and
 * {@link StudentRepository} to fetch and update data related to students
 * and their associated user accounts.</p>
 */
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidation passwordValidation;

    /**
     * Retrieves a student's personal profile using the associated user's ID.
     *
     * @param userId the ID of the user whose student profile is requested
     * @return an {@link Optional} containing the student profile if found, otherwise empty
     */
    public Optional<Students> getStudentProfileByUserId(String userId) {
        Optional<Users> userOptional = userRepository.findById(userId);
        return userOptional.flatMap(studentRepository::findByUser);
    }

    /**
     * Retrieves a user profile by user ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the user profile if found, otherwise empty
     */
    public Optional<Users> getUserProfileByUserId(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Updates student password
     *
     * @param id The student number
     * @param oldPassword   Current password for verification
     * @param newPassword   New password to set
     * @return Success message
     */
    public String updatePassword(String id, String oldPassword, String newPassword) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number is required.");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required.");
        }

        passwordValidation.validatePassword(newPassword);

        Students student = studentRepository.findByUserId(id).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        Users user = student.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password updated successfully.";
    }
}

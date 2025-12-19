package com.attendease.backend.student.service.profile;

import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import java.util.Optional;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for managing student personal profile operations,
 * including retrieving profile information and updating passwords.
 *
 * <p>This service interacts with both {@link UserRepository} and
 * {@link StudentRepository} to fetch and update data related to student
 * and their associated user account.</p>
 */
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    /**
     * Retrieves a student's personal profile using the associated user's ID.
     *
     * @param userId the ID of the user whose student profile is requested
     * @return an {@link Optional} containing the student profile if found, otherwise empty
     */
    public Optional<Students> getStudentProfileByUserId(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.flatMap(studentRepository::findByUser);
    }

    /**
     * Retrieves a user profile by user ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the user profile if found, otherwise empty
     */
    public Optional<User> getUserProfileByUserId(String userId) {
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

        userValidator.validatePassword(newPassword);

        Students student = studentRepository.findByUserId(id).orElseThrow(() -> new IllegalArgumentException("Student not found."));
        User user = student.getUser();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "password updated successfully.";
    }
}

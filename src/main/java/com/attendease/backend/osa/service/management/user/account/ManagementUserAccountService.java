package com.attendease.backend.osa.service.management.user.account;

import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.exceptions.domain.ImportException.CsvImportException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * {@link ManagementUserAccountService} is a service responsible for managing user accounts, including bulk imports, retrievals, and deletions.
 *
 * <p>Provides methods for osa (Office of Student Affairs) operations such as importing student via CSV, retrieving user with associated student details,
 * fetching all student, deleting individual user, and bulk-deleting student by section. Ensures validations, duplicate checks, and cascading deletions
 * to linked entities (User, Students, Sections).</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
public interface ManagementUserAccountService {

    /**
     * {@code importStudentsViaCSV} is used to import student from a CSV file, creating linked user accounts and student entities.
     *
     * <p>Validates the CSV structure (headers, required columns: firstName, lastName, studentNumber, password), parses rows,
     * checks for duplicates (student number), validates fields (e.g., password strength, section format), and associates
     * with sections if provided. Encodes passwords and sets ACTIVE status for STUDENT type user. Skips invalid rows and
     * throws exceptions for parsing or validation errors.</p>
     *
     * @param file the {@link MultipartFile} containing the CSV data
     * @return a list of created {@link User} objects for successfully imported student
     * @throws IllegalArgumentException if the file is invalid (e.g., not CSV, missing headers, empty)
     * @throws CsvImportException if import completes with row-specific errors (e.g., duplicates, validation failures)
     */
    List<User> importStudentsViaCSV(MultipartFile file);

    /**
     * {@code retrieveUsersWithStudents} is used to retrieve all user (osa and student) with their associated student details, if applicable.
     *
     * <p>Fetches all user and joins with student data, mapping to a response DTO that includes hierarchical details
     * (section, course, cluster) for student. Non-student user return without student-specific fields.</p>
     *
     * @return a list of {@link UserStudentResponse} objects enriched with student information where relevant
     */
    List<UserStudentResponse> retrieveUsers();

    /**
     * {@code retrieveAllStudents} is used to retrieve all student entities from the database.
     *
     * @return a list of all {@link Students} objects
     */
    List<UserStudentResponse> retrieveAllStudents();

    /**
     * {@code deleteUserById} is used to permanently deletes a user account by its unique identifier.
     *
     * <p>Cascades deletion to any linked entities (e.g., student records). Intended for administrative removal of individual accounts.</p>
     *
     * @param userId the unique ID of the user to delete
     * @throws ResponseStatusException with 404 status if the user is not found
     */
    void deleteUserById(String userId);

    /**
     * {@code deleteStudentsBySection} is used to delete all student account (user and student) associated with the given section.
     *
     * @param sectionName the name of the section (e.g., "BSIT-401")
     * @throws ResponseStatusException if the section is not found
     */
    void deleteStudentsBySection(String sectionName);
}

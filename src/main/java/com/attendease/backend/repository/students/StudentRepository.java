package com.attendease.backend.repository.students;

import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.user.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@link Students} documents in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard operations such as {@code save}, {@code findAll},
 * {@code findById}, and {@code delete}. This repository also defines custom query methods to
 * retrieve students based on student number, user account, course, section, and combinations thereof.
 * </p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Repository
public interface StudentRepository extends MongoRepository<Students, String> {
    /**
     * Finds a student by their unique student number.
     *
     * @param studentNumber the unique student number
     * @return an {@link Optional} containing the {@link Students} object if found, otherwise empty
     */
    Optional<Students> findByStudentNumber(String studentNumber);

    /**
     * Finds a student by their associated user account.
     *
     * @param user the {@link User} object representing the user's account
     * @return an {@link Optional} containing the {@link Students} object if found, otherwise empty
     */
    Optional<Students> findByUser(User user);

    /**
     * Checks if a student exists by their student number.
     *
     * @param studentNumber the unique student number
     * @return {@code true} if a student with the given student number exists, {@code false} otherwise
     */
    boolean existsByStudentNumber(String studentNumber);

    /**
     * Finds all students whose user account are in the given list.
     *
     * @param users a list of {@link User} objects
     * @return a list of {@link Students} associated with the given user
     */
    List<Students> findByUserIn(List<User> users);

    /**
     * Finds a student by the ID of their associated user account using a custom MongoDB query.
     *
     * @param userId the ID of the {@link User} account
     * @return an {@link Optional} containing the {@link Students} object if found, otherwise empty
     */
    @Query("{ 'user' : ?0 }")
    Optional<Students> findByUserId(String userId);

    /**
     * Finds all students associated with the given section.
     *
     * @param section the {@link Sections} entity representing the section
     * @return a {@link List} of all {@link Students} enrolled in the specified section
     */
    List<Students> findBySection(Sections section);

    List<Students> findBySectionIdIn(List<String> sections);

    Long countBySection(Sections section);
}

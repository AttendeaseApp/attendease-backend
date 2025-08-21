/**
 * Service implementation for OSA managing user-related operations such as retrieval, updating,
 * deactivation, reactivation, deletion, and searching of users and students.
 * This class interacts with Firestore through repositories to perform CRUD operations and
 * bulk operations on user data.
 */

package com.attendease.backend.userManagement.service;

import com.attendease.backend.userManagement.dto.StudentDTO;
import com.attendease.backend.userManagement.dto.BulkUserOperationsDTO;
import com.attendease.backend.userManagement.dto.UpdateUserInfoDTO;
import com.attendease.backend.userManagement.dto.UserSearchDTO;
import com.attendease.backend.userManagement.dto.UsersDTO;
import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.authentication.student.repository.StudentAuthenticationRepository;
import com.attendease.backend.userManagement.repository.UserRepository;
import com.google.cloud.firestore.Firestore;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Implementation of user management service for handling user and student operations.
 * Provides methods for importing, retrieving, updating, deactivating, reactivating,
 * deleting, and searching users and students in the system.
 */
@Service
@Slf4j
public class UserManagementServiceImpl {
    private final StudentAuthenticationRepository studentRepository;
    private final UserRepository userRepository;
    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for UserManagementServiceImpl.
     * @param studentRepository Repository for student-related data operations.
     * @param userRepository Repository for user-related data operations.
     */
    public UserManagementServiceImpl(Firestore firestore, StudentAuthenticationRepository studentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder ) {
        this.firestore = firestore;
        this.studentRepository = studentRepository;
        this.userRepository =userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Imports students from a CSV file and saves them as Users and Students in Firestore.
     * @param file The CSV file containing student data.
     * @return List of UsersDTO for successfully imported students.
     * @throws IOException if there's an error reading the CSV file.
     * @throws CsvValidationException if the CSV file is malformed.
     * @throws ExecutionException if Firestore operations fail.
     * @throws InterruptedException if Firestore operations are interrupted.
     */
    public List<UsersDTO> importStudentsViaCSV(MultipartFile file) throws IOException, CsvValidationException, ExecutionException, InterruptedException {
        List<UsersDTO> importedUsers = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = csvReader.readNext();
            if (header == null) {
                log.error("CSV file is empty or invalid");
                throw new IllegalArgumentException("CSV file is empty or invalid");
            }

            // validation of the required columns
            List<String> requiredColumns = List.of("firstName", "lastName", "studentNumber", "password");
            for (String col : requiredColumns) {
                if (!List.of(header).contains(col)) {
                    log.error("Missing required column: {}", col);
                    throw new IllegalArgumentException("Missing required column: " + col);
                }
            }

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                try{
                    Users user = new Users();
                    Students student = new Students();
                    String userId = UUID.randomUUID().toString();
                    user.setUserId(userId);
                    user.setUserType(UserType.STUDENT);
                    user.setAccountStatus(AccountStatus.ACTIVE);
                    user.setUpdatedBy(UserType.SYSTEM);

                    for (int i = 0; i < header.length && i < row.length; i++) {
                        String value = row[i] != null && !row[i].isEmpty() ? row[i] : null;
                        switch (header[i].toLowerCase()) {
                            case "firstname":
                                user.setFirstName(value);
                                break;
                            case "lastname":
                                user.setLastName(value);
                                break;
                            case "password":
                                if (value != null) {
                                    String hashedPassword = passwordEncoder.encode(value);
                                    user.setPassword(hashedPassword);
                                }
                                break;
                            case "email":
                                user.setEmail(value);
                                break;
                            case "studentnumber":
                                student.setStudentNumber(value);
                                break;
                            case "section":
                                student.setSection(value);
                                break;
                            case "yearlevel":
                                student.setYearLevel(value);
                                break;
                            case "courserefid":
                                if (value != null) {
                                    student.setCourseRefId(firestore.document(value));
                                }
                                break;
                            case "birthdate":
                                if (value != null) {
                                    LocalDate localDate = LocalDate.parse(value, dateFormatter);
                                    user.setBirthdate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                }
                                break;
                            case "address":
                                user.setAddress(value);
                                break;
                            case "contactnumber":
                                user.setContactNumber(value);
                                break;
                        }
                    }

                    if (user.getFirstName() == null || user.getLastName() == null || student.getStudentNumber() == null || user.getPassword() == null) {
                        log.warn("Skipping row due to missing required fields: {}", String.join(",", row));
                        continue;
                    }

                    if (studentRepository.existsByStudentNumber(student.getStudentNumber())) {
                        log.warn("Skipping row due to duplicate studentNumber: {}", student.getStudentNumber());
                        continue;
                    }

                    userRepository.saveUser(user);
                    log.info("Saved user: {}", user.getUserId());

                    // set userRefId and save student
                    student.setUserRefId(firestore.collection("users").document(user.getUserId()));
                    studentRepository.saveStudent(student);
                    log.info("Saved student for user: {}", user.getUserId());

                    UsersDTO userDTO = new UsersDTO(user, student);
                    importedUsers.add(userDTO);
                    log.info("Imported user: {}", userDTO);
                } catch (Exception e) {
                    log.error("Error processing CSV row: {}", String.join(",", row), e);
                }
            }
        }
        log.info("Imported {} students from CSV", importedUsers.size());
        return importedUsers;
    }


    /**
     * Retrieves all users from the repository and maps them to UsersDTO.
     * @return List of UsersDTO containing user information.
     * @throws RuntimeException if retrieval fails due to ExecutionException or InterruptedException.
     */
    public List<UsersDTO> retrieveAllUsersService() {
        try {
            List<Users> users = userRepository.retrieveAllUsers();
            List<UsersDTO> userDTOs = users.stream().map(user -> {
                UsersDTO dto = user instanceof Students ? new UsersDTO(user, (Students) user) : new UsersDTO(user);

                log.info("Mapped to UserDTO: {}", dto);
                return dto;
            }).collect(Collectors.toList());

            log.info("Returning {} UserDTOs: {}", userDTOs.size(), userDTOs);
            return userDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to retrieve users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }


    /**
     * Retrieves all students from the repository and maps them to StudentDTO.
     * @return List of StudentDTO containing student information.
     * @throws RuntimeException if retrieval fails due to ExecutionException or InterruptedException.
     */
    public List<StudentDTO> retrieveAllStudentsService() {
        try {
            List<Students> students = studentRepository.retrieveAllStudents();
            log.info("Retrieved {} students from repository: {}", students.size(), students);
            List<StudentDTO> studentDTOs = students.stream()
                    .map(student -> {
                        StudentDTO dto = new StudentDTO(student);
                        log.info("Mapped student to DTO: {}", dto);
                        return dto;
                    })
                    .collect(Collectors.toList());
            log.info("Returning {} StudentDTOs: {}", studentDTOs.size(), studentDTOs);
            return studentDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to retrieve students", e);
            throw new RuntimeException("Failed to retrieve students", e);
        }
    }


    /**
     * Updates user information based on the provided user ID and update DTO.
     * @param userId The ID of the user to update.
     * @param updateDTO Data transfer object containing updated user information.
     * @return UsersDTO representing the updated user.
     * @throws RuntimeException if update fails due to ExecutionException or InterruptedException.
     */
    public UsersDTO updateUserInformationService(String userId, UpdateUserInfoDTO updateDTO) {
        try {
            Users updatedUser = userRepository.updateUser(userId, updateDTO);
            UsersDTO userDTO = updatedUser instanceof Students ? new UsersDTO(updatedUser, (Students) updatedUser) : new UsersDTO(updatedUser);
            log.info("Updated user {} and mapped to UserDTO: {}", userId, userDTO);
            return userDTO;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to update user {}", userId, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }


    // user deactivation methods

    /**
     * Deactivates a user account based on the provided user ID.
     * @param userId The ID of the user to deactivate.
     * @return UsersDTO representing the deactivated user.
     * @throws RuntimeException if deactivation fails due to ExecutionException or InterruptedException.
     */
    public UsersDTO deactivateUserService(String userId) {
        try {
            Users user = userRepository.deactivateUser(userId);
            UsersDTO userDTO = user instanceof Students ? new UsersDTO(user, (Students) user) : new UsersDTO(user);
            log.info("Deactivated user {} and mapped to UserDTO: {}", userId, userDTO);
            return userDTO;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to deactivate user {}", userId, e);
            throw new RuntimeException("Failed to deactivate user", e);
        }
    }

    /**
     * Deactivates multiple user accounts based on the provided list of user IDs.
     * @param bulkDTO Data transfer object containing a list of user IDs to deactivate.
     * @return List of UsersDTO representing the deactivated users.
     * @throws RuntimeException if bulk deactivation fails due to ExecutionException or InterruptedException.
     */
    public List<UsersDTO> bulkDeactivationOnUsersService(BulkUserOperationsDTO bulkDTO) {
        try {
            List<Users> users = userRepository.deactivateUsers(bulkDTO.getUserIds());
            List<UsersDTO> userDTOs = users.stream().map(user -> user instanceof Students ? new UsersDTO(user, (Students) user) : new UsersDTO(user)).collect(Collectors.toList());
            log.info("Deactivated {} users and mapped to UserDTOs", userDTOs.size());
            return userDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to deactivate users", e);
            throw new RuntimeException("Failed to deactivate users", e);
        }
    }


    // reactivate user account methods

    /**
     * Reactivates a user account based on the provided user ID.
     * @param userId The ID of the user to reactivate.
     * @return UsersDTO representing the reactivated user.
     * @throws RuntimeException if reactivation fails due to ExecutionException or InterruptedException.
     */
    public UsersDTO reactivateUserService(String userId) {
        try {
            Users user = userRepository.reactivateUser(userId);
            UsersDTO userDTO = user instanceof Students ? new UsersDTO(user, (Students) user) : new UsersDTO(user);
            log.info("Reactivated user {} and mapped to UserDTO: {}", userId, userDTO);
            return userDTO;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to reactivate user {}", userId, e);
            throw new RuntimeException("Failed to reactivate user", e);
        }
    }

    // searching users
    /**
     * Searches for users based on the provided search criteria.
     * @param searchDTO Data transfer object containing search parameters.
     * @return List of UsersDTO representing the matching users.
     * @throws RuntimeException if search fails due to ExecutionException or InterruptedException.
     */
    public List<UsersDTO> searchUsers(UserSearchDTO searchDTO) {
        try {
            List<Users> users = userRepository.searchUsers(searchDTO);
            List<UsersDTO> userDTOs = users.stream().map(user -> user instanceof Students ? new UsersDTO(user, (Students) user) : new UsersDTO(user)).collect(Collectors.toList());
            log.info("Returning {} UserDTOs for search", userDTOs.size());
            return userDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to search users", e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
}

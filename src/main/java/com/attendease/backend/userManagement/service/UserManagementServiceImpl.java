package com.attendease.backend.userManagement.service;

import com.attendease.backend.userManagement.dto.*;
import com.attendease.backend.model.enums.AccountStatus;
import com.attendease.backend.model.enums.UserType;
import com.attendease.backend.model.students.Students;
import com.attendease.backend.model.users.Users;
import com.attendease.backend.authentication.student.repository.StudentAuthenticationRepository;
import com.attendease.backend.userManagement.dto.UserWithStudentInfo;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserManagementServiceImpl {
    private final StudentAuthenticationRepository studentRepository;
    private final UserRepository userRepository;
    private final Firestore firestore;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> REQUIRED_CSV_COLUMNS = Set.of("firstName", "lastName", "studentNumber", "password");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public UserManagementServiceImpl(
            Firestore firestore,
            StudentAuthenticationRepository studentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder)
    {
        this.firestore = firestore;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Imports students from CSV with improved validation and error handling
     */
    public List<UsersDTO> importStudentsViaCSV(MultipartFile file) throws IOException, CsvValidationException, ExecutionException, InterruptedException {
        validateCSVFile(file);
        List<UsersDTO> importedUsers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int rowNumber = 0;

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = csvReader.readNext();
            validateCSVHeader(header);

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    CSVRowData rowData = parseCSVRow(header, row);
                    if (!isValidRowData(rowData)) {
                        log.warn("Skipping row {} due to missing required fields", rowNumber);
                        errors.add("Row " + rowNumber + ": Missing required fields");
                        continue;
                    }

                    if (studentRepository.existsByStudentNumber(rowData.getStudentNumber())) {
                        log.warn("Skipping row {} due to duplicate studentNumber: {}", rowNumber, rowData.getStudentNumber());
                        errors.add("Row " + rowNumber + ": Duplicate student number " + rowData.getStudentNumber());
                        continue;
                    }

                    UsersDTO imported = createUserAndStudent(rowData);
                    importedUsers.add(imported);
                    log.info("Successfully imported student: {}", rowData.getStudentNumber());

                } catch (Exception e) {
                    log.error("Error processing CSV row {}: {}", rowNumber, String.join(",", row), e);
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            log.warn("CSV import completed with {} errors: {}", errors.size(), errors);
        }

        log.info("Successfully imported {} out of {} rows from CSV", importedUsers.size(), rowNumber);
        return importedUsers;
    }

    /**
     * retrieves all users
     */
    public List<UsersDTO> retrieveAllUsersService() {
        try {
            List<UserWithStudentInfo> usersWithInfo = userRepository.retrieveAllUsersWithStudentInfo();

            List<UsersDTO> userDTOs = usersWithInfo.stream()
                    .map(this::mapToUsersDTO)
                    .collect(Collectors.toList());

            log.info("Successfully retrieved and mapped {} users", userDTOs.size());
            return userDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to retrieve users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    /**
     * retrieves all students
     */
    public List<StudentDTO> retrieveAllStudentsService() {
        try {
            List<Students> students = studentRepository.retrieveAllStudents();
            log.info("Retrieved {} students from repository", students.size());

            List<StudentDTO> studentDTOs = students.stream()
                    .map(student -> {
                        StudentDTO dto = new StudentDTO(student);
                        log.debug("Mapped student to DTO: {}", dto);
                        return dto;
                    })
                    .collect(Collectors.toList());

            log.info("Successfully mapped {} students to DTOs", studentDTOs.size());
            return studentDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to retrieve students", e);
            throw new RuntimeException("Failed to retrieve students", e);
        }
    }

    /**
     * Updates user information
     */
    public UsersDTO updateUserInformationService(String userId, UpdateUserInfoDTO updateDTO) {
        try {
            UserWithStudentInfo updatedInfo = userRepository.updateUser(userId, updateDTO);
            UsersDTO userDTO = mapToUsersDTO(updatedInfo);

            log.info("Successfully updated user {} and mapped to UserDTO", userId);
            return userDTO;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to update user {}", userId, e);
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }

    /**
     * Deactivates a user
     */
    public UsersDTO deactivateUserService(String userId) {
        try {
            UserWithStudentInfo userInfo = userRepository.deactivateUser(userId);
            UsersDTO userDTO = mapToUsersDTO(userInfo);

            log.info("Successfully deactivated user {} and mapped to UserDTO", userId);
            return userDTO;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to deactivate user {}", userId, e);
            throw new RuntimeException("Failed to deactivate user: " + e.getMessage(), e);
        }
    }

    /**
     * Bulk deactivation
     */
    public List<UsersDTO> bulkDeactivationOnUsersService(BulkUserOperationsDTO bulkDTO) {
        try {
            List<UserWithStudentInfo> usersInfo = userRepository.deactivateUsers(bulkDTO.getUserIds());

            List<UsersDTO> userDTOs = usersInfo.stream()
                    .map(this::mapToUsersDTO)
                    .collect(Collectors.toList());

            log.info("Successfully deactivated {} users in bulk", userDTOs.size());
            return userDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to perform bulk deactivation", e);
            throw new RuntimeException("Failed to deactivate users: " + e.getMessage(), e);
        }
    }

    /**
     * Reactivates a user
     */
    public UsersDTO reactivateUserService(String userId) {
        try {
            UserWithStudentInfo userInfo = userRepository.reactivateUser(userId);
            UsersDTO userDTO = mapToUsersDTO(userInfo);

            log.info("Successfully reactivated user {} and mapped to UserDTO", userId);
            return userDTO;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to reactivate user {}", userId, e);
            throw new RuntimeException("Failed to reactivate user: " + e.getMessage(), e);
        }
    }

    /**
     * Enhanced user search with better performance
     */
    public List<UsersDTO> searchUsers(UserSearchDTO searchDTO) {
        try {
            List<UserWithStudentInfo> usersInfo = userRepository.searchUsers(searchDTO);
            List<UsersDTO> userDTOs = usersInfo.stream().map(this::mapToUsersDTO).collect(Collectors.toList());

            log.info("Successfully found {} users matching search criteria", userDTOs.size());
            return userDTOs;
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to search users", e);
            throw new RuntimeException("Failed to search users: " + e.getMessage(), e);
        }
    }

    // private helper methods
    private void validateCSVFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is required and cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file");
        }
    }

    private void validateCSVHeader(String[] header) {
        if (header == null || header.length == 0) {
            throw new IllegalArgumentException("CSV file is empty or has no header");
        }

        Set<String> headerSet = Set.of(header);
        List<String> missingColumns = REQUIRED_CSV_COLUMNS.stream()
                .filter(col -> !headerSet.contains(col))
                .collect(Collectors.toList());

        if (!missingColumns.isEmpty()) {
            throw new IllegalArgumentException("Missing required columns: " + String.join(", ", missingColumns));
        }
    }

    private CSVRowData parseCSVRow(String[] header, String[] row) {
        CSVRowData data = new CSVRowData();

        for (int i = 0; i < header.length && i < row.length; i++) {
            String value = (row[i] != null && !row[i].trim().isEmpty()) ? row[i].trim() : null;

            switch (header[i].toLowerCase()) {
                case "firstname":
                    data.setFirstName(value);
                    break;
                case "lastname":
                    data.setLastName(value);
                    break;
                case "password":
                    data.setPassword(value);
                    break;
                case "email":
                    data.setEmail(value);
                    break;
                case "studentnumber":
                    data.setStudentNumber(value);
                    break;
                case "section":
                    data.setSection(value);
                    break;
                case "yearlevel":
                    data.setYearLevel(value);
                    break;
                case "courserefid":
                    data.setCourseRefId(value);
                    break;
                case "birthdate":
                    data.setBirthdate(parseDate(value));
                    break;
                case "address":
                    data.setAddress(value);
                    break;
                case "contactnumber":
                    data.setContactNumber(value);
                    break;
                default:
                    log.debug("Ignoring unknown column: {}", header[i]);
                    break;
            }
        }

        return data;
    }

    private Date parseDate(String dateString) {
        if (dateString == null) return null;

        try {
            LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}. Expected format: yyyy-MM-dd", dateString);
            return null;
        }
    }

    private boolean isValidRowData(CSVRowData data) {
        return data.getFirstName() != null &&
                data.getLastName() != null &&
                data.getStudentNumber() != null &&
                data.getPassword() != null;
    }

    private UsersDTO createUserAndStudent(CSVRowData data) throws ExecutionException, InterruptedException {
        Users user = createUserFromRowData(data);
        userRepository.saveUser(user);
        log.info("Saved user: {}", user.getUserId());

        Students student = createStudentFromRowData(data, user.getUserId());
        studentRepository.saveStudent(student);
        log.info("Saved student for user: {}", user.getUserId());

        return new UsersDTO(user, student);
    }

    private Users createUserFromRowData(CSVRowData data) {
        Users user = new Users();
        user.setUserId(UUID.randomUUID().toString());
        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(UserType.SYSTEM);

        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setEmail(data.getEmail());
        user.setBirthdate(data.getBirthdate());
        user.setAddress(data.getAddress());
        user.setContactNumber(data.getContactNumber());

        if (data.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(data.getPassword()));
        }

        return user;
    }

    private Students createStudentFromRowData(CSVRowData data, String userId) {
        Students student = new Students();
        student.setUserRefId(firestore.collection("users").document(userId));
        student.setStudentNumber(data.getStudentNumber());
        student.setSection(data.getSection());
        student.setYearLevel(data.getYearLevel());

        if (data.getCourseRefId() != null) {
            try {
                student.setCourseRefId(firestore.document(data.getCourseRefId()));
            } catch (Exception e) {
                log.warn("Invalid courseRefId: {}, skipping", data.getCourseRefId());
            }
        }

        return student;
    }

    private UsersDTO mapToUsersDTO(UserWithStudentInfo userInfo) {
        if (userInfo.hasStudentInfo()) {
            return new UsersDTO(userInfo.getUser(), userInfo.getStudentInfo());
        } else {
            return new UsersDTO(userInfo.getUser());
        }
    }
}
package com.attendease.backend.osaModule.service.management.user;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.students.CSV.CSVRowData;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.students.UserStudent.UserStudentResponse;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersManagementService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SectionsRepository sectionsRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> REQUIRED_CSV_COLUMNS = Set.of("firstName", "lastName", "studentNumber", "password");

    /**
     * Imports students from CSV with improved validation and error handling
     */
    public List<Users> importStudentsViaCSV(MultipartFile file) {
        try {
            validateCSVFile(file);
            List<Users> importedUsers = new ArrayList<>();
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

                        Users imported = createUserAndStudent(rowData);
                        importedUsers.add(imported);
                        log.info("Successfully imported student: {}", rowData.getStudentNumber());
                    } catch (IllegalArgumentException e) {
                        log.error("Validation error processing CSV row {}: {}", rowNumber, String.join(",", row), e);
                        errors.add("Row " + rowNumber + ": " + e.getMessage());
                    } catch (Exception e) {
                        log.error("Unexpected error processing CSV row {}: {}", rowNumber, String.join(",", row), e);
                        errors.add("Row " + rowNumber + ": Unexpected error: " + e.getMessage());
                    }
                }
            }

            if (!errors.isEmpty()) {
                log.warn("CSV import completed with {} errors: {}", errors.size(), errors);
                throw new IllegalArgumentException("CSV import completed with errors: " + String.join("; ", errors));
            }

            log.info("Successfully imported {} out of {} rows from CSV", importedUsers.size(), rowNumber);
            return importedUsers;
        } catch (IOException | CsvValidationException e) {
            log.error("File or CSV parsing error", e);
            throw new IllegalArgumentException("File or CSV parsing error: " + e.getMessage(), e);
        }
    }

    /**
     * retrieves all users
     */
    public List<UserStudentResponse> retrieveUsersWithStudents() {
        List<Users> users = userRepository.findAll();
        log.info("Retrieved {} users", users.size());

        List<Students> students = studentRepository.findByUserIn(users);
        log.info("Retrieved {} students", students.size());

        Map<String, Students> studentMap = students.stream().collect(Collectors.toMap(s -> s.getUser().getUserId(), s -> s));

        return users
            .stream()
            .map(user -> mapToResponseDTO(user, studentMap.get(user.getUserId())))
            .collect(Collectors.toList());
    }

    /**
     * retrieves all students
     */
    public List<Students> retrieveAllStudent() {
        List<Students> students = studentRepository.findAll();
        log.info("Retrieved {} students from repository", students.size());
        return students;
    }

    public void deleteUserById(String userId) throws ExecutionException, InterruptedException {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found: " + userId);
        }

        userRepository.deleteById(userId);
    }

    /**
     * Deletes all student accounts (users and students) associated with the given section.
     *
     * @param sectionName the name of the section (e.g., "BSIT-401")
     * @throws ResponseStatusException if the section is not found
     */
    public void deleteStudentsBySection(String sectionName) {
        validateSectionFormat(sectionName);
        Optional<Sections> optSection = sectionsRepository.findByName(sectionName);
        if (optSection.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found: " + sectionName);
        }
        Sections section = optSection.get();
        List<Students> students = studentRepository.findBySection(section);
        if (students.isEmpty()) {
            log.info("No students found in section: {}", sectionName);
            return;
        }
        List<String> userIds = students.stream().map(student -> student.getUser().getUserId()).collect(Collectors.toList());
        userRepository.deleteAllById(userIds);
        studentRepository.deleteAll(students);
        log.info("Deleted {} students and their associated user accounts from section: {}", students.size(), sectionName);
    }

    /**
     * PRIVATE HELPERS
     */

    private void validateSectionFormat(String section) {
        if (section == null || section.trim().isEmpty()) {
            throw new IllegalArgumentException("Section is required and cannot be blank");
        }
        String trimmed = section.trim();
        if (!trimmed.matches("[A-Z]+-[0-9]+")) {
            throw new IllegalArgumentException("Invalid section format. Expected: COURSE_NAME-NUMBER (e.g., BSIT-101), got: " + trimmed);
        }
    }

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

    private boolean isValidRowData(CSVRowData data) {
        return data.getFirstName() != null && data.getLastName() != null && data.getStudentNumber() != null && data.getPassword() != null;
    }

    private Users createUserAndStudent(CSVRowData data) {
        Users user = createUserFromRowData(data);
        userRepository.save(user);
        log.info("Saved user: {}", user.getUserId());

        Students student = createStudentFromRowData(data, user);
        studentRepository.save(student);
        log.info("Saved student for user: {}", user.getUserId());

        return user;
    }

    private Users createUserFromRowData(CSVRowData data) {
        Users user = new Users();
        user.setUserId(UUID.randomUUID().toString());
        user.setUserType(UserType.STUDENT);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setUpdatedBy(String.valueOf(UserType.SYSTEM));

        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setEmail(data.getEmail());
        user.setContactNumber(data.getContactNumber());

        LocalDateTime now = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        if (data.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(data.getPassword()));
        }
        return user;
    }

    private Students createStudentFromRowData(CSVRowData data, Users user) {
        Students student = new Students();
        student.setUser(user);
        student.setStudentNumber(data.getStudentNumber());

        validateSectionFormat(data.getSection());

        Optional<Sections> optSection = sectionsRepository.findByName(data.getSection());
        if (optSection.isEmpty()) {
            throw new IllegalArgumentException("Section not found: " + data.getSection());
        }
        student.setSection(optSection.get());
        log.info("Assigned existing section '{}' to student '{}'", data.getSection(), data.getStudentNumber());

        return student;
    }

    public UserStudentResponse mapToResponseDTO(Users user, Students student) {
        Optional<Students> optStudent = Optional.ofNullable(student);
        Optional<Sections> optSection = optStudent.map(Students::getSection);
        Optional<Courses> optCourse = optSection.map(Sections::getCourse);
        Optional<Clusters> optCluster = optCourse.map(Courses::getCluster);

        return UserStudentResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .accountStatus(user.getAccountStatus())
                .userType(user.getUserType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .studentId(optStudent.map(Students::getId).orElse(null))
                .studentNumber(optStudent.map(Students::getStudentNumber).orElse(null))
                .section(optSection.map(Sections::getName).orElse(null))
                .course(optCourse.map(Courses::getCourseName).orElse(null))
                .cluster(optCluster.map(Clusters::getClusterName).orElse(null))
                .build();
    }
}
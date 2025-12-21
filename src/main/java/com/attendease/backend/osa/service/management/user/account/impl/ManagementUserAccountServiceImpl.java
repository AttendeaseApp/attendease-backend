package com.attendease.backend.osa.service.management.user.account.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.user.account.management.users.csv.row.UserAccountManagementUsersCSVRowData;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.exceptions.domain.ImportException.CsvImportError;
import com.attendease.backend.exceptions.domain.ImportException.CsvImportException;
import com.attendease.backend.osa.service.management.user.account.ManagementUserAccountService;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.validation.UserValidator;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementUserAccountServiceImpl implements ManagementUserAccountService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SectionsRepository sectionsRepository;
    private final BiometricsRepository biometricsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    private static final Set<String> REQUIRED_CSV_COLUMNS = Set.of("firstName", "lastName", "studentNumber", "password");

    @Override
    public List<User> importStudentsViaCSV(MultipartFile file) {
        try {
            validateCSVFile(file);
            List<User> importedUsers = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int rowNumber = 0;

            try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                String[] header = csvReader.readNext();
                validateCSVHeader(header);

                String[] row;
                while ((row = csvReader.readNext()) != null) {
                    rowNumber++;
                    try {
                        UserAccountManagementUsersCSVRowData rowData = parseCSVRow(header, row);

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

                        User imported = createUserAndStudent(rowData);
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
                List<CsvImportError> errorDetails = errors.stream()
                        .map(err -> {
                            String[] parts = err.split(": ", 2);
                            int row = Integer.parseInt(parts[0].replace("Row ", ""));
                            String message = parts[1];
                            return new CsvImportError(row, List.of(message));
                        })
                        .collect(Collectors.toList());
                throw new CsvImportException("CSV import completed with errors", errorDetails);
            }

            log.info("Successfully imported {} out of {} rows from CSV", importedUsers.size(), rowNumber);
            return importedUsers;
        } catch (IOException | CsvValidationException e) {
            log.error("File or CSV parsing error", e);
            throw new IllegalArgumentException("File or CSV parsing error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserStudentResponse> retrieveUsers() {
        List<User> users = userRepository.findAll();
        List<Students> students = studentRepository.findByUserIn(users);

        log.info("Retrieved {} students and {} users", students.size(), users.size());

        Map<String, Students> studentMap = students.stream().collect(Collectors.toMap(s -> s.getUser().getUserId(), s -> s));

        return users.stream().map(user -> mapToResponseDTO(user, studentMap.get(user.getUserId()))).collect(Collectors.toList());
    }

    @Override
    public List<UserStudentResponse> retrieveAllStudents() {
        List<Students> students = studentRepository.findAll();

        if (students.isEmpty()) {
            log.info("No students found");
            return Collections.emptyList();
        }

        List<String> userIds = students.stream().map(Students::getUserId).filter(Objects::nonNull).distinct().toList();

        List<User> users = userRepository.findAllById(userIds);
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));

        return students.stream().map(student -> mapToResponseDTO(userMap.get(student.getUserId()), student)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUserById(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found: " + userId);
        }

        studentRepository.findByUser_UserId(userId).ifPresent(student -> {
            if (student.getFacialData() != null) {
                biometricsRepository.deleteById(student.getFacialData().getFacialId());
            }
            studentRepository.deleteById(student.getId());
        });

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void deleteStudentsBySection(String sectionName) {
        userValidator.validateFullCourseSectionFormat(sectionName);
        Sections section = sectionsRepository.findBySectionName(sectionName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found: " + sectionName));

        List<Students> students = studentRepository.findBySection(section);

        if (students.isEmpty()) {
            log.info("No students found in section: {}", sectionName);
            return;
        }

        List<String> userIds = students.stream().map(Students::getUserId).toList();

        students.stream().filter(s -> s.getFacialData() != null).forEach(s -> biometricsRepository.deleteById(s.getFacialData().getFacialId()));

        studentRepository.deleteAll(students);
        userRepository.deleteAllById(userIds);

        log.info("Deleted {} students and their associated user accounts from section: {}", students.size(), sectionName);
    }

    /**
     * PRIVATE HELPERS
     */

    private boolean isValidRowData(UserAccountManagementUsersCSVRowData data) {
        return data.getFirstName() != null &&
                data.getLastName() != null &&
                data.getStudentNumber() != null &&
                data.getPassword() != null;
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
        List<String> missingColumns = REQUIRED_CSV_COLUMNS.stream().filter(col -> !headerSet.contains(col)).collect(Collectors.toList());

        if (!missingColumns.isEmpty()) {
            throw new IllegalArgumentException("Missing required columns: " + String.join(", ", missingColumns));
        }
    }

    private UserAccountManagementUsersCSVRowData parseCSVRow(String[] header, String[] row) {
        UserAccountManagementUsersCSVRowData data = new UserAccountManagementUsersCSVRowData();

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
                    data.setSectionName(value);
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

    private User createUserAndStudent(UserAccountManagementUsersCSVRowData data) {
        userValidator.validateFirstName(data.getFirstName(), "First name");
        userValidator.validateLastName(data.getLastName(), "Last name");
        userValidator.validateContactNumber(data.getContactNumber());
        userValidator.validatePassword(data.getPassword());
        userValidator.validateStudentNumber(data.getStudentNumber());
        userValidator.validateFullCourseSectionFormat(data.getSectionName());

        Sections section = sectionsRepository.findBySectionName(data.getSectionName())
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + data.getSectionName()));

        User user = User.builder()
                .userType(UserType.STUDENT)
                .accountStatus(AccountStatus.ACTIVE)
                .updatedBy(String.valueOf(UserType.SYSTEM))
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .email(data.getEmail())
                .contactNumber(data.getContactNumber())
                .password(passwordEncoder.encode(data.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        Students student = Students.builder()
                .user(savedUser)
                .userId(savedUser.getUserId())
                .studentNumber(data.getStudentNumber())
                .section(section)
                .sectionName(section.getSectionName())
                .build();

        studentRepository.save(student);

        log.info("Imported student saved: {}", savedUser.getUserId());
        return savedUser;
    }

    public UserStudentResponse mapToResponseDTO(User user, Students student) {
        Optional<Students> optStudent = Optional.ofNullable(student);
        Optional<Sections> optSection = optStudent.map(Students::getSection);
        Optional<Courses> optCourse = optSection.map(Sections::getCourse);
        Optional<Clusters> optCluster = optCourse.map(Courses::getCluster);
        Optional<BiometricData> optBiometric = optStudent.map(Students::getFacialData);

        return UserStudentResponse.builder()
                // parent data of USER/STUDENT
                .userId(user != null ? user.getUserId() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .email(user != null ? user.getEmail() : null)
                .contactNumber(user != null ? user.getContactNumber() : null)
                .accountStatus(user != null ? user.getAccountStatus() : null)
                .userType(user != null ? user.getUserType() : null)
                .createdAt(user != null ? user.getCreatedAt() : null)
                .updatedAt(user != null ? user.getUpdatedAt() : null)

                // basic data of STUDENT
                .studentId(optStudent.map(Students::getId).orElse(null))
                .studentNumber(optStudent.map(Students::getStudentNumber).orElse(null))

                // academic data of STUDENT
                .section(optSection.map(Sections::getSectionName).orElse(null))
                .sectionId(optSection.map(Sections::getId).orElse(null))
                .course(optCourse.map(Courses::getCourseName).orElse(null))
                .courseId(optCourse.map(Courses::getId).orElse(null))
                .cluster(optCluster.map(Clusters::getClusterName).orElse(null))
                .clusterId(optCluster.map(Clusters::getClusterId).orElse(null))

                // biometric data of STUDENT
                .biometricId(optBiometric.map(BiometricData::getFacialId).orElse(null))
                .biometricStatus(optBiometric.map(BiometricData::getBiometricsStatus).orElse(null))
                .biometricCreatedAt(optBiometric.map(BiometricData::getCreatedAt).orElse(null))
                .biometricLastUpdated(optBiometric.map(BiometricData::getLastUpdated).orElse(null))
                .hasBiometricData(optBiometric.isPresent())
                .build();
    }
}
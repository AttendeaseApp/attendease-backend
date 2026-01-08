package com.attendease.backend.osa.service.management.user.account.impl;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.exception.error.csv.CsvImportErrorResponse;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.user.account.management.users.csv.row.UserAccountManagementUsersCSVRowData;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.student.user.student.UserStudentResponse;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.exceptions.domain.ImportException.CsvImportException;
import com.attendease.backend.osa.service.management.user.account.ManagementUserAccountService;
import com.attendease.backend.osa.service.utility.csv.parser.UserCsvParser;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.section.SectionRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.validation.UserValidator;
import java.io.IOException;
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

// TODO: CACHING FOR QUERY OPTIMIZATIONS
@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementUserAccountServiceImpl implements ManagementUserAccountService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final BiometricsRepository biometricsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public List<User> importStudentsViaCSV(MultipartFile file) {
        validateCSVFile(file);
        List<UserAccountManagementUsersCSVRowData> rows;

        try {
            rows = UserCsvParser.parse(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid CSV file: " + e.getMessage(), e);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("CSV file contains no data rows");
        }

        List<User> importedUsers = new ArrayList<>();
        List<CsvImportErrorResponse.RowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 1;
            UserAccountManagementUsersCSVRowData row = rows.get(i);

            try {
                normalize(row);
                if (!isValidRowData(row)) {
                    errors.add(new CsvImportErrorResponse.RowError(rowNumber, List.of("Missing required fields")));
                    continue;
                }

                if (studentRepository.existsByStudentNumber(row.getStudentNumber())) {
                    errors.add(new CsvImportErrorResponse.RowError(
                            rowNumber,
                            List.of("Duplicate student number: " + row.getStudentNumber())
                    ));
                    continue;
                }

                User user = createUserAndStudent(row);
                importedUsers.add(user);
            } catch (IllegalArgumentException e) {
                errors.add(new CsvImportErrorResponse.RowError(rowNumber, List.of(e.getMessage())));
            } catch (Exception e) {
                log.error("Unexpected error at row {}", rowNumber, e);
                errors.add(new CsvImportErrorResponse.RowError(
                        rowNumber,
                        List.of("Unexpected error: " + e.getMessage())
                ));
            }
        }

        if (!errors.isEmpty()) {
            CsvImportErrorResponse errorResponse = CsvImportErrorResponse.fromErrors(errors, rows.size());
            throw new CsvImportException(errorResponse.getMessage(), errorResponse);
        }

        log.info("Successfully imported {} students", importedUsers.size());
        return importedUsers;
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
    public List<UserStudentResponse> retrieveActiveStudents() {
        return retrieveStudentsByAccountStatus(AccountStatus.ACTIVE);
    }

    @Override
    public List<UserStudentResponse> retrieveInactiveStudents() {
        return retrieveStudentsByAccountStatus(AccountStatus.INACTIVE);
    }

    @Override
    @Transactional
    public void bulkActivateStudents(List<String> userIds) {
        bulkUpdateStudentAccountStatus(userIds, AccountStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void bulkDeactivateStudents(List<String> userIds) {
        bulkUpdateStudentAccountStatus(userIds, AccountStatus.INACTIVE);
    }

    @Override
    public List<UserStudentResponse> retrieveAllStudents() {
        List<Students> students = studentRepository.findAll();
        if (students.isEmpty())
        {
            log.info("No students found");
            return Collections.emptyList();
        }
        List<String> userIds = students.stream()
                .map(Students::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<User> users = userRepository.findAllById(userIds);
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, u -> u));
        return students.stream()
                .map(student -> mapToResponseDTO(userMap.get(student.getUserId()), student))
                .collect(Collectors.toList());
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
        Section section = sectionRepository.findBySectionName(sectionName).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found: " + sectionName));

        List<Students> studentsByName = studentRepository.findBySectionName(sectionName);
        List<Students> studentsBySectionId = studentRepository.findByCurrentSectionId(section.getId());
        List<Students> studentsByDBRef = studentRepository.findBySection(section);
        Set<Students> allStudents = new HashSet<>();
        allStudents.addAll(studentsByName);
        allStudents.addAll(studentsBySectionId);
        allStudents.addAll(studentsByDBRef);

        List<Students> students = new ArrayList<>(allStudents);

        if (students.isEmpty()) {
            log.info("No students found in section: {}", sectionName);
            return;
        }
        List<String> userIds = students.stream().map(Students::getUserId).toList();
        students.stream()
                .filter(s -> s.getFacialData() != null)
                .forEach(s -> biometricsRepository.deleteById(s.getFacialData().getFacialId()));
        studentRepository.deleteAll(students);
        userRepository.deleteAllById(userIds);
        log.info("Deleted {} students and their associated user accounts from section: {}",
                students.size(), sectionName);
    }

    /**
     * PRIVATE HELPERS
     */

    private List<UserStudentResponse> retrieveStudentsByAccountStatus(AccountStatus status) {
        List<User> users = userRepository.findByUserTypeAndAccountStatus(UserType.STUDENT, status);
        if (users.isEmpty()) {
            log.info("No {} students found", status);
            return Collections.emptyList();
        }
        List<Students> students = studentRepository.findByUserIn(users);
        Map<String, Students> studentMap = students.stream().collect(Collectors.toMap(s -> s.getUser().getUserId(), s -> s));
        return users.stream()
                .map(user -> mapToResponseDTO(user, studentMap.get(user.getUserId())))
                .collect(Collectors.toList());
    }

    private void bulkUpdateStudentAccountStatus(List<String> userIds, AccountStatus targetStatus) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID list must not be empty");
        }

        List<User> users = userRepository.findAllById(userIds);

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found for the provided IDs");
        }

        List<User> nonStudents = users.stream().filter(u -> u.getUserType() != UserType.STUDENT).toList();

        if (!nonStudents.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bulk operation allowed for STUDENT accounts only");
        }

        List<User> toUpdate = users.stream()
                .filter(u -> u.getAccountStatus() != targetStatus)
                .peek(u -> {
                    u.setAccountStatus(targetStatus);
                    u.setUpdatedBy(String.valueOf(UserType.OSA));
                }).toList();

        if (toUpdate.isEmpty()) {
            log.info("No student accounts needed status update to {}", targetStatus);
            return;
        }

        userRepository.saveAll(toUpdate);
        log.info("Bulk updated {} student accounts to status {}", toUpdate.size(), targetStatus);
    }


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

    private void normalize(UserAccountManagementUsersCSVRowData row) {
        row.setFirstName(trim(row.getFirstName()));
        row.setLastName(trim(row.getLastName()));
        row.setEmail(lower(trim(row.getEmail())));
        row.setStudentNumber(trim(row.getStudentNumber()));
        row.setSectionName(trim(row.getSectionName()));
        row.setContactNumber(trim(row.getContactNumber()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String lower(String value) {
        return value == null ? null : value.toLowerCase();
    }

    private User createUserAndStudent(UserAccountManagementUsersCSVRowData data) {
        userValidator.validateFirstName(data.getFirstName(), "First name");
        userValidator.validateLastName(data.getLastName(), "Last name");
        userValidator.validateContactNumber(data.getContactNumber());
        userValidator.validatePassword(data.getPassword());
        userValidator.validateStudentNumber(data.getStudentNumber());

        Section section = null;
        String courseName = null;
        String clusterName = null;

        if (data.getSectionName() != null && !data.getSectionName().isBlank()) {
            userValidator.validateFullCourseSectionFormat(data.getSectionName());
            section = sectionRepository.findBySectionName(data.getSectionName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Section '" + data.getSectionName() + "' does not exist. " +
                                    "Please create this section first using the Academic Management, " +
                                    "then re-import the CSV."
                    ));
            if (section.getCourse() != null) {
                courseName = section.getCourse().getCourseName();
                if (section.getCourse().getCluster() != null) {
                    clusterName = section.getCourse().getCluster().getClusterName();
                }
            }
        }

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
                .currentSectionId(section != null ? section.getId() : null)
                .sectionName(section != null ? section.getSectionName() : null)
                .courseName(courseName)
                .clusterName(clusterName)
                .build();

        studentRepository.save(student);
        log.info("Imported student saved: {} (Section: {})", savedUser.getUserId(), section != null ? section.getSectionName() : "Not assigned");
        return savedUser;
    }

    public UserStudentResponse mapToResponseDTO(User user, Students student) {
        if (student == null) {
            return buildUserOnlyResponse(user);
        }

        Section fullSection = resolveSection(student);
        Optional<BiometricData> optBiometric = Optional.ofNullable(student.getFacialData());

        UserStudentResponse.UserStudentResponseBuilder builder = UserStudentResponse.builder()
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
                .studentId(student.getId())
                .studentNumber(student.getStudentNumber())

                // biometric data of STUDENT
                .biometricId(optBiometric.map(BiometricData::getFacialId).orElse(null))
                .biometricStatus(optBiometric.map(BiometricData::getBiometricsStatus).orElse(null))
                .biometricCreatedAt(optBiometric.map(BiometricData::getCreatedAt).orElse(null))
                .biometricLastUpdated(optBiometric.map(BiometricData::getLastUpdated).orElse(null))
                .hasBiometricData(optBiometric.isPresent());

        if (fullSection != null) {
            builder.section(fullSection.getSectionName())
                    .sectionId(fullSection.getId());

            if (fullSection.getCourse() != null) {
                builder.course(fullSection.getCourse().getCourseName())
                        .courseId(fullSection.getCourse().getId());

                if (fullSection.getCourse().getCluster() != null) {
                    builder.cluster(fullSection.getCourse().getCluster().getClusterName())
                            .clusterId(fullSection.getCourse().getCluster().getClusterId());
                }
            }
        } else {
            builder.section(student.getSectionName())
                    .sectionId(student.getCurrentSectionId())
                    .course(student.getCourseName())
                    .cluster(student.getClusterName());
        }

        return builder.build();
    }

    private Section resolveSection(Students student) {
        if (student.getCurrentSectionId() != null) {
            Optional<Section> sectionOpt = sectionRepository.findById(student.getCurrentSectionId());
            if (sectionOpt.isPresent()) {
                return sectionOpt.get();
            }
        }
        if (student.getSection() != null) {
            return student.getSection();
        }
        if (student.getSectionName() != null) {
            return sectionRepository.findBySectionName(student.getSectionName()).orElse(null);
        }
        return null;
    }

    private UserStudentResponse buildUserOnlyResponse(User user) {
        return UserStudentResponse.builder()
                .userId(user != null ? user.getUserId() : null)
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .email(user != null ? user.getEmail() : null)
                .contactNumber(user != null ? user.getContactNumber() : null)
                .accountStatus(user != null ? user.getAccountStatus() : null)
                .userType(user != null ? user.getUserType() : null)
                .createdAt(user != null ? user.getCreatedAt() : null)
                .updatedAt(user != null ? user.getUpdatedAt() : null)
                .hasBiometricData(false)
                .build();
    }
}
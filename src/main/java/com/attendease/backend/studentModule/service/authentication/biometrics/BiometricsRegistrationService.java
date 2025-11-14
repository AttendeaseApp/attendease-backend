package com.attendease.backend.studentModule.service.authentication.biometrics;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.enums.BiometricStatus;
import com.attendease.backend.repository.biometrics.BiometricsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.studentModule.dto.response.FacialEncodingResponse;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.studentModule.service.utils.BiometricImageRequestValidator;
import com.attendease.backend.studentModule.service.utils.FacialRecognitionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Service responsible for registering and managing facial biometric data for students.
 * <p>
 * This service handles validation of uploaded images, communication with an external
 * facial recognition API, and persistence of biometric data to the database.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BiometricsRegistrationService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final BiometricsRepository biometricsRepository;
    private final BiometricImageRequestValidator imageValidator;
    private final FacialRecognitionClient facialRecognitionClient;

    /**
     * Registers facial biometrics for a student associated with the given user ID.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *     <li>Validate uploaded image files.</li>
     *     <li>Retrieve the student number associated with the user ID.</li>
     *     <li>Call an external facial recognition service to extract facial encodings.</li>
     *     <li>Validate the API response and encoding data.</li>
     *     <li>Persist the biometric data to the database.</li>
     * </ol>
     *
     * @param userId the ID of the user whose facial biometrics are being registered
     * @param images a list of uploaded image files containing the student's face
     * @return a {@link ResponseEntity} with success or error message
     */
    public ResponseEntity<String> registerFacialBiometrics(String userId, List<MultipartFile> images) {
        ResponseEntity<String> validationResponse = imageValidator.validateImages(images);
        if (validationResponse != null) {
            return validationResponse;
        }

        String studentNumber = getStudentNumberByUserId(userId).orElseThrow(() -> new IllegalArgumentException("No student profile found for authenticated user"));

        FacialEncodingResponse apiResponse;
        try {
            apiResponse = facialRecognitionClient.extractFacialEncodings(images);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to process image files");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }

        if (apiResponse == null || !apiResponse.isSuccess()) {
            String errorMsg = (apiResponse != null && apiResponse.getMessage() != null) ? apiResponse.getMessage() : "Invalid response from facial recognition service";
            return ResponseEntity.badRequest().body("Face processing failed: " + StringEscapeUtils.escapeHtml4(errorMsg));
        }

        if (apiResponse.getFacialEncoding() == null || apiResponse.getFacialEncoding().isEmpty()) {
            return ResponseEntity.badRequest().body("Face processing failed: No encoding data received");
        }

        try {
            Students student = studentRepository.findByStudentNumber(studentNumber).orElseThrow(() -> new IllegalArgumentException("Student not found with student number: " + studentNumber));
            List<Double> encodings = apiResponse.getFacialEncoding();
            String result = saveBiometricsDataToDatabase(student, encodings);
            return ResponseEntity.ok(result);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Your facial biometrics is already registered");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid facial registration request");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


    /**
     * Saves the extracted facial encoding data for a student to the database.
     *
     * @param student       the student entity to associate with the biometric data
     * @param facialEncoding a list of 128 facial encoding values extracted from images
     * @return a success message indicating registration completion
     * @throws IllegalStateException    if the student already has registered biometrics
     * @throws IllegalArgumentException if the facial encoding is invalid
     */
    private String saveBiometricsDataToDatabase(Students student, List<Double> facialEncoding) {
        String studentNumber = student.getStudentNumber();

        Optional<BiometricData> existing = biometricsRepository.findByStudentNumber(studentNumber);
        if (existing.isPresent()) {
            throw new IllegalStateException("Facial biometrics already registered for this student.");
        }

        if (facialEncoding == null || facialEncoding.size() != 128) {
            throw new IllegalArgumentException("Facial encoding must contain exactly 128 elements");
        }

        BiometricData biometricData = BiometricData.builder()
                .facialId(String.valueOf(UUID.randomUUID()))
                .studentNumber(studentNumber)
                .facialEncoding(facialEncoding)
                .biometricsStatus(BiometricStatus.ACTIVE)
                .build();

        biometricsRepository.save(biometricData);

        log.debug("Saving facial biometric data for student {}", studentNumber);

        return "Your facial biometrics registered successfully";
    }

    /**
     * Retrieves the student number associated with a given user ID.
     *
     * @param userId the ID of the user
     * @return an {@link Optional} containing the student number if present, otherwise empty
     */
    public Optional<String> getStudentNumberByUserId(String userId) {
        return userRepository.findById(userId).flatMap(studentRepository::findByUser).map(Students::getStudentNumber);
    }
}

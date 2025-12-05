package com.attendease.backend.osa.controller.management.student.registration;

import com.attendease.backend.domain.students.Registration.Request.StudentRegistrationRequest;
import com.attendease.backend.osa.service.management.student.registration.StudentRegistrationService;
import com.attendease.backend.osa.service.management.student.registration.impl.StudentRegistrationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * {@code StudentRegistrationController} is used for managing student account registrations.
 *
 * <p>This controller provides endpoints for registering new student users, secured for OSA (Office of Student Affairs) role users only.
 * It handles request validation and delegates to the service layer for business logic, including entity creation and associations.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@RestController
@RequestMapping("/api/auth/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class StudentRegistrationController {

    private final StudentRegistrationService studentRegistrationService;

    /**
     * Registers a new student account based on the provided request details.
     *
     * <p>This endpoint validates the incoming request and creates a new student user account, including
     * linking to a section and course if specified. It is intended for administrative use by OSA users
     * to onboard new students.</p>
     *
     * <p><strong>Request Body:</strong> {@link StudentRegistrationRequest} containing user details such as first name,
     * last name, password, email, contact number, student number, and optional section (ID or name).</p>
     *
     * <p><strong>Response:</strong> HTTP 200 with a success confirmation message.</p>
     *
     * @param request the validated {@link StudentRegistrationRequest} object
     * @return {@link ResponseEntity} with status 200 and the registration success message
     * @throws IllegalArgumentException if validation fails, duplicate student number is detected, or section/course issues arise
     * @see StudentRegistrationService#registerNewStudentAccount(StudentRegistrationRequest)
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        String response = studentRegistrationService.registerNewStudentAccount(request);
        return ResponseEntity.ok(response);
    }
}

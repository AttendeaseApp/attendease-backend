package com.attendease.backend.osa.controller.management.osa.registration;

import com.attendease.backend.domain.users.OSA.Registration.Request.OsaRegistrationRequest;
import com.attendease.backend.osa.service.management.osa.registration.ManagementOSARegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * {@code OSARegistrationController} is used for managing Office of Student Affairs (OSA) account registrations.
 *
 * <p>This controller provides endpoints for registering new OSA users, secured for existing OSA role users only.
 * It handles request validation and delegates to the service layer for business logic.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/osa")
@PreAuthorize("hasRole('OSA')")
public class OSARegistrationController {

    private final ManagementOSARegistrationService managementOsaRegistrationService;

    /**
     * Registers a new OSA account based on the provided request details.
     *
     * <p>This endpoint validates the incoming request and creates a new OSA user account.
     * It is intended for administrative use by existing OSA users to onboard new team members.</p>
     *
     * <p><strong>Request Body:</strong> {@link OsaRegistrationRequest} containing user details such as first name,
     * last name, password, email, and contact number.</p>
     *
     * <p><strong>Response:</strong> HTTP 200 with a confirmation message including the generated user ID.</p>
     *
     * @param osaRegistrationRequest the validated {@link OsaRegistrationRequest} object
     * @return {@link ResponseEntity} with status 200 and the registration confirmation message
     * @throws IllegalArgumentException if validation fails or duplicate email is detected
     * @see ManagementOSARegistrationService#registerNewOsaAccount(OsaRegistrationRequest)
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerOsa(@Valid @RequestBody OsaRegistrationRequest osaRegistrationRequest) {
        return ResponseEntity.ok(managementOsaRegistrationService.registerNewOsaAccount(osaRegistrationRequest));
    }
}

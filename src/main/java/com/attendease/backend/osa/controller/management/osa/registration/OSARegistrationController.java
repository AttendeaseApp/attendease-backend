package com.attendease.backend.osa.controller.management.osa.registration;

import com.attendease.backend.domain.user.account.osa.registration.UserAccountOsaRegistrationRequest;
import com.attendease.backend.osa.service.management.osa.registration.ManagementOSARegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * {@code OSARegistrationController} is used for managing Office of Student Affairs (osa) account registrations.
 *
 * <p>This controller provides endpoints for registering new osa user, secured for existing osa role user only.
 * It handles request validation and delegates to the service layer for business logic.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/osa/account")
@PreAuthorize("hasRole('OSA')")
public class OSARegistrationController {

    private final ManagementOSARegistrationService managementOsaRegistrationService;

    /**
     * Registers a new osa account based on the provided request details.
     *
     * <p>This endpoint validates the incoming request and creates a new osa user account.
     * It is intended for administrative use by existing osa user to onboard new team members.</p>
     *
     * <p><strong>Request Body:</strong> {@link UserAccountOsaRegistrationRequest} containing user details such as first name,
     * last name, password, email, and contact number.</p>
     *
     * <p><strong>Response:</strong> HTTP 200 with a confirmation message including the generated user ID.</p>
     *
     * @param userAccountOsaRegistrationRequest the validated {@link UserAccountOsaRegistrationRequest} object
     * @return {@link ResponseEntity} with status 200 and the registration confirmation message
     * @throws IllegalArgumentException if validation fails or duplicate email is detected
     * @see ManagementOSARegistrationService#registerNewOsaAccount(UserAccountOsaRegistrationRequest)
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerOsa(@Valid @RequestBody UserAccountOsaRegistrationRequest userAccountOsaRegistrationRequest) {
        return ResponseEntity.ok(managementOsaRegistrationService.registerNewOsaAccount(userAccountOsaRegistrationRequest));
    }
}

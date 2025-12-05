package com.attendease.backend.osa.service.management.osa.registration;

import com.attendease.backend.domain.users.OSA.Registration.Request.OsaRegistrationRequest;

/**
 * {@link ManagementOSARegistrationService} is a service responsible for handling Office of Student Affairs (OSA) account registrations.
 *
 * <p>Provides methods to create new OSA user account, including validation of user details, password encoding,
 * and duplicate email checks. Ensures new account are set to {@link com.attendease.backend.domain.enums.AccountStatus#ACTIVE}
 * with {@link com.attendease.backend.domain.enums.UserType#OSA} type.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-26
 */
public interface ManagementOSARegistrationService {

    /**
     * {@code registerNewOsaAccount} is used to register a new OSA account based on the provided request details.
     * Performs validations on first name, last name, password, email, and contact number. Checks for existing
     * email to prevent duplicates. Encodes the password and persists the user with ACTIVE status and OSA type.
     *
     * @param request the {@link OsaRegistrationRequest} containing user details (first name, last name, password, email, contact number)
     * @return a confirmation message including the generated user ID (e.g., "Added OSA with id: {userId}")
     * @throws IllegalArgumentException if validation fails (e.g., invalid fields or duplicate email)
     */
    String registerNewOsaAccount(OsaRegistrationRequest request);
}

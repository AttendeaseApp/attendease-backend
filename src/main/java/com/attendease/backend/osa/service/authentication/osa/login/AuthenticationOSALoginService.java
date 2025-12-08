package com.attendease.backend.osa.service.authentication.osa.login;

/**
 * {@code AuthenticationOSALoginService } is a service responsible for handling OSA-specific login authentication requests.
 *
 * <p>This service validates user credentials and generates
 * an authentication token for authorized users.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-25
 */
public interface AuthenticationOSALoginService {

    /**
     * {@code loginOSA} Attempts to authenticate an Office of Student Affairs (OSA) user using the provided email and password.
     *
     * @param email the email address of an Office of Student Affairs (OSA) user
     * @param password the raw (unencrypted) password an Office of Student Affairs (OSA) user
     * @return a JWT token and the email of the OSA if authentication succeeds
     *
     * @throws IllegalArgumentException if the email does not exist or the password does not match
     */
    String loginOSA(String email, String password);
}

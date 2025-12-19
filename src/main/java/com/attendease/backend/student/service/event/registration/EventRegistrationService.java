package com.attendease.backend.student.service.event.registration;

import com.attendease.backend.domain.events.Registration.Request.EventRegistrationRequest;

/**
 * EventRegistrationServiceImpl is responsible for handling student event registrations.
 * <p>
 * This includes:
 * <ul>
 *     <li>Validating event registration time windows</li>
 *     <li>Confirming physical location proximity</li>
 *     <li>Performing biometric (facial) verification</li>
 *     <li>Ensuring the student is not already registered</li>
 *     <li>Creating attendance records upon successful registration</li>
 * </ul>
 * </p>
 */
public interface EventRegistrationService {

    /**
     * Registers a student for an event after validating:
     * <ul>
     *     <li>Event availability</li>
     *     <li>Location boundaries</li>
     *     <li>Duplicate registration prevention</li>
     *     <li>Biometric facial verification</li>
     * </ul>
     *
     * @param authenticatedUserId the ID of the currently authenticated user
     * @param registrationRequest the registration request containing event, location, and biometric data
     * @return the original registration request if successful
     *
     * @throws IllegalStateException if the user, student, event, location, or biometrics are invalid,
     *                               or if registration conditions are not met
     */
    EventRegistrationRequest eventRegistration(String authenticatedUserId, EventRegistrationRequest registrationRequest);
}

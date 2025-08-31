package com.attendease.backend.eventCheckInDiscovery.service;

import com.attendease.backend.eventMonitoring.dto.EventCheckInDto;
import java.util.concurrent.ExecutionException;

/**
 * Abstraction for event check-in operations.
 */
public interface EventCheckInServiceInterface {
    EventCheckInDto checkInStudent(String studentNumber, EventCheckInDto checkInDTO) throws ExecutionException, InterruptedException;
}

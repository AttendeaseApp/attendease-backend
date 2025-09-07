package com.attendease.backend.attendanceTrackingService.service.AttendanceTrackingService;

import com.attendease.backend.eventAttendanceMonitoringService.dto.EventCheckInDto;

import java.util.concurrent.ExecutionException;

/**
 * Abstraction for event check-in operations.
 */
public interface AttendanceTrackingServiceInterface {
    EventCheckInDto checkInStudent(String studentNumber, EventCheckInDto checkInDTO) throws ExecutionException, InterruptedException;
}

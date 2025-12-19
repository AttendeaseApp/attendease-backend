package com.attendease.backend.student.service.attendance.history;

import com.attendease.backend.domain.attendance.History.Response.AttendanceHistoryResponse;

import java.util.List;

/**
 * Service responsible for retrieving event attendance records of a student for the student module.
 * <p>
 * Provides methods for:
 * <ul>
 *     <li>Retrieving attendance records of a stundent</li>
 * </ul>
 * </p>
 */
public interface AttendanceHistoryService {

    /**
     * Retrieves all attendance records for the student associated with the given authenticated user ID.
     *
     * @param authenticatedUserId the ID of the currently authenticated user
     * @return a list of {@link AttendanceHistoryResponse} containing the student's attendance history
     * @throws IllegalStateException if the user or corresponding student record cannot be found
     */
    List<AttendanceHistoryResponse> getAttendanceHistoryForStudent(String authenticatedUserId);
}

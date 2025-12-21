package com.attendease.backend.student.controller.attendance.history;

import com.attendease.backend.domain.attendance.History.Response.AttendanceHistoryResponse;
import com.attendease.backend.student.service.attendance.history.AttendanceHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for fetching attendance history of authenticated student.
 * <p>
 * All endpoints in this controller are restricted to user with the 'STUDENT' role.
 * </p>
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AttendanceHistoryController {

    private final AttendanceHistoryService attendanceHistoryService;

    /**
     * Retrieves the attendance history of the authenticated student.
     *
     * @param authentication the Spring Security authentication object containing the current user's details
     * @return a {@link ResponseEntity} containing a list of {@link AttendanceHistoryResponse} objects
     */
    @GetMapping("/history")
    public ResponseEntity<List<AttendanceHistoryResponse>> getStudentAttendanceHistory(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        return ResponseEntity.ok(attendanceHistoryService.getAttendanceHistoryForStudent(authenticatedUserId));
    }
}

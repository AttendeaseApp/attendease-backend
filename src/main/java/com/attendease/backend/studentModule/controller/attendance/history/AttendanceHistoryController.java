package com.attendease.backend.studentModule.controller.attendance.history;

import com.attendease.backend.domain.attendance.History.Response.AttendanceHistoryResponse;
import com.attendease.backend.studentModule.service.attendance.history.AttendanceHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class AttendanceHistoryController {

    private final AttendanceHistoryService attendanceHistoryService;

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceHistoryResponse>> getStudentAttendanceHistory(Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        return ResponseEntity.ok(attendanceHistoryService.getAttendanceHistoryForStudent(authenticatedUserId));
    }
}

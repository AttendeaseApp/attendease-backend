package com.attendease.backend.studentModule.service.attendance.history;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.AttendanceHistoryResponse;
import com.attendease.backend.domain.students.Students;
import com.attendease.backend.domain.users.Users;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AttendanceHistoryService {

    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public List<AttendanceHistoryResponse> getAttendanceHistoryForStudent(String authenticatedUserId) {
        Users user = userRepository.findById(authenticatedUserId).orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        Students student = studentRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));

        List<AttendanceRecords> records = attendanceRecordsRepository.findByStudentId(student.getId());

        return records
            .stream()
            .filter(record -> record.getEvent() != null)
            .map(record ->
                AttendanceHistoryResponse.builder()
                    .eventId(record.getEvent().getEventId())
                    .eventName(record.getEvent().getEventName())
                    .timeIn(record.getTimeIn())
                    .timeOut(record.getTimeOut())
                    .attendanceStatus(record.getAttendanceStatus())
                    .reason(record.getReason())
                    .build()
            )
            .toList();
    }
}

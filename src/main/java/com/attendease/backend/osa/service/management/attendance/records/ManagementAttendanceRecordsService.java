package com.attendease.backend.osa.service.management.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link ManagementAttendanceRecordsService} is a service responsible for managing attendance records of student.
 *
 * <p>Provides methods to retrieve ongoing and finalized events, fetch attendee details, and update attendance statuses
 * with audit logging support.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Nov-11
 */
public interface ManagementAttendanceRecordsService {

    List<FinalizedAttendanceRecordsResponse> getFinalizedEvents();

    EventAttendeesResponse getAttendeesByEvent(String eventId);

    List<AttendanceRecords> getAttendanceRecordsByStudentId(String studentId);

    AttendanceRecords updateAttendanceStatus(String studentId, String eventId, AttendanceStatus status, String reason, String updatedByUserId);

    List<AttendanceRecords> getAllAttendanceRecords();

    void deleteAttendanceRecordById(String recordId);

    void deleteAllAttendanceRecords();
}

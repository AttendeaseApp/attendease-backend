package com.attendease.backend.repository.attendanceRecords;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.students.Students;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordsRepository extends MongoRepository<AttendanceRecords, String> {
    List<AttendanceRecords> findByStudentAndEventAndLocationAndAttendanceStatus(Students student, EventSessions event, EventLocations location, AttendanceStatus status);
    List<AttendanceRecords> findByEventEventId(String eventId);
    Optional<AttendanceRecords> findByStudentAndEventAndLocation(Students student, EventSessions event, EventLocations location);
}

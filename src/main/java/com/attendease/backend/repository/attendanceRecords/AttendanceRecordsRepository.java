package com.attendease.backend.repository.attendanceRecords;

import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.records.AttendanceRecords;
import com.attendease.backend.domain.students.Students;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordsRepository extends MongoRepository<AttendanceRecords, String> {
    List<AttendanceRecords> findByStudentAndEventAndLocationAndAttendanceStatus(Students student, EventSessions event, EventLocations location, AttendanceStatus status);
    List<AttendanceRecords> findByEvent_Id(String eventId);
}

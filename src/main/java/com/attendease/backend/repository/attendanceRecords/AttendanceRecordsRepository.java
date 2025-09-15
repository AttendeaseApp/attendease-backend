package com.attendease.backend.repository.attendanceRecords;

import com.attendease.backend.model.enums.AttendanceStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.locations.EventLocations;
import com.attendease.backend.model.records.AttendanceRecords;
import com.attendease.backend.model.students.Students;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordsRepository extends MongoRepository<AttendanceRecords, String> {
    List<AttendanceRecords> findByStudentAndEventAndLocationAndAttendanceStatus(Students student, EventSessions event, EventLocations location, AttendanceStatus status);
    List<AttendanceRecords> findByEvent_Id(String eventId);
}

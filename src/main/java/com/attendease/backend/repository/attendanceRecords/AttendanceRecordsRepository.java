package com.attendease.backend.repository.attendanceRecords;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.students.Students;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on {@link AttendanceRecords} documents in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard data access methods such as
 * {@code save}, {@code findAll}, {@code findById}, and {@code delete}.
 * </p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Repository
public interface AttendanceRecordsRepository extends MongoRepository<AttendanceRecords, String> {
    /**
     * Retrieves a list of attendance records filtered by student, event, location, and attendance status.
     *
     * @param student the student whose attendance records are being queried
     * @param event the event session associated with the attendance records
     * @param location the event location associated with the attendance records
     * @param status the attendance status to filter by (e.g., PRESENT, ABSENT)
     * @return a list of {@link AttendanceRecords} matching the given criteria
     */
    List<AttendanceRecords> findByStudentAndEventAndLocationAndAttendanceStatus(Students student, EventSessions event, EventLocations location, AttendanceStatus status);

    /**
     * Retrieves a list of attendance records associated with a specific event ID.
     *
     * @param eventId the unique identifier of the event
     * @return a list of {@link AttendanceRecords} for the given event
     */
    List<AttendanceRecords> findByEventEventId(String eventId);

    /**
     * Retrieves an optional attendance record for a specific student, event, and location.
     *
     * @param student the student whose attendance record is being queried
     * @param event the event session associated with the attendance record
     * @param location the event location associated with the attendance record
     * @return an {@link Optional} containing the {@link AttendanceRecords} if found, otherwise empty
     */
    Optional<AttendanceRecords> findByStudentAndEventAndLocation(Students student, EventSessions event, EventLocations location);

    /**
     * Retrieves a list of attendance records for a specific student by their student ID.
     *
     * @param studentId the unique identifier of the student
     * @return a list of {@link AttendanceRecords} associated with the student
     */
    List<AttendanceRecords> findByStudentId(String studentId);

    /**
     * Retrieves an optional attendance record for a specific student ID and event ID.
     *
     * @param studentId the unique identifier of the student
     * @param eventId the unique identifier of the event session
     * @return an {@link Optional} containing the {@link AttendanceRecords} if found, otherwise empty
     */
    Optional<AttendanceRecords> findByStudentIdAndEventEventId(String studentId, String eventId);
}

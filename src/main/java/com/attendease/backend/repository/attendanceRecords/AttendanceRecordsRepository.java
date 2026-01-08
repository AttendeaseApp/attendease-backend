package com.attendease.backend.repository.attendanceRecords;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.student.Students;

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
     * Retrieves a list of attendance records associated with a specific event ID.
     *
     * @param eventId the unique identifier of the event
     * @return a list of {@link AttendanceRecords} for the given event
     */
    List<AttendanceRecords> findByEventEventId(String eventId);

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

    Long countByEventLocationId(String eventLocationId);

    Long countByEventEventId(String id);

    Optional<AttendanceRecords> findByStudentAndEvent(Students student, Event event);

    List<AttendanceRecords> findByEventEventIdAndStudentUserIdIn(String eventId, List<String> activeUserIds);
}

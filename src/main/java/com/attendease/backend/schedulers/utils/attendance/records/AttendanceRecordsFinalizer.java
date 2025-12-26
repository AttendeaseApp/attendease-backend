package com.attendease.backend.schedulers.utils.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceRecordsFinalizer {

    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final StudentRepository studentRepository;
    private final SectionsRepository sectionRepository;
    private final CourseRepository courseRepository;

    /**
     * Re-evaluates and finalizes attendance based on ping logs.
     * A student is marked PRESENT if they were inside for at least 70% of the event duration.
     */
    public void finalizeAttendanceForEvent(Event event) {
        String eventId = event.getEventId();
        String eventName = event.getEventName();

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventId(eventId);
        List<Students> expectedStudents = getExpectedStudentsForEvent(event);
        Set<String> studentsWithRecords = attendanceRecords.stream().map(r -> r.getStudent().getId()).collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecords record : attendanceRecords) {
            AttendanceStatus oldStatus = record.getAttendanceStatus();
            AttendanceStatus finalStatus = evaluateAttendanceFromLogs(event, record);

            if (finalStatus == AttendanceStatus.PRESENT) {
                AttendanceStatus lateStatus = evaluateLateAttendees(event, record);
                if (lateStatus == AttendanceStatus.LATE) {
                    finalStatus = AttendanceStatus.LATE;
                    log.info("Adjusted attendance to LATE for student {} (arrived after event started) in event {}", record.getStudent().getStudentNumber(), eventName);
                }
            }

            if (finalStatus != oldStatus) {
                record.setAttendanceStatus(finalStatus);
                record.setTimeOut(now);
                attendanceRecordsRepository.save(record);
                log.info("Finalized attendance for student {} as {} in event {}", record.getStudent().getStudentNumber(), finalStatus, eventName);
            }
        }

        // mark missing student as ABSENT
        for (Students student : expectedStudents) {
            if (!studentsWithRecords.contains(student.getId())) {
                AttendanceRecords absentRecord = AttendanceRecords.builder()
                        .student(student)
                        .event(event)
                        .attendanceStatus(AttendanceStatus.ABSENT)
                        .reason("No attendance recorded – may have missed the event or not registered in time.")
                        .timeIn(null)
                        .timeOut(null)
                        .build();
                attendanceRecordsRepository.save(absentRecord);
                log.info("Recorded as absent for student {} in event {}, {}", student.getStudentNumber(), eventId, eventName);
            }
        }
        log.info("Attendance finalization completed for event {}, {}", eventId, eventName);
    }

    /**
     * Uses attendance ping logs to decide the student's final attendance.
     */
    private AttendanceStatus evaluateAttendanceFromLogs(Event event, AttendanceRecords record) {
        List<AttendanceTrackingResponse> pings = record.getAttendancePingLogs();
        if (pings == null || pings.isEmpty()) {
            record.setReason("No location updates were detected during the event.");
            return AttendanceStatus.ABSENT;
        }

        long eventStart = event.getStartingDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long eventEnd = event.getEndingDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long eventDuration = eventEnd - eventStart;
        long insideTime = computeInsideDuration(pings, eventStart, eventEnd);

        double insideRatio = (double) insideTime / eventDuration;
        double percentage = insideRatio * 100;
        log.info("Student {} was present for approximately {}% of event {}", record.getStudent().getStudentNumber(), percentage, event.getEventName());

        if (insideRatio >= 0.7) {
            record.setReason(null);
            return AttendanceStatus.PRESENT;
        } else if (insideRatio >= 0.3) {
            record.setReason(String.format("Partially attended the event – present for %.1f%% of the time.", percentage));
            return AttendanceStatus.IDLE;
        } else {
            record.setReason(String.format("Minimal attendance – present for only %.1f%% of the time.", percentage));
            return AttendanceStatus.ABSENT;
        }
    }

    private AttendanceStatus evaluateLateAttendees(Event event, AttendanceRecords record) {
        if (record.getTimeIn() == null || !record.getTimeIn().isAfter(event.getStartingDateTime())) {
            return null;
        }
        record.setReason("Arrived late to the event after it started at " + record.getTimeIn());
        return AttendanceStatus.LATE;
    }

    /**
     * Estimates how long (in ms) the student was inside based on pings.
     * This assumes pings are roughly evenly spaced in time.
     */
    private long computeInsideDuration(List<AttendanceTrackingResponse> pings, long eventStart, long eventEnd) {
        if (pings.size() < 2) return 0;

        pings.sort(Comparator.comparingLong(AttendanceTrackingResponse::getTimestamp));

        long totalInside = 0;

        for (int i = 0; i < pings.size() - 1; i++) {
            AttendanceTrackingResponse current = pings.get(i);
            AttendanceTrackingResponse next = pings.get(i + 1);

            long t1 = Math.max(current.getTimestamp(), eventStart);
            long t2 = Math.min(next.getTimestamp(), eventEnd);

            if (current.isInside()) {
                totalInside += (t2 - t1);
            }
        }

        return totalInside;
    }

    private List<Students> getExpectedStudentsForEvent(Event event) {
        EventEligibility criteria = event.getEligibleStudents();
        if (criteria == null || criteria.isAllStudents()) {
            return studentRepository.findAll();
        }
        if (!CollectionUtils.isEmpty(criteria.getSections())) {
            List<Students> expectedStudents = studentRepository.findBySectionIdIn(criteria.getSections());
            log.info("Total expected student for event {}: {} (from {} sections)",
                    event.getEventId(), expectedStudents.size(), criteria.getSections().size());
            return expectedStudents;
        }
        Set<Students> uniqueStudents = new HashSet<>();

        if (!CollectionUtils.isEmpty(criteria.getCourses())) {
            List<Sections> courseSections = sectionRepository.findByCourseIdIn(criteria.getCourses());
            List<String> allSectionIds = courseSections.stream().map(Sections::getId).collect(Collectors.toList());
            if (!allSectionIds.isEmpty()) {
                List<Students> courseStudents = studentRepository.findBySectionIdIn(allSectionIds);
                uniqueStudents.addAll(courseStudents);
            }
        }

        if (!CollectionUtils.isEmpty(criteria.getClusters())) {
            List<Course> clusterCourses = courseRepository.findByClusterClusterIdIn(criteria.getClusters());
            List<String> allCourseIds = clusterCourses.stream().map(Course::getId).collect(Collectors.toList());
            if (!allCourseIds.isEmpty()) {
                List<Sections> clusterSections = sectionRepository.findByCourseIdIn(allCourseIds);
                List<String> allClusterSectionIds = clusterSections.stream().map(Sections::getId).collect(Collectors.toList());
                if (!allClusterSectionIds.isEmpty()) {
                    List<Students> clusterStudents = studentRepository.findBySectionIdIn(allClusterSectionIds);
                    uniqueStudents.addAll(clusterStudents);
                }
            }
        }

        List<Students> expected = new ArrayList<>(uniqueStudents);
        log.info("Total expected student for event {}: {} (fallback from courses/clusters)", event.getEventId(), expected.size());
        return expected;
    }
}

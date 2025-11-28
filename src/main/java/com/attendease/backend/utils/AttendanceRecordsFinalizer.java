package com.attendease.backend.utils;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.students.Students;
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
    public void finalizeAttendanceForEvent(EventSessions event) {
        String eventId = event.getEventId();
        String eventName = event.getEventName();

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventId(eventId);
        List<Students> expectedStudents = getExpectedStudentsForEvent(event);
        Set<String> studentsWithRecords = attendanceRecords
            .stream()
            .map(r -> r.getStudent().getId())
            .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecords record : attendanceRecords) {
            AttendanceStatus oldStatus = record.getAttendanceStatus();
            AttendanceStatus finalStatus = evaluateAttendanceFromLogs(event, record);

            if (finalStatus == AttendanceStatus.PRESENT) {
                AttendanceStatus lateStatus = evaluateLateAttendees(event, record);
                if (lateStatus == AttendanceStatus.LATE) {
                    finalStatus = AttendanceStatus.LATE;
                    log.info("Overrode to LATE for student {} (arrived after start) in event {}", record.getStudent().getStudentNumber(), eventName);
                }
            }

            if (finalStatus != oldStatus) {
                record.setAttendanceStatus(finalStatus);
                record.setTimeOut(now);
                attendanceRecordsRepository.save(record);
                log.info("Updated student {} to {} for event {}", record.getStudent().getStudentNumber(), finalStatus, eventName);
            }
        }

        // mark missing students as ABSENT
        for (Students student : expectedStudents) {
            if (!studentsWithRecords.contains(student.getId())) {
                AttendanceRecords absentRecord = AttendanceRecords.builder()
                    .student(student)
                    .event(event)
                    .attendanceStatus(AttendanceStatus.ABSENT)
                    .reason("Missing, no any presence found and did not registered at all")
                    .timeIn(null)
                    .timeOut(null)
                    .build();
                attendanceRecordsRepository.save(absentRecord);
                log.info("Marked ABSENT for missing student {} in event {}, {}", student.getStudentNumber(), eventId, eventName);
            }
        }
        log.info("Attendance finalization completed for event {}, {}", eventId, eventName);
    }

    /**
     * Uses attendance ping logs to decide the student's final attendance.
     */
    private AttendanceStatus evaluateAttendanceFromLogs(EventSessions event, AttendanceRecords record) {
        List<AttendanceTrackingResponse> pings = record.getAttendancePingLogs();
        if (pings == null || pings.isEmpty()) {
            record.setReason("No location pings from student mobile were recorded");
            return AttendanceStatus.ABSENT;
        }

        long eventStart = event.getStartDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long eventEnd = event.getEndDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long eventDuration = eventEnd - eventStart;
        long insideTime = computeInsideDuration(pings, eventStart, eventEnd);

        double insideRatio = (double) insideTime / eventDuration;
        log.info("Student {} inside {}% of event {}", record.getStudent().getStudentNumber(), insideRatio * 100, event.getEventName());

        if (insideRatio >= 0.7) {
            record.setReason(null);
            return AttendanceStatus.PRESENT;
        } else if (insideRatio >= 0.3) {
            record.setReason("Present only for part of the event");
            return AttendanceStatus.IDLE;
        } else {
            record.setReason("Outside for most of the event");
            return AttendanceStatus.ABSENT;
        }
    }

    private AttendanceStatus evaluateLateAttendees(EventSessions event, AttendanceRecords record) {
        if (record.getTimeIn() == null || !record.getTimeIn().isAfter(event.getStartDateTime())) {
            return null;
        }
        record.setReason("Late attendee: Arrived after event start at " + record.getTimeIn());
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

    private List<Students> getExpectedStudentsForEvent(EventSessions event) {
        EligibilityCriteria criteria = event.getEligibleStudents();
        if (criteria == null || criteria.isAllStudents()) {
            return studentRepository.findAll();
        }
        Set<Students> uniqueStudents = new HashSet<>();

        if (!CollectionUtils.isEmpty(criteria.getSections())) {
            List<Students> sectionStudents = studentRepository.findBySectionIdIn(criteria.getSections());
            uniqueStudents.addAll(sectionStudents);
            log.debug("Fetched {} students from {} sections for event {}", sectionStudents.size(), criteria.getSections().size(), event.getEventId());
        }

        if (!CollectionUtils.isEmpty(criteria.getCourse())) {
            List<Sections> courseSections = sectionRepository.findByCourseIdIn(criteria.getCourse());
            List<String> allSectionIds = courseSections.stream().map(Sections::getId).collect(Collectors.toList());
            if (!allSectionIds.isEmpty()) {
                List<Students> courseStudents = studentRepository.findBySectionIdIn(allSectionIds);
                uniqueStudents.addAll(courseStudents);
                log.debug("Fetched {} students from courses {} for event {}", courseStudents.size(), criteria.getCourse().size(), event.getEventId());
            }
        }

        if (!CollectionUtils.isEmpty(criteria.getCluster())) {
            List<Courses> clusterCourses = courseRepository.findByClusterClusterIdIn(criteria.getCluster());
            List<String> allCourseIds = clusterCourses.stream().map(Courses::getId).collect(Collectors.toList());
            if (!allCourseIds.isEmpty()) {
                List<Sections> clusterSections = sectionRepository.findByCourseIdIn(allCourseIds);
                List<String> allClusterSectionIds = clusterSections.stream().map(Sections::getId).collect(Collectors.toList());
                if (!allClusterSectionIds.isEmpty()) {
                    List<Students> clusterStudents = studentRepository.findBySectionIdIn(allClusterSectionIds);
                    uniqueStudents.addAll(clusterStudents);
                    log.debug("Fetched {} students from clusters {} for event {}", clusterStudents.size(), criteria.getCluster().size(), event.getEventId());
                }
            }
        }

        List<Students> expected = new ArrayList<>(uniqueStudents);
        log.info("Total expected students for event {}: {}", event.getEventId(), expected.size());
        return expected;
    }
}

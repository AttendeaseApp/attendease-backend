package com.attendease.backend.schedulers.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.section.SectionRepository;
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
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;

    /**
     * Re-evaluates and finalizes attendance based on ping logs.
     * If attendance location monitoring is disabled, students who registered are marked as PRESENT.
     * If enabled, a student is marked PRESENT if they were inside for at least 70% of the event duration.
     * PARTIALLY_REGISTERED students who never reached the venue are marked as ABSENT.
     */
    public void finalizeAttendanceForEvent(Event event) {
        String eventId = event.getEventId();
        String eventName = event.getEventName();
        boolean locationMonitoringEnabled = event.getAttendanceLocationMonitoringEnabled() != null
                && event.getAttendanceLocationMonitoringEnabled();

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventId(eventId);
        List<Students> expectedStudents = getExpectedStudentsForEvent(event);
        Set<String> studentsWithRecords = attendanceRecords.stream()
                .map(r -> r.getStudent().getId())
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        for (AttendanceRecords record : attendanceRecords) {
            AttendanceStatus oldStatus = record.getAttendanceStatus();
            AttendanceStatus finalStatus;

            if (oldStatus == AttendanceStatus.PARTIALLY_REGISTERED) {
                finalStatus = AttendanceStatus.ABSENT;
                record.setReason("Checked in at registration area but never entered the event venue.");
                log.info("Student {} marked as ABSENT (was PARTIALLY_REGISTERED) for event {}",
                        record.getStudent().getStudentNumber(), eventName);
            } else if (!locationMonitoringEnabled) {
                finalStatus = evaluateAttendanceWithoutMonitoring(event, record);
                log.info("Location monitoring disabled for event {}. Student {} marked as {}",
                        eventName, record.getStudent().getStudentNumber(), finalStatus);
            } else {
                finalStatus = evaluateAttendanceFromLogs(event, record);

                if (finalStatus == AttendanceStatus.PRESENT) {
                    AttendanceStatus lateStatus = evaluateLateAttendees(event, record);
                    if (lateStatus == AttendanceStatus.LATE) {
                        finalStatus = AttendanceStatus.LATE;
                        log.info("Adjusted attendance to LATE for student {} (arrived after event started) in event {}",
                                record.getStudent().getStudentNumber(), eventName);
                    }
                }
            }

            if (finalStatus != oldStatus) {
                record.setAttendanceStatus(finalStatus);
                record.setTimeOut(now);
                attendanceRecordsRepository.save(record);
                log.info("Finalized attendance for student {} as {} in event {}",
                        record.getStudent().getStudentNumber(), finalStatus, eventName);
            }
        }

        // mark missing students as ABSENT
        for (Students student : expectedStudents) {
            if (!studentsWithRecords.contains(student.getId())) {
                AttendanceRecords absentRecord = AttendanceRecords.builder()
                        .student(student)
                        .event(event)
                        .location(null)
                        .eventLocationId(null)
                        .academicYear(event.getAcademicYear())
                        .academicYearId(event.getAcademicYearId())
                        .academicYearName(event.getAcademicYearName())
                        .semester(event.getSemester())
                        .semesterName(event.getSemesterName())
                        .attendanceStatus(AttendanceStatus.ABSENT)
                        .reason("No attendance recorded – may have missed the event or not registered in time.")
                        .timeIn(null)
                        .timeOut(null)
                        .build();
                attendanceRecordsRepository.save(absentRecord);
                log.info("Recorded as absent for student {} in event {}, {} (Academic Year: {}, Semester: {})",
                        student.getStudentNumber(), eventId, eventName,
                        event.getAcademicYearName(), event.getSemesterName());
            }
        }
        log.info("Attendance finalization completed for event {}, {}", eventId, eventName);
    }


    private AttendanceStatus evaluateAttendanceWithoutMonitoring(Event event, AttendanceRecords record) {
        if (record.getTimeIn() != null && record.getTimeIn().isAfter(event.getStartingDateTime())) {
            record.setReason("Arrived late to the event at " + record.getTimeIn());
            return AttendanceStatus.LATE;
        }
        record.setReason(null);
        return AttendanceStatus.PRESENT;
    }


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
        List<Students> expectedStudents;

        if (criteria == null || criteria.isAllStudents()) {
            expectedStudents = studentRepository.findAll();
        } else {
            List<String> sectionsToCheck = criteria.getSelectedSections() != null && !criteria.getSelectedSections().isEmpty()
                    ? criteria.getSelectedSections()
                    : criteria.getSections();

            List<String> coursesToCheck = criteria.getSelectedCourses() != null && !criteria.getSelectedCourses().isEmpty()
                    ? criteria.getSelectedCourses()
                    : criteria.getCourses();

            List<String> clustersToCheck = criteria.getSelectedClusters() != null && !criteria.getSelectedClusters().isEmpty()
                    ? criteria.getSelectedClusters()
                    : criteria.getClusters();

            Set<Students> uniqueStudents = new HashSet<>();

            if (!CollectionUtils.isEmpty(sectionsToCheck)) {
                uniqueStudents.addAll(studentRepository.findBySectionIdIn(sectionsToCheck));
            }
            else if (!CollectionUtils.isEmpty(coursesToCheck)) {
                List<Section> courseSections = sectionRepository.findByCourseIdIn(coursesToCheck);
                List<String> sectionIds = courseSections.stream().map(Section::getId).toList();
                if (!sectionIds.isEmpty()) {
                    uniqueStudents.addAll(studentRepository.findBySectionIdIn(sectionIds));
                }
            }
            else if (!CollectionUtils.isEmpty(clustersToCheck)) {
                List<Course> clusterCourses = courseRepository.findByClusterClusterIdIn(clustersToCheck);
                List<String> courseIds = clusterCourses.stream().map(Course::getId).toList();
                if (!courseIds.isEmpty()) {
                    List<Section> clusterSections = sectionRepository.findByCourseIdIn(courseIds);
                    List<String> sectionIds = clusterSections.stream().map(Section::getId).toList();
                    if (!sectionIds.isEmpty()) {
                        uniqueStudents.addAll(studentRepository.findBySectionIdIn(sectionIds));
                    }
                }
            }

            if (criteria.getTargetYearLevels() != null && !criteria.getTargetYearLevels().isEmpty()) {
                uniqueStudents = uniqueStudents.stream()
                        .filter(student -> student.getSection() != null
                                && criteria.getTargetYearLevels().contains(student.getSection().getYearLevel()))
                        .collect(Collectors.toSet());
                log.info("Filtered expected students by year levels {} for event {}",
                        criteria.getTargetYearLevels(), event.getEventId());
            }
            expectedStudents = new ArrayList<>(uniqueStudents);
        }

        expectedStudents = expectedStudents.stream().filter(s -> s.getUser() != null && s.getUser().getAccountStatus() == AccountStatus.ACTIVE).toList();
        log.info("Total expected ACTIVE students for event {}: {}", event.getEventId(), expectedStudents.size());
        return expectedStudents;
    }
}

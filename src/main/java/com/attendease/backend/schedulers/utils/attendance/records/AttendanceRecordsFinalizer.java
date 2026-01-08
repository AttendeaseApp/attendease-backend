package com.attendease.backend.schedulers.utils.attendance.records;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Tracking.Response.AttendanceTrackingResponse;
import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.enums.AccountStatus;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.UserType;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.section.SectionRepository;
import com.attendease.backend.repository.students.StudentRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.attendease.backend.repository.users.UserRepository;
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
    private final UserRepository userRepository;
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

        List<String> activeUserIds = userRepository.findByUserTypeAndAccountStatus(UserType.STUDENT, AccountStatus.ACTIVE)
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        List<AttendanceRecords> attendanceRecords = attendanceRecordsRepository.findByEventEventIdAndStudentUserIdIn(eventId, activeUserIds);

        List<Students> expectedStudents = getExpectedStudentsForEvent(event, activeUserIds);
        Set<String> studentsWithRecords = attendanceRecords.stream()
                .map(r -> r.getStudent().getId())
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        List<AttendanceRecords> changedRecords = new ArrayList<>();
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
                if (finalStatus == AttendanceStatus.PRESENT || finalStatus == AttendanceStatus.LATE) {
                    record.setTimeOut(now);
                } else {
                    record.setTimeOut(null);
                }
                changedRecords.add(record);
                log.info("Finalized attendance for student {} as {} in event {}",
                        record.getStudent().getStudentNumber(), finalStatus, eventName);
            }
        }

        if (!changedRecords.isEmpty()) {
            attendanceRecordsRepository.saveAll(changedRecords);
        }

        // Mark missing active students as ABSENT
        List<AttendanceRecords> absentRecords = new ArrayList<>();
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
                absentRecords.add(absentRecord);
                log.info("Recorded as absent for student {} in event {}, {} (Academic Year: {}, Semester: {})",
                        student.getStudentNumber(), eventId, eventName,
                        event.getAcademicYearName(), event.getSemesterName());
            }
        }
        if (!absentRecords.isEmpty()) {
            attendanceRecordsRepository.saveAll(absentRecords);
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


    private List<Students> getExpectedStudentsForEvent(Event event, List<String> activeUserIds) {
        EventEligibility criteria = event.getEligibleStudents();
        List<Students> expectedStudents;

        if (criteria == null || criteria.isAllStudents()) {
            expectedStudents = studentRepository.findByUserIdIn(activeUserIds);
        } else if (!CollectionUtils.isEmpty(criteria.getSections())) {
            expectedStudents = studentRepository.findBySectionIdInAndUserIdIn(criteria.getSections(), activeUserIds);
        } else {
            Set<Students> uniqueStudents = new HashSet<>();

            if (!CollectionUtils.isEmpty(criteria.getCourses())) {
                List<Section> courseSections = sectionRepository.findByCourseIdIn(criteria.getCourses());
                List<String> sectionIds = courseSections.stream().map(Section::getId).collect(Collectors.toList());
                if (!sectionIds.isEmpty()) {
                    uniqueStudents.addAll(studentRepository.findBySectionIdInAndUserIdIn(sectionIds, activeUserIds));
                }
            }

            if (!CollectionUtils.isEmpty(criteria.getClusters())) {
                List<Course> clusterCourses = courseRepository.findByClusterClusterIdIn(criteria.getClusters());
                List<String> courseIds = clusterCourses.stream().map(Course::getId).collect(Collectors.toList());
                if (!courseIds.isEmpty()) {
                    List<Section> clusterSections = sectionRepository.findByCourseIdIn(courseIds);
                    List<String> sectionIds = clusterSections.stream().map(Section::getId).collect(Collectors.toList());
                    if (!sectionIds.isEmpty()) {
                        uniqueStudents.addAll(studentRepository.findBySectionIdInAndUserIdIn(sectionIds, activeUserIds));
                    }
                }
            }
            expectedStudents = new ArrayList<>(uniqueStudents);
        }

        log.info("Total expected ACTIVE students for event {}: {}", event.getEventId(), expectedStudents.size());
        return expectedStudents;
    }
}

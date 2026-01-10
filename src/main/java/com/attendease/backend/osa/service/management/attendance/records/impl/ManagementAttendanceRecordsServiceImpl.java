package com.attendease.backend.osa.service.management.attendance.records.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.osa.service.management.attendance.records.ManagementAttendanceRecordsService;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.event.EventRepository;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public final class ManagementAttendanceRecordsServiceImpl implements ManagementAttendanceRecordsService {

    private final EventRepository eventRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    @Override
    public List<FinalizedAttendanceRecordsResponse> getFinalizedEvents() {
        List<Event> finalizedEvents = eventRepository.findByEventStatusIn(List.of(EventStatus.FINALIZED));
        List<FinalizedAttendanceRecordsResponse> responses = new ArrayList<>();

        for (Event event : finalizedEvents) {
            List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(event.getEventId());
            long totalPresent = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.PRESENT)
                .count();
            long totalAbsent = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.ABSENT)
                .count();
            long totalIdle = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.IDLE)
                .count();
            long totalLate = records
                .stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceStatus.LATE)
                .count();

            String locationName = event.getVenueLocation() != null ? event.getVenueLocation().getLocationName() : null;

            FinalizedAttendanceRecordsResponse response = FinalizedAttendanceRecordsResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .locationName(locationName)
                .timeInRegistrationStartDateTime(event.getRegistrationDateTime())
                .startDateTime(event.getStartingDateTime())
                .endDateTime(event.getEndingDateTime())
                .eventStatus(event.getEventStatus())
                .totalPresent((int) totalPresent)
                .totalAbsent((int) totalAbsent)
                .totalIdle((int) totalIdle)
                .totalLate((int) totalLate)
                .build();
            responses.add(response);
        }

        return responses;
    }

    @Override
    public EventAttendeesResponse getAttendeesByEvent(String eventId) {
        List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);
        List<AttendeesResponse> attendees = records
            .stream()
            .filter(Objects::nonNull)
            .filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
            .map(record -> {
                var student = record.getStudent();
                var user = student.getUser();
                var section = student.getSection();
                String sectionName = (section != null) ? section.getSectionName() : "";
                String courseName = (section != null && section.getCourse() != null) ? section.getCourse().getCourseName() : "";
                String clusterName = (section != null && section.getCourse() != null && section.getCourse().getCluster() != null) ? section.getCourse().getCluster().getClusterName() : "";

                return AttendeesResponse.builder()
                    .userId(user.getUserId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .contactNumber(user.getContactNumber())
                    .accountStatus(user.getAccountStatus())
                    .userType(user.getUserType())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .studentId(student.getId())
                    .studentNumber(student.getStudentNumber())
                    .sectionName(sectionName)
                    .courseName(courseName)
                    .clusterName(clusterName)
                    .attendanceStatus(record.getAttendanceStatus())
                    .reason(record.getReason())
                    .timeIn(record.getTimeIn())
                    .timeOut(record.getTimeOut())
                    .attendanceRecordId(record.getRecordId())
                    .build();
            })
            .distinct()
            .toList();
        return EventAttendeesResponse.builder().totalAttendees(attendees.size()).attendees(attendees).build();
    }

    @Override
    public List<AttendanceRecords> getAttendanceRecordsByStudentId(String studentId) {
        return attendanceRecordsRepository.findByStudentId(studentId);
    }

    @Override
    public AttendanceRecords updateAttendanceStatus(String studentId, String eventId, AttendanceStatus status, String reason, String updatedByUserId) {
        Optional<AttendanceRecords> optionalRecord = attendanceRecordsRepository.findByStudentIdAndEventEventId(studentId, eventId);
        if (optionalRecord.isEmpty()) {
            throw new RuntimeException("Attendance record not found for student ID: " + studentId + " and event ID: " + eventId);
        }

        AttendanceRecords record = optionalRecord.get();
        record.setAttendanceStatus(status);
        if (reason != null) {
            record.setReason(reason);
        }
        record.setUpdatedByUserId(updatedByUserId);

        return attendanceRecordsRepository.save(record);
    }

    @Override
    public List<AttendanceRecords> getAllAttendanceRecords() {
        return attendanceRecordsRepository.findAll();
    }

    @Override
    public void deleteAttendanceRecordById(String recordId) {
        if (!attendanceRecordsRepository.existsById(recordId)) {
            throw new RuntimeException("Attendance record not found: " + recordId);
        }
        attendanceRecordsRepository.deleteById(recordId);
        log.info("Deleted attendance record: {}", recordId);
    }

    @Override
    public void deleteAllAttendanceRecords() {
        long count = attendanceRecordsRepository.count();
        if (count > 0) {
            attendanceRecordsRepository.deleteAll();
            log.warn("Deleted all {} attendance records", count);
        }
    }
}

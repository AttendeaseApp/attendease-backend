package com.attendease.backend.attendanceTrackingService.service.attendanceTrackingService;

import com.attendease.backend.attendanceTrackingService.dto.LocationInfoDto;
import com.attendease.backend.attendanceTrackingService.repository.AttendanceRepository;
import com.attendease.backend.model.enums.AttendanceStatus;
import com.attendease.backend.model.events.EventSessions;
import com.attendease.backend.model.records.AttendanceRecords;
import com.google.cloud.firestore.DocumentReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AutomatedAttendanceTrackingService {

    private final ConcurrentHashMap<String, LocationInfoDto> locationMap = new ConcurrentHashMap<>();
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AutomatedAttendanceTrackingService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public void updateLocation(String studentNumber, LocationInfoDto locationInfo) {
        log.debug("Updating location for student [{}] with info: {}", studentNumber, locationInfo);
        locationMap.put(studentNumber, locationInfo);
    }

    public AttendanceStatus evaluateAttendance(EventSessions event, AttendanceRecords record, LocationInfoDto locationInfo) {
        Date now = new Date();
        Date eventStart = event.getStartDateTime();
        Date eventEnd = event.getEndDateTime();
        Date checkInTime = record.getTimeIn();

        log.debug("Evaluating attendance: studentTimeIn={}, eventStart={}, eventEnd={}, locationInfo={}",
                checkInTime, eventStart, eventEnd, locationInfo);

        // late check-in
        if (checkInTime != null && checkInTime.after(eventStart)) {
            log.info("Student was late for event. Marking as ABSENT.");
            return AttendanceStatus.ABSENT;
        }

        // left location for >10min before event end
        if (locationInfo.getLastExitTime() != null &&
                locationInfo.getLastExitTime().before(new Date(eventEnd.getTime() - 10 * 60 * 1000)) &&
                (locationInfo.getLastReturnTime() == null ||
                        locationInfo.getLastReturnTime().after(new Date(locationInfo.getLastExitTime().getTime() + 10 * 60 * 1000)))) {
            log.info("Student left early for more than 10 minutes. Marking as ABSENT.");
            return AttendanceStatus.ABSENT;
        }

        // stayed until event end
        if (locationInfo.isPresentAtLocation() && now.after(eventEnd)) {
            log.info("Student stayed at location until event end. Marking as PRESENT.");
            return AttendanceStatus.PRESENT;
        }

        log.debug("Attendance status not yet determinable. Continuing to monitor.");
        return null;
    }

    /**
     * Updates and evaluates attendance, then persists it to Firestore if status is determinable.
     */
    public void processAndPersistAttendance(
            String studentNumber,
            DocumentReference studentRef,
            DocumentReference eventRef,
            DocumentReference locationRef,
            DocumentReference updatedByUserRef,
            LocationInfoDto locationInfo,
            EventSessions event,
            AttendanceRecords existingRecord
    ) {
        try {
            log.info("Processing attendance for student [{}] and event [{}]", studentNumber, eventRef.getId());

            updateLocation(studentNumber, locationInfo);

            AttendanceStatus status = evaluateAttendance(event, existingRecord, locationInfo);

            if (status != null) {
                existingRecord.setStudentNumberRefId(studentRef);
                existingRecord.setEventRefId(eventRef);
                existingRecord.setLocationRefId(locationRef);
                existingRecord.setAttendanceStatus(status);
                existingRecord.setUpdatedByUserRefId(updatedByUserRef);
                existingRecord.setTimeOut(new Date());

                log.info("Saving attendance record: {}", existingRecord);
                attendanceRepository.saveAttendance(existingRecord);
                log.info("Attendance record saved for student [{}]", studentNumber);
            } else {
                log.debug("Attendance status not final. Record will not be saved yet.");
            }

        } catch (Exception e) {
            log.error("Failed to process attendance for student [{}] and event [{}]: {}",
                    studentNumber,
                    eventRef != null ? eventRef.getId() : "unknown",
                    e.getMessage(),
                    e
            );
        }
    }

    public AttendanceRecords getExistingAttendance(DocumentReference studentRef, DocumentReference eventRef) {
        try {
            log.debug("Fetching existing attendance for studentRef [{}], eventRef [{}]",
                    studentRef.getPath(), eventRef.getPath());

            AttendanceRecords record = attendanceRepository.getAttendanceByStudentAndEvent(studentRef, eventRef);

            if (record != null) {
                log.info("Existing attendance found for student [{}] and event [{}]", studentRef.getId(), eventRef.getId());
            } else {
                log.info("No existing attendance found for student [{}] and event [{}]", studentRef.getId(), eventRef.getId());
            }

            return record;

        } catch (Exception e) {
            log.error("Error retrieving existing attendance record: {}", e.getMessage(), e);
            return null;
        }
    }
}

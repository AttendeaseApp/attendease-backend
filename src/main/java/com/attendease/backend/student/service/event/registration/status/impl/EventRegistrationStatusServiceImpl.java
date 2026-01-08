package com.attendease.backend.student.service.event.registration.status.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.registration.EventRegistrationStatusResponse;
import com.attendease.backend.domain.student.Students;
import com.attendease.backend.domain.user.User;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.repository.users.UserRepository;
import com.attendease.backend.student.service.event.registration.status.EventRegistrationStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for checking event registration status.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventRegistrationStatusServiceImpl implements EventRegistrationStatusService {

	private final UserRepository userRepository;
	private final StudentRepository studentRepository;
	private final EventRepository eventRepository;
	private final AttendanceRecordsRepository attendanceRecordsRepository;

	@Override
	public EventRegistrationStatusResponse checkRegistrationStatus(String authenticatedUserId, String eventId) {

		User user = userRepository.findById(authenticatedUserId)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
		Students student = studentRepository.findByUser(user)
				.orElseThrow(() -> new IllegalStateException("Student record not found for authenticated user"));
		Event event = eventRepository.findById(eventId)
				.orElseThrow(() -> new IllegalStateException("Event not found"));

		Optional<AttendanceRecords> attendanceRecord = attendanceRecordsRepository
				.findByStudentAndEvent(student, event);

		if (attendanceRecord.isPresent()) {
			AttendanceRecords record = attendanceRecord.get();

			log.info("Student {} is registered for event {} with status {}",
					student.getStudentNumber(),
					eventId,
					record.getAttendanceStatus());

			return EventRegistrationStatusResponse.builder()
					.isRegistered(true)
					.attendanceStatus(record.getAttendanceStatus())
					.registrationTime(record.getTimeIn())
					.attendanceRecordId(record.getRecordId())
					.registrationLocationName(record.getLocation() != null ?
							record.getLocation().getLocationName() : "Unknown")
					.academicYearName(record.getAcademicYearName())
					.semester(record.getSemester())
					.message(buildRegistrationMessage(record.getAttendanceStatus()))
					.build();
		}

		log.info("Student {} is not registered for event {}",
				student.getStudentNumber(),
				eventId);

		return EventRegistrationStatusResponse.builder()
				.isRegistered(false)
				.message("You are not registered for this event yet.")
				.build();
	}

	private String buildRegistrationMessage(AttendanceStatus status) {
		return switch (status) {
			case REGISTERED -> "You are registered for this event.";
			case LATE -> "You registered late for this event.";
			case PRESENT -> "You attended this event.";
			case ABSENT -> "You were marked absent for this event.";
			case PARTIALLY_REGISTERED -> "You are currently marked as partially registered for this event.";
			case IDLE -> "Your attendance is being tracked.";
			case EXCUSED -> "You have an excused absence for this event.";
		};
	}
}

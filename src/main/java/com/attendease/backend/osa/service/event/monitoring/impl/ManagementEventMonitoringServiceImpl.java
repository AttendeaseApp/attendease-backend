package com.attendease.backend.osa.service.event.monitoring.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.osa.service.event.monitoring.ManagementEventMonitoringService;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.event.EventRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagementEventMonitoringServiceImpl implements ManagementEventMonitoringService {

	private final EventRepository eventRepository;
	private final AttendanceRecordsRepository attendanceRecordsRepository;

	@Override
	public List<Event> getEventWithUpcomingRegistrationOngoingStatuses() {
		return eventRepository.findByEventStatusIn(List.of(EventStatus.UPCOMING, EventStatus.REGISTRATION, EventStatus.ONGOING));
	}

	@Override
	public EventAttendeesResponse getAttendeesByEventWithRegisteredAttendanceStatus(String eventId) {
		List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);

		List<AttendeesResponse> attendees = records
				.stream()
				.filter(Objects::nonNull)
				.filter(record -> record.getAttendanceStatus() == AttendanceStatus.REGISTERED || record.getAttendanceStatus() == AttendanceStatus.LATE)
				.filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
				.map(this::mapToAttendeeResponse)
				.distinct()
				.toList();

		return EventAttendeesResponse.builder().totalAttendees(attendees.size()).attendees(attendees).build();
	}

	private AttendeesResponse mapToAttendeeResponse(AttendanceRecords record) {
		var student = record.getStudent();
		var user = student.getUser();
		var section = student.getSection();
		var course = section != null ? section.getCourse() : null;
		var cluster = course != null ? course.getCluster() : null;

		return AttendeesResponse.builder()
				.attendanceRecordId(record.getRecordId())
				.userId(user.getUserId())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.email(user.getEmail())
				.contactNumber(user.getContactNumber())
				.accountStatus(user.getAccountStatus())
				.userType(user.getUserType())
				.attendanceStatus(record.getAttendanceStatus())
				.reason(record.getReason() != null ? record.getReason() : "")
				.timeIn(record.getTimeIn())
				.timeOut(record.getTimeOut())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.studentId(student.getId())
				.studentNumber(student.getStudentNumber())
				.yearLevel(student.getYearLevel())
				.sectionName(section != null ? section.getSectionName() : "")
				.courseName(course != null ? course.getCourseName() : "")
				.clusterName(cluster != null ? cluster.getClusterName() : "")
				.build();
	}
}

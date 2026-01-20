package com.attendease.backend.osa.service.attendance.records.impl;

import com.attendease.backend.domain.attendance.AttendanceRecords;
import com.attendease.backend.domain.attendance.History.Response.FinalizedAttendanceRecordsResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import com.attendease.backend.domain.attendance.Monitoring.Records.Management.Response.EventAttendeesResponse;
import com.attendease.backend.domain.attendance.sorted.SortedAttendanceRecordsResponse;
import com.attendease.backend.domain.enums.AttendanceStatus;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.enums.attendance.AttendanceSortCriteria;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.osa.service.attendance.records.AttendanceRecordsManagementService;
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
public final class AttendanceRecordsManagementServiceImpl implements AttendanceRecordsManagementService {

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

			String venueLocationName = event.getVenueLocation() != null ? event.getVenueLocation().getLocationName() : null;
			String registrationLocationName = event.getVenueLocation() != null ? event.getRegistrationLocation().getLocationName() : null;

			FinalizedAttendanceRecordsResponse response = FinalizedAttendanceRecordsResponse.builder()
					.eventId(event.getEventId())
					.eventName(event.getEventName())
					.registrationLocationName(registrationLocationName).venueLocationName(venueLocationName)
					.registrationDateTime(event.getRegistrationDateTime())
					.startingDateTime(event.getStartingDateTime())
					.endingDateTime(event.getEndingDateTime())
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
	public List<FinalizedAttendanceRecordsResponse> getFinalizedEventsByAcademicYear(String academicYearId) {
		List<Event> finalizedEvents = eventRepository.findByEventStatusInAndAcademicYearId(
				List.of(EventStatus.FINALIZED), academicYearId);

		return finalizedEvents.stream()
				.map(this::mapEventToFinalizedResponse)
				.toList();
	}

	@Override
	public List<FinalizedAttendanceRecordsResponse> getFinalizedEventsBySemester(String academicYearId, Integer semester) {
		List<Event> finalizedEvents = eventRepository.findByEventStatusInAndAcademicYearIdAndSemester(
				List.of(EventStatus.FINALIZED), academicYearId, semester);

		return finalizedEvents.stream()
				.map(this::mapEventToFinalizedResponse)
				.toList();
	}

	@Override
	public void deleteByAcademicYear(String academicYearId) {
		List<Event> events = eventRepository.findByAcademicYearId(academicYearId);

		for (Event event : events) {
			List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(event.getEventId());
			if (!records.isEmpty()) {
				attendanceRecordsRepository.deleteAll(records);
				log.info("Deleted {} attendance records for event {}", records.size(), event.getEventId());
			}
		}

		log.warn("Deleted all attendance records linked to academic year: {}", academicYearId);
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
							.yearLevel(section != null ? section.getYearLevel() : null)
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
	public SortedAttendanceRecordsResponse getSortedAttendanceRecords(String eventId, AttendanceSortCriteria sortCriteria) {
		log.info("Retrieving sorted attendance records for event: {} by {}", eventId, sortCriteria);
		Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
		List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(eventId);
		List<AttendeesResponse> attendees = records
				.stream()
				.filter(Objects::nonNull)
				.filter(record -> record.getStudent() != null && record.getStudent().getUser() != null)
				.map(record -> {
					var student = record.getStudent();
					var user = student.getUser();
					var section = student.getSection();
					String sectionName = (section != null) ? section.getSectionName() : "No Section";
					String courseName = (section != null && section.getCourse() != null) ? section.getCourse().getCourseName() : "No Course";
					String clusterName = (section != null && section.getCourse() != null && section.getCourse().getCluster() != null)
							? section.getCourse().getCluster().getClusterName() : "No Cluster";
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
							.yearLevel(section != null ? section.getYearLevel() : null)
							.attendanceStatus(record.getAttendanceStatus())
							.reason(record.getReason())
							.timeIn(record.getTimeIn())
							.timeOut(record.getTimeOut())
							.attendanceRecordId(record.getRecordId())
							.build();
				})
				.distinct()
				.toList();

		Map<String, SortedAttendanceRecordsResponse.GroupedAttendees> groupedMap;

		switch (sortCriteria) {
			case CLUSTER:
				groupedMap = attendees.stream()
						.collect(Collectors.groupingBy(
								AttendeesResponse::getClusterName,
								LinkedHashMap::new,
								Collectors.collectingAndThen(
										Collectors.toList(),
										list -> SortedAttendanceRecordsResponse.GroupedAttendees.builder()
												.groupName(list.isEmpty() ? "Unknown" : list.get(0).getClusterName())
												.clusterName(list.isEmpty() ? "Unknown" : list.get(0).getClusterName())
												.count(list.size())
												.attendees(list)
												.build()
								)
						));
				break;

			case COURSE:
				groupedMap = attendees.stream()
						.collect(Collectors.groupingBy(
								AttendeesResponse::getCourseName,
								LinkedHashMap::new,
								Collectors.collectingAndThen(
										Collectors.toList(),
										list -> SortedAttendanceRecordsResponse.GroupedAttendees.builder()
												.groupName(list.isEmpty() ? "Unknown" : list.get(0).getCourseName())
												.clusterName(list.isEmpty() ? "Unknown" : list.get(0).getClusterName())
												.courseName(list.isEmpty() ? "Unknown" : list.get(0).getCourseName())
												.count(list.size())
												.attendees(list)
												.build()
								)
						));
				break;

			case SECTION:
				groupedMap = attendees.stream()
						.collect(Collectors.groupingBy(
								AttendeesResponse::getSectionName,
								LinkedHashMap::new,
								Collectors.collectingAndThen(
										Collectors.toList(),
										list -> SortedAttendanceRecordsResponse.GroupedAttendees.builder()
												.groupName(list.isEmpty() ? "Unknown" : list.get(0).getSectionName())
												.clusterName(list.isEmpty() ? "Unknown" : list.get(0).getClusterName())
												.courseName(list.isEmpty() ? "Unknown" : list.get(0).getCourseName())
												.sectionName(list.isEmpty() ? "Unknown" : list.get(0).getSectionName())
												.yearLevel(list.isEmpty() ? null : list.get(0).getYearLevel())
												.count(list.size())
												.attendees(list)
												.build()
								)
						));
				break;

			case YEAR_LEVEL:
				groupedMap = attendees.stream()
						.collect(Collectors.groupingBy(
								a -> a.getYearLevel() != null ? a.getYearLevel().toString() : "Unknown",
								LinkedHashMap::new,
								Collectors.collectingAndThen(
										Collectors.toList(),
										list -> {
											Integer yearLevel = list.isEmpty() || list.get(0).getYearLevel() == null
													? null : list.get(0).getYearLevel();
											String yearLevelName = yearLevel != null
													? getYearLevelName(yearLevel)
													: "Unknown Year Level";

											return SortedAttendanceRecordsResponse.GroupedAttendees.builder()
													.groupName(yearLevelName)
													.yearLevel(yearLevel)
													.count(list.size())
													.attendees(list)
													.build();
										}
								)
						));
				break;

			default:
				throw new IllegalArgumentException("Invalid sort criteria: " + sortCriteria);
		}
		log.info("Grouped {} attendees into {} groups by {}", attendees.size(), groupedMap.size(), sortCriteria);
		return SortedAttendanceRecordsResponse.builder()
				.eventId(eventId)
				.eventName(event.getEventName())
				.sortBy(sortCriteria.name())
				.totalAttendees(attendees.size())
				.groupedAttendees(groupedMap)
				.build();
	}

	@Override
	public List<AttendanceRecords> getAttendanceRecordsByStudentId(String studentId) {
		return attendanceRecordsRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
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

	private String getYearLevelName(Integer yearLevel) {
		if (yearLevel == null) {
			return "Unknown Year Level";
		}
		String suffix = switch (yearLevel % 10) {
			case 1 -> (yearLevel == 11) ? "th" : "st";
			case 2 -> (yearLevel == 12) ? "th" : "nd";
			case 3 -> (yearLevel == 13) ? "th" : "rd";
			default -> "th";
		};
		return yearLevel + suffix + " Year";
	}

	private FinalizedAttendanceRecordsResponse mapEventToFinalizedResponse(Event event) {
		List<AttendanceRecords> records = attendanceRecordsRepository.findByEventEventId(event.getEventId());
		long totalPresent = records.stream().filter(r -> r.getAttendanceStatus() == AttendanceStatus.PRESENT).count();
		long totalAbsent = records.stream().filter(r -> r.getAttendanceStatus() == AttendanceStatus.ABSENT).count();
		long totalIdle = records.stream().filter(r -> r.getAttendanceStatus() == AttendanceStatus.IDLE).count();
		long totalLate = records.stream().filter(r -> r.getAttendanceStatus() == AttendanceStatus.LATE).count();

		String regLocationName = event.getRegistrationLocation() != null
				? event.getRegistrationLocation().getLocationName()
				: "No Registration Location";

		String venueLocationName = event.getVenueLocation() != null
				? event.getVenueLocation().getLocationName()
				: "No Venue Location";

		return FinalizedAttendanceRecordsResponse.builder()
				.eventId(event.getEventId())
				.eventName(event.getEventName())
				.registrationLocationName(regLocationName)
				.venueLocationName(venueLocationName)
				.registrationDateTime(event.getRegistrationDateTime())
				.startingDateTime(event.getStartingDateTime())
				.endingDateTime(event.getEndingDateTime())
				.eventStatus(event.getEventStatus())
				.totalPresent((int) totalPresent)
				.totalAbsent((int) totalAbsent)
				.totalIdle((int) totalIdle)
				.totalLate((int) totalLate)
				.build();
	}
}
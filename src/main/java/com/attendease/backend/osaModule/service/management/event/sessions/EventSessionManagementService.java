package com.attendease.backend.osaModule.service.management.event.sessions;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EligibleAttendees.EligibilityCriteria;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.domain.events.Session.Management.Request.EventSessionRequest;
import com.attendease.backend.domain.events.Session.Management.Response.EventCreationResponse;
import com.attendease.backend.domain.locations.EventLocations;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import com.attendease.backend.repository.locations.LocationRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.attendease.backend.repository.sections.SectionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * {@link EventSessionManagementService} is a service used for managing event sessions, including creation, retrieval,
 * updates, and deletion of events.
 *
 * <p>Provides methods to handle event lifecycle operations such as creating upcoming events, updating details,
 * canceling events, and querying by status or date ranges. Ensures date validations and location references are properly handled.</p>
 *
 * <p>Authored: jakematthewviado204@gmail.com</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSessionManagementService {

    private final LocationRepository locationRepository;
    private final SectionsRepository sectionsRepository;
    private final CourseRepository courseRepository;
    private final ClustersRepository clustersRepository;
    private final EventSessionsRepository eventSessionRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    /**
     * Creates a new event session with the provided details.
     * Validates the date range and sets the initial status to {@link EventStatus#UPCOMING}.
     * Associates the event with a location if provided.
     *
     * @param request the {@link EventSessions} object containing event details
     * @return the saved {@link EventSessions} with generated ID and timestamps
     * @throws IllegalArgumentException if date validations fail or location ID is invalid
     */
    public EventCreationResponse createEvent(EventSessionRequest request) {
        EligibilityCriteria domainCriteria;
        EligibilityCriteria reqCriteria = request.getEligibleStudents();
        if (reqCriteria == null) {
            domainCriteria = EligibilityCriteria.builder().allStudents(true).build();
        } else {
            validateEligibilityCriteria(reqCriteria);
            domainCriteria = populateDomainEligibilityCriteria(reqCriteria);
        }

        EventSessions eventSession = EventSessions.builder()
                .eventName(request.getEventName())
                .description(request.getDescription())
                .timeInRegistrationStartDateTime(request.getTimeInRegistrationStartDateTime())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .eligibleStudents(domainCriteria)
                .build();

        eventSession.setEventLocationId(request.getEventLocationId());
        if (eventSession.getEventLocationId() == null) {
            throw new IllegalArgumentException("Event location is required when creating event");
        }
        validateDateRange(eventSession.getTimeInRegistrationStartDateTime(), eventSession.getStartDateTime(), eventSession.getEndDateTime());
        eventSession.setEventStatus(EventStatus.UPCOMING);
        eventSession.setCreatedAt(LocalDateTime.now());
        eventSession.setUpdatedAt(LocalDateTime.now());

        if (eventSession.getEventLocationId() != null) {
            EventLocations location = locationRepository.findById(eventSession.getEventLocationId()).orElseThrow(() -> new IllegalArgumentException("Location ID does not exist"));
            eventSession.setEventLocation(location);
        }
        checkLocationConflict(eventSession, null);
        EventSessions savedEvent = eventSessionRepository.save(eventSession);
        log.info("Successfully created event session with ID: {}", savedEvent.getEventId());
        return mapToEventCreationResponse(savedEvent);
    }

    /**
     * Retrieves an event session by its unique identifier.
     *
     * @param id the unique ID of the event session
     * @return the {@link EventSessions} matching the ID
     * @throws RuntimeException if no event is found with the provided ID
     */
    public EventSessions getEventById(String id) {
        log.info("Retrieving event session with ID: {}", id);
        return eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
    }

    /**
     * Retrieves all event sessions (ordered by newest first).
     *
     * @return a list of all {@link EventSessions}
     */
    public List<EventSessions> getAllEvents() {
        return eventSessionRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieves event sessions filtered by a specific status.
     *
     * @param status the {@link EventStatus} to filter by
     * @return a list of {@link EventSessions} with the matching status
     */
    public List<EventSessions> getEventsByStatus(EventStatus status) {
        return eventSessionRepository.findByEventStatus(status);
    }

    /**
     * Retrieves event sessions within a specified date range.
     *
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return a list of {@link EventSessions} within the date range
     */
    public List<EventSessions> getEventsByDateRange(Date from, Date to) {
        return eventSessionRepository.findByDateRange(from, to);
    }

    /**
     * Retrieves event sessions filtered by status and within a specified date range.
     *
     * @param status the {@link EventStatus} to filter by
     * @param from the start date (inclusive)
     * @param to the end date (inclusive)
     * @return a list of {@link EventSessions} matching the status and date range
     */
    public List<EventSessions> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) {
        return eventSessionRepository.findByStatusAndDateRange(status, from, to);
    }

    /**
     * Deletes an event session by its unique identifier.
     * Performs data integrity checks: allows deletion for UPCOMING or CANCELLED events unconditionally.
     * For REGISTRATION, ONGOING, CONCLUDED, or FINALIZED events, prevents deletion if attendance records exist (>= 0),
     * throwing a status-specific exception message.
     *
     * @param id the unique ID of the event session to delete
     * @throws RuntimeException if no event is found with the provided ID or if deletion is prevented due to data integrity constraints
     */
    public void deleteEventById(String id) {
        EventSessions event = eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));

        EventStatus status = event.getEventStatus();
        if (status == EventStatus.UPCOMING || status == EventStatus.CANCELLED) {
            eventSessionRepository.deleteById(id);
            log.info("Deleted event with ID: {}", id);
            return;
        }

        long attendanceCount = attendanceRecordsRepository.countByEventEventId(id);
        if (attendanceCount >= 0) {
            String eventName = event.getEventName();
            String message = switch (status) {
                case REGISTRATION -> "You cannot delete event '" + eventName + "' because this event is about to start and there might be already registered students: (" + attendanceCount + "). This action is prevented because it may affect ongoing registrations. If you wish to adjust event details, consider cancelling or edit the event instead";
                case ONGOING -> "You cannot delete ongoing event '" + eventName + "' due to active attendance tracking (" + attendanceCount + " records). The event is currently in progress.";
                case CONCLUDED -> "You cannot delete concluded event '" + eventName + "' with attendance records (" + attendanceCount + "). This action is prevented because deleting would removed pre-attendance records data.";
                case FINALIZED -> "You cannot delete finalized event '" + eventName + "' with attendance records (" + attendanceCount + "). This action is prevented because deleting would removed finalized records for all students.";
                default -> "Cannot delete event '" + eventName + "' with status " + status + " due to existing attendance records (" + attendanceCount + "). This protects data integrity.";
            };
            throw new RuntimeException(message);
        }

        eventSessionRepository.deleteById(id);
        log.info("Deleted event with ID: {}", id);
    }

    /**
     * Updates an existing event session with new details.
     * Validates location if provided and updates timestamps.
     * Prevents updates for events that are CONCLUDED or FINALIZED to maintain attendance records integrity.
     * Event status cannot be manually updated here; use dedicated methods like cancelEvent or rely on the scheduler for time-based changes.
     *
     * @param eventId the unique ID of the event to update
     * @param updateEvent the {@link EventSessions} object containing updated fields
     * @return the updated {@link EventSessions}
     * @throws RuntimeException if no event is found with the provided ID or if update is prevented due to event status constraints
     * @throws IllegalArgumentException if the location ID is invalid or if attempting to update event status
     */
    public EventSessions updateEvent(String eventId, EventSessions updateEvent) {
        EventSessions existingEvent = eventSessionRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        EventStatus currentStatus = existingEvent.getEventStatus();
        if (currentStatus == EventStatus.CONCLUDED || currentStatus == EventStatus.FINALIZED) {
            final String message = updatingConcludedAndFinalizedEventMessage(eventId, existingEvent, currentStatus);
            throw new RuntimeException(message);
        }

        if (updateEvent.getEventStatus() != null) {
            throw new IllegalArgumentException("Event status cannot be updated via this method. Use cancelEvent for cancellation or rely on the scheduler for time-based status changes.");
        }

        if (updateEvent.getEventName() != null) {
            existingEvent.setEventName(updateEvent.getEventName());
        }
        if (updateEvent.getDescription() != null) {
            existingEvent.setDescription(updateEvent.getDescription());
        }

        boolean datesUpdated = false;
        LocalDateTime newTimeIn = updateEvent.getTimeInRegistrationStartDateTime();
        LocalDateTime newStart = updateEvent.getStartDateTime();
        LocalDateTime newEnd = updateEvent.getEndDateTime();

        if (newTimeIn != null || newStart != null || newEnd != null) {
            datesUpdated = true;
            LocalDateTime timeIn = newTimeIn != null ? newTimeIn : existingEvent.getTimeInRegistrationStartDateTime();
            LocalDateTime start = newStart != null ? newStart : existingEvent.getStartDateTime();
            LocalDateTime end = newEnd != null ? newEnd : existingEvent.getEndDateTime();
            validateDateRange(timeIn, start, end);
            existingEvent.setTimeInRegistrationStartDateTime(timeIn);
            existingEvent.setStartDateTime(start);
            existingEvent.setEndDateTime(end);
        }

        if (datesUpdated) {
            setStatusBasedOnCurrentTime(existingEvent);
        }

        if (updateEvent.getEligibleStudents() != null) {
            validateEligibilityCriteria(updateEvent.getEligibleStudents());
            EligibilityCriteria expandedCriteria = populateDomainEligibilityCriteria(updateEvent.getEligibleStudents());
            existingEvent.setEligibleStudents(expandedCriteria);
        }

        if (updateEvent.getEventLocationId() != null) {
            EventLocations location = locationRepository.findById(updateEvent.getEventLocationId()).orElseThrow(() -> new IllegalArgumentException("Location ID does not exist: " + updateEvent.getEventLocationId()));
            existingEvent.setEventLocation(location);
            existingEvent.setEventLocationId(updateEvent.getEventLocationId());
        } else if (existingEvent.getEventLocationId() == null) {
            throw new IllegalArgumentException("Event location ID cannot be removed or left unset");
        }

        existingEvent.setUpdatedAt(LocalDateTime.now());
        checkLocationConflict(existingEvent, eventId);
        EventSessions updatedEvent = eventSessionRepository.save(existingEvent);
        log.info("Successfully updated event session with ID: {}", eventId);
        return updatedEvent;
    }

    /**
     * Cancels an event session by setting its status to {@link EventStatus#CANCELLED}.
     *
     * @param id the unique ID of the event to cancel
     * @return the updated {@link EventSessions} with cancelled status
     * @throws RuntimeException if no event is found with the provided ID
     */
    public EventSessions cancelEvent(String id) {
        EventSessions existingEvent = eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));

        existingEvent.setEventStatus(EventStatus.CANCELLED);
        existingEvent.setUpdatedAt(LocalDateTime.now());
        return eventSessionRepository.save(existingEvent);
    }

    /**
     * PRIVATE HELPERS
     */

    private void checkLocationConflict(EventSessions eventSession, String excludeEventId) {
        if (eventSession.getEventLocationId() == null) {
            return;
        }
        List<EventSessions> allEvents = eventSessionRepository.findAll();
        List<EventSessions> conflicts = allEvents.stream()
                .filter(e -> !e.getEventId().equals(excludeEventId))
                .filter(e -> e.getEventStatus() != EventStatus.CANCELLED && e.getEventStatus() != EventStatus.CONCLUDED && e.getEventStatus() != EventStatus.FINALIZED)
                .filter(e -> e.getEventLocationId() != null)
                .filter(e -> e.getEventLocationId().equals(eventSession.getEventLocationId()))
                .filter(e -> hasTimeOverlap(eventSession.getStartDateTime(), eventSession.getEndDateTime(), e.getStartDateTime(), e.getEndDateTime()))
                .toList();
        if (!conflicts.isEmpty()) {
            String conflictNames = conflicts.stream()
                    .map(EventSessions::getEventName)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("The selected location is already in use during the specified time by the following event(s): " + conflictNames);
        }
    }

    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void setStatusBasedOnCurrentTime(EventSessions event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime registrationStart = event.getTimeInRegistrationStartDateTime();
        LocalDateTime start = event.getStartDateTime();
        LocalDateTime end = event.getEndDateTime();

        if (registrationStart == null || start == null || end == null) {
            return;
        }

        EventStatus currentStatus = event.getEventStatus();
        if (currentStatus == EventStatus.CANCELLED || currentStatus == EventStatus.FINALIZED) {
            return;
        }

        EventStatus newStatus;
        if (now.isBefore(registrationStart)) {
            newStatus = EventStatus.UPCOMING;
        } else if (now.isBefore(start)) {
            newStatus = EventStatus.REGISTRATION;
        } else if (now.isBefore(end)) {
            newStatus = EventStatus.ONGOING;
        } else {
            newStatus = EventStatus.CONCLUDED;
        }

        if (currentStatus != newStatus) {
            event.setEventStatus(newStatus);
            log.info("Event {} status updated from {} to {} due to date changes", event.getEventId(), currentStatus, newStatus);
        }
    }

    private String updatingConcludedAndFinalizedEventMessage(String eventId, EventSessions existingEvent, EventStatus currentStatus) {
        String eventName = existingEvent.getEventName();
        return switch (currentStatus) {
            case CONCLUDED -> "You cannot update the event details with CONCLUDED status '" + eventName + "'. Once an event is concluded, changes could alter all the pre-attendance records data.";
            case FINALIZED -> "You cannot update the event details with FINALIZED status '" + eventName + "'. Finalized events are locked for auditing and reporting purposes to ensure historical data integrity of student's attendance records.";
            default -> "You cannot update the event '" + eventName + "' (ID: " + eventId + ") with status " + currentStatus + ". This status prevents modifications to protect attendance records.";
        };
    }

    private void validateDateRange(LocalDateTime timeInDateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (timeInDateTime == null || startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Please provide all date fields: time-in registration, start time, and end time.");
        }

        if (timeInDateTime.isBefore(now)) {
            throw new IllegalArgumentException("The time-in registration date and time must be in the future.");
        }
        if (timeInDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException("The time-in registration must start before the event begins.");
        }
        if (startDateTime.isBefore(now)) {
            throw new IllegalArgumentException("The event start date and time must be in the future.");
        }
        if (endDateTime.isBefore(now)) {
            throw new IllegalArgumentException("The event end date and time must be in the future.");
        }

        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("The event start time must be before the end time.");
        }

        long durationInMinutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (durationInMinutes < 30) {
            throw new IllegalArgumentException("The event must last at least 30 minutes.");
        }
        if (durationInMinutes > 360) {
            throw new IllegalArgumentException("The event cannot exceed 6 hours in duration.");
        }
    }

    private EligibilityCriteria populateDomainEligibilityCriteria(EligibilityCriteria reqCriteria) {
        if (reqCriteria.isAllStudents()) {
            return EligibilityCriteria.builder().allStudents(true).build();
        }

        Set<String> clusterIds = new HashSet<>();
        Set<String> courseIds = new HashSet<>();
        Set<String> sectionIds = new HashSet<>();

        if (reqCriteria.getCluster() != null && !reqCriteria.getCluster().isEmpty()) {
            clusterIds.addAll(reqCriteria.getCluster());
            for (String clusterId : reqCriteria.getCluster()) {
                List<Courses> coursesUnderCluster = courseRepository.findByClusterClusterId(clusterId);
                for (Courses course : coursesUnderCluster) {
                    courseIds.add(course.getId());
                    List<Sections> sectionsUnderCourse = sectionsRepository.findByCourseId(course.getId());
                    sectionIds.addAll(sectionsUnderCourse.stream().map(Sections::getId).collect(Collectors.toSet()));
                }
            }
        }

        if (reqCriteria.getCourse() != null && !reqCriteria.getCourse().isEmpty()) {
            for (String courseId : reqCriteria.getCourse()) {
                courseIds.add(courseId);
                Courses course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Course ID not found: " + courseId));
                if (course.getCluster() != null && course.getCluster().getClusterId() != null) {
                    clusterIds.add(course.getCluster().getClusterId());
                }
                List<Sections> sectionsUnderCourse = sectionsRepository.findByCourseId(courseId);
                sectionIds.addAll(sectionsUnderCourse.stream().map(Sections::getId).collect(Collectors.toSet()));
            }
        }

        if (reqCriteria.getSections() != null && !reqCriteria.getSections().isEmpty()) {
            for (String sectionId : reqCriteria.getSections()) {
                sectionIds.add(sectionId);
                Sections section = sectionsRepository.findById(sectionId)
                        .orElseThrow(() -> new IllegalArgumentException("Section ID not found: " + sectionId));
                if (section.getCourse() != null && section.getCourse().getId() != null) {
                    courseIds.add(section.getCourse().getId());
                    Courses course = section.getCourse();
                    if (course.getCluster() != null && course.getCluster().getClusterId() != null) {
                        clusterIds.add(course.getCluster().getClusterId());
                    }
                }
            }
        }

        List<String> clusterNames = clusterIds.isEmpty() ? null : clustersRepository.findAllById(new ArrayList<>(clusterIds)).stream().map(Clusters::getClusterName).sorted().collect(Collectors.toList());
        List<String> courseNames = courseIds.isEmpty() ? null : courseRepository.findAllById(new ArrayList<>(courseIds)).stream().map(Courses::getCourseName).sorted().collect(Collectors.toList());
        List<String> sectionNames = sectionIds.isEmpty() ? null : sectionsRepository.findAllById(new ArrayList<>(sectionIds)).stream().map(Sections::getSectionName).sorted().collect(Collectors.toList());

        return EligibilityCriteria.builder()
                .allStudents(false)
                .cluster(new ArrayList<>(clusterIds))
                .clusterNames(clusterNames)
                .course(new ArrayList<>(courseIds))
                .courseNames(courseNames)
                .sections(new ArrayList<>(sectionIds))
                .sectionNames(sectionNames)
                .build();
    }

    private void validateEligibilityCriteria(EligibilityCriteria criteria) {
        if (criteria.isAllStudents()) {
            return;
        }
        List<String> clusterIds = criteria.getCluster();
        List<String> courseIds = criteria.getCourse();
        List<String> sectionIds = criteria.getSections();
        if ((clusterIds == null || clusterIds.isEmpty()) && (courseIds == null || courseIds.isEmpty()) && (sectionIds == null || sectionIds.isEmpty())) {
            throw new IllegalArgumentException("At least one cluster, course, or section ID must be provided when not targeting all students.");
        }
        if ((clusterIds != null && clusterIds.stream().anyMatch(String::isBlank)) || (courseIds != null && courseIds.stream().anyMatch(String::isBlank)) || (sectionIds != null && sectionIds.stream().anyMatch(String::isBlank))) {
            throw new IllegalArgumentException("IDs cannot be blank.");
        }
    }

    private EventCreationResponse mapToEventCreationResponse(EventSessions event) {
        EligibilityCriteria criteria = event.getEligibleStudents();
        EventCreationResponse response = EventCreationResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .eventLocationId(event.getEventLocationId())
                .timeInRegistrationStartDateTime(event.getTimeInRegistrationStartDateTime())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .eventStatus(event.getEventStatus())
                .allStudents(criteria == null || criteria.isAllStudents())
                .build();

        if (criteria != null && !criteria.isAllStudents()) {
            response.setClusterIDs(criteria.getCluster());
            response.setClusterNames(criteria.getClusterNames());
            response.setCourseIDs(criteria.getCourse());
            response.setCourseNames(criteria.getCourseNames());
            response.setSectionIDs(criteria.getSections());
            response.setSectionNames(criteria.getSectionNames());
        }
        return response;
    }
}

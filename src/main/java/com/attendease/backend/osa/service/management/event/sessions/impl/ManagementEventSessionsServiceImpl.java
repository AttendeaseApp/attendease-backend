package com.attendease.backend.osa.service.management.event.sessions.impl;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.management.EventManagementRequest;
import com.attendease.backend.domain.event.management.EventManagementResponse;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osa.service.management.event.sessions.ManagementEventSessionsService;
import com.attendease.backend.repository.attendanceRecords.AttendanceRecordsRepository;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.location.LocationRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.attendease.backend.repository.sections.SectionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManagementEventSessionsServiceImpl implements ManagementEventSessionsService {

    private final LocationRepository locationRepository;
    private final SectionsRepository sectionsRepository;
    private final CourseRepository courseRepository;
    private final ClustersRepository clustersRepository;
    private final EventRepository eventSessionRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    @Override
    public EventManagementResponse createEvent(EventManagementRequest request) {
        EventEligibility domainCriteria;
        EventEligibility reqCriteria = request.getEligibleStudents();
        if (reqCriteria == null) {
            domainCriteria = EventEligibility.builder().allStudents(true).build();
        } else {
            validateEligibilityCriteria(reqCriteria);
            domainCriteria = populateDomainEligibilityCriteria(reqCriteria);
        }

        Event eventSession = Event.builder()
                .eventName(request.getEventName())
                .description(request.getDescription())
                .timeInRegistrationStartDateTime(request.getTimeInRegistrationStartDateTime())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .eligibleStudents(domainCriteria)
                .facialVerificationEnabled(request.getFacialVerificationEnabled() != null ? request.getFacialVerificationEnabled() : true)
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
            Location location = locationRepository.findById(eventSession.getEventLocationId()).orElseThrow(() -> new IllegalArgumentException("Location ID does not exist"));
            eventSession.setEventLocation(location);
        }
        checkLocationConflict(eventSession, null);
        Event savedEvent = eventSessionRepository.save(eventSession);
        log.info("Successfully created event session with ID: {}", savedEvent.getEventId());
        return mapToEventCreationResponse(savedEvent);
    }

    @Override
    public Event getEventById(String id) {
        log.info("Retrieving event session with ID: {}", id);
        return eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventSessionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<Event> getEventsByStatus(EventStatus status) {
        return eventSessionRepository.findByEventStatus(status);
    }

    @Override
    public List<Event> getEventsByDateRange(Date from, Date to) {
        return eventSessionRepository.findByDateRange(from, to);
    }

    @Override
    public List<Event> getEventsByStatusAndDateRange(EventStatus status, Date from, Date to) {
        return eventSessionRepository.findByStatusAndDateRange(status, from, to);
    }

    @Override
    public void deleteEventById(String id) {
        Event event = eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));

        EventStatus status = event.getEventStatus();
        if (status == EventStatus.UPCOMING || status == EventStatus.CANCELLED) {
            eventSessionRepository.deleteById(id);
            log.info("Deleted event with ID: {}", id);
            return;
        }

        long attendanceCount = attendanceRecordsRepository.countByEventEventId(id);
        if (attendanceCount > 0) {
            String eventName = event.getEventName();
            String message = switch (status) {
                case REGISTRATION -> "You cannot delete event '" + eventName + "' because this event is about to start and there might be already registered student: (" + attendanceCount + "). This action is prevented because it may affect ongoing registrations. If you wish to adjust event details, consider cancelling or edit the event instead";
                case ONGOING -> "You cannot delete ongoing event '" + eventName + "' due to active attendance verification (" + attendanceCount + " records). The event is currently in progress.";
                case CONCLUDED -> "You cannot delete concluded event '" + eventName + "' with attendance records (" + attendanceCount + "). This action is prevented because deleting would removed pre-attendance records data.";
                case FINALIZED -> "You cannot delete finalized event '" + eventName + "' with attendance records (" + attendanceCount + "). This action is prevented because deleting would removed finalized records for all student.";
                default -> "Cannot delete event '" + eventName + "' with status " + status + " due to existing attendance records (" + attendanceCount + "). This protects data integrity.";
            };
            throw new RuntimeException(message);
        }

        eventSessionRepository.deleteById(id);
        log.info("Deleted event with ID: {}", id);
    }

    @Override
    public Event updateEvent(String eventId, Event updateEvent) {
        Event existingEvent = eventSessionRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

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

        if (updateEvent.getFacialVerificationEnabled() != null) {
            existingEvent.setFacialVerificationEnabled(updateEvent.getFacialVerificationEnabled());
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
            EventEligibility expandedCriteria = populateDomainEligibilityCriteria(updateEvent.getEligibleStudents());
            existingEvent.setEligibleStudents(expandedCriteria);
        }

        if (updateEvent.getEventLocationId() != null) {
            Location location = locationRepository.findById(updateEvent.getEventLocationId()).orElseThrow(() -> new IllegalArgumentException("Location ID does not exist: " + updateEvent.getEventLocationId()));
            existingEvent.setEventLocation(location);
            existingEvent.setEventLocationId(updateEvent.getEventLocationId());
        } else if (existingEvent.getEventLocationId() == null) {
            throw new IllegalArgumentException("Event location ID cannot be removed or left unset");
        }

        existingEvent.setUpdatedAt(LocalDateTime.now());
        checkLocationConflict(existingEvent, eventId);
        Event updatedEvent = eventSessionRepository.save(existingEvent);
        log.info("Successfully updated event session with ID: {}", eventId);
        return updatedEvent;
    }

    @Override
    public Event cancelEvent(String id) {
        Event existingEvent = eventSessionRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));

        existingEvent.setEventStatus(EventStatus.CANCELLED);
        existingEvent.setUpdatedAt(LocalDateTime.now());
        return eventSessionRepository.save(existingEvent);
    }

    /**
     * PRIVATE HELPERS
     */

    private void checkLocationConflict(Event eventSession, String excludeEventId) {
        if (eventSession.getEventLocationId() == null) {
            return;
        }
        List<Event> allEvents = eventSessionRepository.findAll();
        List<Event> conflicts = allEvents.stream()
                .filter(e -> !e.getEventId().equals(excludeEventId))
                .filter(e -> e.getEventStatus() != EventStatus.CANCELLED && e.getEventStatus() != EventStatus.CONCLUDED && e.getEventStatus() != EventStatus.FINALIZED)
                .filter(e -> e.getEventLocationId() != null)
                .filter(e -> e.getEventLocationId().equals(eventSession.getEventLocationId()))
                .filter(e -> hasTimeOverlap(eventSession.getStartDateTime(), eventSession.getEndDateTime(), e.getStartDateTime(), e.getEndDateTime()))
                .toList();
        if (!conflicts.isEmpty()) {
            String conflictNames = conflicts.stream()
                    .map(Event::getEventName)
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("The selected location is already in use during the specified time by the following event(s): " + conflictNames);
        }
    }

    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void setStatusBasedOnCurrentTime(Event event) {
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

    private String updatingConcludedAndFinalizedEventMessage(String eventId, Event existingEvent, EventStatus currentStatus) {
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

    private EventEligibility populateDomainEligibilityCriteria(EventEligibility reqCriteria) {
        if (reqCriteria.isAllStudents()) {
            return EventEligibility.builder().allStudents(true).build();
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

        return EventEligibility.builder()
                .allStudents(false)
                .cluster(new ArrayList<>(clusterIds))
                .clusterNames(clusterNames)
                .course(new ArrayList<>(courseIds))
                .courseNames(courseNames)
                .sections(new ArrayList<>(sectionIds))
                .sectionNames(sectionNames)
                .build();
    }

    private void validateEligibilityCriteria(EventEligibility criteria) {
        if (criteria.isAllStudents()) {
            return;
        }
        List<String> clusterIds = criteria.getCluster();
        List<String> courseIds = criteria.getCourse();
        List<String> sectionIds = criteria.getSections();
        if ((clusterIds == null || clusterIds.isEmpty()) && (courseIds == null || courseIds.isEmpty()) && (sectionIds == null || sectionIds.isEmpty())) {
            throw new IllegalArgumentException("At least one cluster, course, or section ID must be provided when not targeting all student.");
        }
        if ((clusterIds != null && clusterIds.stream().anyMatch(String::isBlank)) || (courseIds != null && courseIds.stream().anyMatch(String::isBlank)) || (sectionIds != null && sectionIds.stream().anyMatch(String::isBlank))) {
            throw new IllegalArgumentException("IDs cannot be blank.");
        }
    }

    private EventManagementResponse mapToEventCreationResponse(Event event) {
        EventEligibility criteria = event.getEligibleStudents();
        EventManagementResponse response = EventManagementResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .eventLocationId(event.getEventLocationId())
                .timeInRegistrationStartDateTime(event.getTimeInRegistrationStartDateTime())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .eventStatus(event.getEventStatus())
                .allStudents(criteria == null || criteria.isAllStudents())
                .facialVerificationEnabled(event.getFacialVerificationEnabled())
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
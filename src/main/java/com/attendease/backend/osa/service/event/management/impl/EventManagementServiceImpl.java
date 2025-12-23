package com.attendease.backend.osa.service.event.management.impl;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.enums.location.LocationPurpose;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.event.Event;
import com.attendease.backend.domain.event.management.EventManagementRequest;
import com.attendease.backend.domain.event.management.EventManagementResponse;
import com.attendease.backend.domain.location.Location;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.exceptions.domain.Event.*;
import com.attendease.backend.exceptions.domain.Location.InvalidLocationPurposeException;
import com.attendease.backend.exceptions.domain.Location.LocationNotFoundException;
import com.attendease.backend.osa.service.event.management.EventManagementService;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of event management service.
 *
 * <p>This class is marked as {@code final} to prevent inheritance and ensure
 * the implementation remains as designed. Service behavior should be modified
 * through configuration or by implementing a different service class, not by
 * extending this one.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-16 refactored in 2025-Dec-23, happy holidays!
 */
@Service
@Slf4j
@RequiredArgsConstructor
public final class EventManagementServiceImpl implements EventManagementService {

    private final LocationRepository locationRepository;
    private final SectionsRepository sectionsRepository;
    private final CourseRepository courseRepository;
    private final ClustersRepository clustersRepository;
    private final EventRepository eventRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;

    private static final long MIN_EVENT_DURATION_MINUTES = 30;
    private static final long MAX_EVENT_DURATION_MINUTES = 360;
    private static final List<EventStatus> EXCLUDED_CONFLICT_STATUSES = List.of(
            EventStatus.CANCELLED,
            EventStatus.CONCLUDED,
            EventStatus.FINALIZED
    );

    @Override
    @Transactional
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
                .registrationLocationId(request.getRegistrationLocationId())
                .venueLocationId(request.getVenueLocationId())
                .registrationDateTime(request.getRegistrationDateTime())
                .startingDateTime(request.getStartingDateTime())
                .endingDateTime(request.getEndingDateTime())
                .eligibleStudents(domainCriteria)
                .facialVerificationEnabled(Boolean.FALSE.equals(request.getFacialVerificationEnabled()))
                .attendanceLocationMonitoringEnabled(Boolean.FALSE.equals(request.getAttendanceLocationMonitoringEnabled()))
                .build();

        String regLocationId = request.getRegistrationLocationId();
        String venueLocationId = request.getVenueLocationId();

        if (regLocationId == null || venueLocationId == null) {
            throw new IllegalArgumentException("Both registration and venue locations are required");
        }

        eventSession.setRegistrationLocationId(regLocationId);
        eventSession.setVenueLocationId(venueLocationId);

        Location regLocation = locationRepository.findById(regLocationId)
                .orElseThrow(() -> new LocationNotFoundException("The selected location for registration does not exist: " + regLocationId));

        eventSession.setRegistrationLocation(regLocation);
        eventSession.setRegistrationLocationName(regLocation.getLocationName());

        Location venueLocation = locationRepository.findById(venueLocationId)
                .orElseThrow(() -> new LocationNotFoundException("The selected location for venue does not exist: " + venueLocationId));

        validateLocationPurposes(regLocation, venueLocation);

        eventSession.setVenueLocation(venueLocation);
        eventSession.setVenueLocationName(venueLocation.getLocationName());

        validateDateRange(eventSession.getRegistrationDateTime(), eventSession.getStartingDateTime(), eventSession.getEndingDateTime());
        eventSession.setEventStatus(EventStatus.UPCOMING);
        eventSession.setCreated(LocalDateTime.now());
        eventSession.setLastModified(LocalDateTime.now());

        checkLocationConflict(eventSession, null);
        Event savedEvent = eventRepository.save(eventSession);
        log.info("Successfully created event session with ID: {} (Reg: {}, Venue: {})", savedEvent.getEventId(), regLocationId, venueLocationId);
        return toEventCreationResponse(savedEvent, regLocationId, venueLocationId);
    }

    @Override
    public Event getEventById(String id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id, true));
    }

    @Override
    public List<EventManagementResponse> getAllEvents() {
        List<Event> events = eventRepository.findAllByOrderByCreatedDesc();
        return events.stream().map(this::toEventManagementResponse).collect(Collectors.toList());
    }

    @Override
    public List<EventManagementResponse> getEventsByStatus(EventStatus status) {
        List<Event> events = eventRepository.findByEventStatus(status);
        return events.stream().map(this::toEventManagementResponse).collect(Collectors.toList());
    }

    // TODO: Allow deletion of FINALIZED EVENT when academic year ends
    @Override
    @Transactional
    public void deleteEventById(String id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id, true));

        EventStatus status = event.getEventStatus();
        if (status == EventStatus.UPCOMING || status == EventStatus.CANCELLED) {
            eventRepository.deleteById(id);
            log.debug("Deleted event with ID: {}", id);
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
            throw new EventDeletionNotAllowedException(message);
        }

        eventRepository.deleteById(id);
        log.debug("Deleted event with ID: {}", id);
    }

    // TODO: Allow updating of FINALIZED EVENT when academic year ends
    @Override
    @Transactional
    public EventManagementResponse updateEvent(String eventId, EventManagementRequest updateEvent) {
        Event existingEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId, true));

        EventStatus currentStatus = existingEvent.getEventStatus();
        if (currentStatus == EventStatus.CONCLUDED || currentStatus == EventStatus.FINALIZED) {
            final String message = updatingConcludedAndFinalizedEventMessage(eventId, existingEvent, currentStatus);
            throw new EventUpdateNotAllowedException(message);
        }

        if (updateEvent.getEventStatus() != null) {
            throw new EventStatusException("Event status cannot be updated via this method. Use cancelEvent for cancellation or rely on the scheduler for time-based status changes.");
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
        if (updateEvent.getAttendanceLocationMonitoringEnabled() != null) {
            existingEvent.setAttendanceLocationMonitoringEnabled(updateEvent.getAttendanceLocationMonitoringEnabled());
        }

        boolean datesUpdated = false;
        LocalDateTime newTimeInReg = updateEvent.getRegistrationDateTime();
        LocalDateTime newStart = updateEvent.getStartingDateTime();
        LocalDateTime newEnd = updateEvent.getEndingDateTime();

        if (newTimeInReg != null || newStart != null || newEnd != null) {
            datesUpdated = true;
            LocalDateTime timeIn = newTimeInReg != null ? newTimeInReg : existingEvent.getRegistrationDateTime();
            LocalDateTime start = newStart != null ? newStart : existingEvent.getStartingDateTime();
            LocalDateTime end = newEnd != null ? newEnd : existingEvent.getEndingDateTime();

            validateDateRange(timeIn, start, end);

            existingEvent.setRegistrationDateTime(timeIn);
            existingEvent.setStartingDateTime(start);
            existingEvent.setEndingDateTime(end);
        }

        String newRegLocationId = updateEvent.getRegistrationLocationId();
        String newVenueLocationId = updateEvent.getVenueLocationId();
        if (newRegLocationId != null || newVenueLocationId != null) {
            if (newRegLocationId != null) {
                Location regLocation = locationRepository.findById(newRegLocationId)
                        .orElseThrow(() -> new LocationNotFoundException("The selected location for registration does not exist: " + newRegLocationId));
                existingEvent.setRegistrationLocation(regLocation);
                existingEvent.setRegistrationLocationId(newRegLocationId);
                existingEvent.setRegistrationLocationName(regLocation.getLocationName());
            }
            if (newVenueLocationId != null) {
                Location venueLocation = locationRepository.findById(newVenueLocationId)
                        .orElseThrow(() -> new LocationNotFoundException("The selected location for venue does not exist: " + newVenueLocationId));
                existingEvent.setVenueLocation(venueLocation);
                existingEvent.setVenueLocationId(newVenueLocationId);
                existingEvent.setVenueLocationName(venueLocation.getLocationName());
            }

            validateLocationPurposes(existingEvent.getRegistrationLocation(), existingEvent.getVenueLocation());
        } else if (existingEvent.getRegistrationLocationId() == null || existingEvent.getVenueLocationId() == null) {
            throw new IllegalArgumentException("Registration and venue location cannot be removed or left unset");
        }

        if (datesUpdated) {
            setStatusBasedOnCurrentTime(existingEvent);
        }

        if (updateEvent.getEligibleStudents() != null) {
            validateEligibilityCriteria(updateEvent.getEligibleStudents());
            EventEligibility expandedCriteria = populateDomainEligibilityCriteria(updateEvent.getEligibleStudents());
            existingEvent.setEligibleStudents(expandedCriteria);
        }

        existingEvent.setLastModified(LocalDateTime.now());
        checkLocationConflict(existingEvent, eventId);
        Event updatedEvent = eventRepository.save(existingEvent);
        log.debug("Successfully updated event session with ID: {}", eventId);
        return toEventManagementResponse(updatedEvent);
    }

    @Override
    public Event cancelEvent(String eventId) {
        Event existingEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId, true));
        existingEvent.setEventStatus(EventStatus.CANCELLED);
        existingEvent.setLastModified(LocalDateTime.now());
        return eventRepository.save(existingEvent);
    }

    /**
     * PRIVATE HELPERS
     */

    private void checkLocationConflict(Event eventSession, String excludeEventId) {
        if (eventSession.getRegistrationLocationId() == null || eventSession.getVenueLocationId() == null) {
            return;
        }

        Set<Event> conflicts = new HashSet<>();

        LocalDateTime regStart = eventSession.getRegistrationDateTime();
        LocalDateTime regEnd = eventSession.getStartingDateTime();

        List<Event> regLocationEvents = eventRepository.findByRegistrationLocationIdAndEventStatusNotIn(
                eventSession.getRegistrationLocationId(),
                EXCLUDED_CONFLICT_STATUSES
        );

        regLocationEvents.stream().filter(e -> !e.getEventId().equals(excludeEventId))
                .filter(e -> hasTimeOverlap(regStart, regEnd, e.getRegistrationDateTime(), e.getStartingDateTime()))
                .forEach(conflicts::add);

        LocalDateTime eventStart = eventSession.getStartingDateTime();
        LocalDateTime eventEnd = eventSession.getEndingDateTime();

        List<Event> venueLocationEvents = eventRepository
                .findByVenueLocationIdAndEventStatusNotIn(eventSession.getVenueLocationId(),EXCLUDED_CONFLICT_STATUSES);

        venueLocationEvents.stream().filter(e -> !e.getEventId().equals(excludeEventId))
                .filter(e -> hasTimeOverlap(eventStart, eventEnd, e.getStartingDateTime(), e.getEndingDateTime()))
                .forEach(conflicts::add);

        if (!conflicts.isEmpty()) {
            String conflictDetails = conflicts.stream()
                    .map(e -> String.format("%s (Registration: %s-%s, Start-End: %s-%s)",
                            e.getEventName(),
                            e.getRegistrationDateTime(), e.getStartingDateTime(),
                            e.getStartingDateTime(), e.getEndingDateTime()))
                    .collect(Collectors.joining(", "));
            throw new EventLocationConflictException(
                    "Location conflict detected: The selected registration or venue is already in use during the " +
                            "specified times by the following event(s): " + conflictDetails
            );
        }
    }

    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private void setStatusBasedOnCurrentTime(Event event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime registrationStart = event.getRegistrationDateTime();
        LocalDateTime start = event.getStartingDateTime();
        LocalDateTime end = event.getEndingDateTime();

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
            log.debug("Event {} status updated from {} to {} due to date changes", event.getEventId(), currentStatus, newStatus);
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
            throw new InvalidDateRangeException("Please provide all date fields: (Date & Time) for registration, starting, and ending.");
        }

        if (timeInDateTime.isBefore(now)) {
            throw new InvalidDateRangeException("Registration date & time field must be in the future.");
        }
        if (timeInDateTime.isAfter(startDateTime)) {
            throw new InvalidDateRangeException("Registration must start before the event begins.");
        }
        if (startDateTime.isBefore(now)) {
            throw new InvalidDateRangeException("Starting date & time field must be in the future.");
        }
        if (endDateTime.isBefore(now)) {
            throw new InvalidDateRangeException("Ending date & time field must be in the future.");
        }

        if (startDateTime.isAfter(endDateTime)) {
            throw new InvalidDateRangeException("Starting time must be before the end time.");
        }

        long durationInMinutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (durationInMinutes < MIN_EVENT_DURATION_MINUTES) {
            throw new InvalidDateRangeException("Event must last at least " + MIN_EVENT_DURATION_MINUTES + " minutes.");
        }
        if (durationInMinutes > MAX_EVENT_DURATION_MINUTES) {
            throw new InvalidDateRangeException("Event cannot exceed " + MAX_EVENT_DURATION_MINUTES + " minutes in duration.");
        }
    }

    private EventEligibility populateDomainEligibilityCriteria(EventEligibility reqCriteria) {
        if (reqCriteria.isAllStudents()) {
            return EventEligibility.builder().allStudents(true).build();
        }

        Set<String> clusterIds = new HashSet<>();
        Set<String> courseIds = new HashSet<>();
        Set<String> sectionIds = new HashSet<>();

        if (reqCriteria.getClusters() != null && !reqCriteria.getClusters().isEmpty()) {
            clusterIds.addAll(reqCriteria.getClusters());
            for (String clusterId : reqCriteria.getClusters()) {
                List<Courses> coursesUnderCluster = courseRepository.findByClusterClusterId(clusterId);
                for (Courses course : coursesUnderCluster) {
                    courseIds.add(course.getId());
                    List<Sections> sectionsUnderCourse = sectionsRepository.findByCourseId(course.getId());
                    sectionIds.addAll(sectionsUnderCourse.stream().map(Sections::getId).collect(Collectors.toSet()));
                }
            }
        }

        if (reqCriteria.getCourses() != null && !reqCriteria.getCourses().isEmpty()) {
            for (String courseId : reqCriteria.getCourses()) {
                courseIds.add(courseId);
                Courses course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new InvalidEligibilityCriteriaException("Course not found: " + courseId));
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
                        .orElseThrow(() -> new InvalidEligibilityCriteriaException("Section not found: " + sectionId));
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
                .clusters(new ArrayList<>(clusterIds))
                .clusterNames(clusterNames)
                .courses(new ArrayList<>(courseIds))
                .courseNames(courseNames)
                .sections(new ArrayList<>(sectionIds))
                .sectionNames(sectionNames)
                .build();
    }

    private void validateEligibilityCriteria(EventEligibility criteria) {
        if (criteria.isAllStudents()) {
            return;
        }
        List<String> clusterIds = criteria.getClusters();
        List<String> courseIds = criteria.getCourses();
        List<String> sectionIds = criteria.getSections();
        if ((clusterIds == null || clusterIds.isEmpty()) && (courseIds == null || courseIds.isEmpty()) && (sectionIds == null || sectionIds.isEmpty())) {
            throw new InvalidEligibilityCriteriaException("At least one cluster, course, or section must be provided when you are not targeting all student.");
        }
        if ((clusterIds != null && clusterIds.stream().anyMatch(String::isBlank)) || (courseIds != null && courseIds.stream().anyMatch(String::isBlank)) || (sectionIds != null && sectionIds.stream().anyMatch(String::isBlank))) {
            throw new InvalidEligibilityCriteriaException("Criteria cannot be blank.");
        }
    }

    private EventManagementResponse toEventCreationResponse(Event event, String regLocationId, String venueLocationId) {
        EventEligibility criteria = event.getEligibleStudents();

        Location regLocation = locationRepository.findById(regLocationId)
                .orElseThrow(() -> new LocationNotFoundException("Registration Location ID does not exist: " + regLocationId));
        event.setRegistrationLocation(regLocation);
        event.setRegistrationLocationName(regLocation.getLocationName());

        Location venueLocation = locationRepository.findById(venueLocationId)
                .orElseThrow(() -> new LocationNotFoundException("Venue Location ID does not exist: " + venueLocationId));
        event.setVenueLocation(venueLocation);
        event.setVenueLocationName(venueLocation.getLocationName());

        EventManagementResponse response = EventManagementResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .registrationLocationId(event.getRegistrationLocationId())
                .venueLocationId(event.getVenueLocationId())
                .registrationDateTime(event.getRegistrationDateTime())
                .startingDateTime(event.getStartingDateTime())
                .endingDateTime(event.getEndingDateTime())
                .eventStatus(event.getEventStatus())
                .allStudents(criteria == null || criteria.isAllStudents())
                .facialVerificationEnabled(event.getFacialVerificationEnabled())
                .attendanceLocationMonitoringEnabled(event.getAttendanceLocationMonitoringEnabled())
                .build();

        if (criteria != null && !criteria.isAllStudents()) {
            response.setClusterIDs(criteria.getClusters());
            response.setClusterNames(criteria.getClusterNames());
            response.setCourseIDs(criteria.getCourses());
            response.setCourseNames(criteria.getCourseNames());
            response.setSectionIDs(criteria.getSections());
            response.setSectionNames(criteria.getSectionNames());
        }

        response.setRegistrationLocationName(event.getRegistrationLocationName());
        response.setVenueLocationName(event.getVenueLocationName());

        return response;
    }

    private EventManagementResponse toEventManagementResponse(Event event) {
        EventEligibility criteria = event.getEligibleStudents();
        EventManagementResponse response = EventManagementResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .registrationLocationId(event.getRegistrationLocationId())
                .registrationLocationName(event.getRegistrationLocationName())
                .venueLocationId(event.getVenueLocationId())
                .venueLocationName(event.getVenueLocationName())
                .registrationDateTime(event.getRegistrationDateTime())
                .startingDateTime(event.getStartingDateTime())
                .endingDateTime(event.getEndingDateTime())
                .eventStatus(event.getEventStatus())
                .allStudents(criteria == null || criteria.isAllStudents())
                .facialVerificationEnabled(event.getFacialVerificationEnabled())
                .attendanceLocationMonitoringEnabled(event.getAttendanceLocationMonitoringEnabled())
                .build();

        if (criteria != null && !criteria.isAllStudents()) {
            response.setClusterIDs(criteria.getClusters());
            response.setClusterNames(criteria.getClusterNames());
            response.setCourseIDs(criteria.getCourses());
            response.setCourseNames(criteria.getCourseNames());
            response.setSectionIDs(criteria.getSections());
            response.setSectionNames(criteria.getSectionNames());
        }

        return response;
    }

    private void validateLocationPurposes(Location registrationLocation, Location venueLocation) {
        if (registrationLocation != null && !LocationPurpose.REGISTRATION_AREA.equals(registrationLocation.getPurpose())) {
            throw new InvalidLocationPurposeException("Registration location must have purpose REGISTRATION_AREA (current: " + registrationLocation.getPurpose() + ")");
        }
        if (venueLocation != null && !LocationPurpose.EVENT_VENUE.equals(venueLocation.getPurpose())) {
            throw new InvalidLocationPurposeException("Venue location must have purpose EVENT_VENUE (current: " + venueLocation.getPurpose() + ")");
        }
    }
}
package com.attendease.backend.student.service.event.retrieval.homepage;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.events.EventSessions;
import com.attendease.backend.repository.eventSessions.EventSessionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class HomepageEventsService {

    private final EventSessionsRepository eventSessionRepository;

    /**
     * Retrieves all events that are either:
     * <ul>
     *     <li>ONGOING</li>
     *     <li>UPCOMING</li>
     *     <li>REGISTRATION</li>
     * </ul>
     *
     * <p>This is primarily used to display events that students may interact with.</p>
     *
     * @return a list of event sessions matching any of the allowed statuses
     */
    public List<EventSessions> getOngoingRegistrationAndActiveEvents() {
        return eventSessionRepository.findByEventStatusIn(Arrays.asList(EventStatus.ONGOING, EventStatus.UPCOMING, EventStatus.REGISTRATION));
    }
}

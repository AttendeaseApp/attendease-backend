package com.attendease.backend.student.controller.event.retrieval;

import com.attendease.backend.domain.event.Event;
import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventBroadcastService {

	private final SimpMessagingTemplate messagingTemplate;
	private final EventRetrievalService eventRetrievalService;

	private List<Event> cachedEvents = null;

	/**
	 * Send initial broadcast when server starts
	 */
	@PostConstruct
	public void sendInitialBroadcast() {
		log.info("Sending initial event broadcast on startup");
		broadcastHomepageEvents();
	}

	/**
	 * Broadcast homepage events every 30 seconds
	 * Only broadcasts if data has changed to reduce network traffic
	 */
	@Scheduled(fixedDelay = 30000)
	public void broadcastHomepageEvents() {
		try {
			long startTime = System.currentTimeMillis();
			List<Event> currentEvents = eventRetrievalService.getOngoingRegistrationAndActiveEvents();
			if (shouldBroadcast(currentEvents)) {
				messagingTemplate.convertAndSend("/topic/homepage-events", currentEvents);
				cachedEvents = currentEvents;
				long duration = System.currentTimeMillis() - startTime;
				log.info("Broadcast {} events to all subscribers in {}ms", currentEvents.size(), duration);
			} else {
				log.debug("No changes detected, skipping broadcast");
			}
		} catch (Exception e) {
			log.error("Error broadcasting homepage events", e);
		}
	}

	/**
	 * Check if we should broadcast (data changed)
	 */
	private boolean shouldBroadcast(List<Event> currentEvents) {
		if (cachedEvents == null) {
			return true;
		}
		if (cachedEvents.size() != currentEvents.size()) {
			return true;
		}
		for (int i = 0; i < currentEvents.size(); i++) {
			Event current = currentEvents.get(i);
			Event cached = cachedEvents.get(i);
			if (!current.getEventId().equals(cached.getEventId()) || current.getEventStatus() != cached.getEventStatus()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Trigger immediate broadcast when event is created/updated
	 * Call this from your admin event creation/update endpoints
	 */
	public void triggerImmediateBroadcast() {
		log.info("Triggering immediate broadcast");
		cachedEvents = null;
		broadcastHomepageEvents();
	}

	/**
	 * Broadcast specific event update
	 */
	public void broadcastEventUpdate(Event event) {
		log.info("Broadcasting update for event: {}", event.getEventId());
		messagingTemplate.convertAndSend("/topic/events/" + event.getEventId(), event);
	}
}

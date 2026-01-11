package com.attendease.backend.student.service.event.state.listener;

import com.attendease.backend.domain.event.status.EventStatusChangedEvent;
import com.attendease.backend.student.service.event.state.EventStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusChangeListener {

	private final EventStateService eventStateService;

	@EventListener
	public void handleEventStatusChange(EventStatusChangedEvent event) {
		log.info("Event status changed: {} -> {}", event.getEventId(), event.getNewStatus());
		eventStateService.broadcastEventStateChange(event.getEventId());
	}
}

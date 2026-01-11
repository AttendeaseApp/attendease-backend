package com.attendease.backend.domain.event.status;

import com.attendease.backend.domain.enums.EventStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EventStatusChangedEvent extends ApplicationEvent {
	private final String eventId;
	private final EventStatus oldStatus;
	private final EventStatus newStatus;

	public EventStatusChangedEvent(Object source, String eventId, EventStatus oldStatus, EventStatus newStatus) {
		super(source);
		this.eventId = eventId;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}
}

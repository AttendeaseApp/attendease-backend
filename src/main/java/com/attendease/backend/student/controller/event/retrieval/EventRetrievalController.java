package com.attendease.backend.student.controller.event.retrieval;

import com.attendease.backend.domain.event.Event;
import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/event/registration")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class EventRetrievalController {

	private final EventRetrievalService eventRetrievalService;

	@GetMapping("/homepage")
	public List<Event> getHomepageEvents() {
		return eventRetrievalService.getOngoingRegistrationAndActiveEvents();
	}

	@GetMapping("/{eventId}")
	public ResponseEntity<Event> getEventById(@PathVariable String eventId) {
		return eventRetrievalService.getEventById(eventId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}
}

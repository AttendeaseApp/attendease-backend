package com.attendease.backend.student.controller.event.registration;

import com.attendease.backend.domain.event.Event;
import com.attendease.backend.student.service.event.retrieval.EventRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/student/event")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class RestEventRetrievalController {

	private final EventRetrievalService eventRetrievalService;

	@GetMapping("/homepage")
	public List<Event> getHomepageEvents() {
		return eventRetrievalService.getOngoingRegistrationAndActiveEvents();
	}
}

package com.attendease.backend.osa.controller.academic.year.scheduler;

import com.attendease.backend.schedulers.academic.AcademicYearActivationScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for manually triggering scheduled tasks.
 * <p>
 * Useful for testing and manual intervention when needed.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-28
 */
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AcademicYearSchedulerController {

	private final AcademicYearActivationScheduler academicYearActivationScheduler;

	/**
	 * Manually triggers the academic year activation check.
	 * <p>
	 * This endpoint allows OSA administrators to manually run the scheduler
	 * logic without waiting for the scheduled time (midnight).
	 * </p>
	 * <p>
	 * Use cases:
	 * <ul>
	 *   <li>Testing the scheduler logic</li>
	 *   <li>Immediate activation when an academic year should start</li>
	 *   <li>Troubleshooting semester transition issues</li>
	 * </ul>
	 * </p>
	 *
	 * @return success message
	 */
	@PostMapping("/trigger-academic-year-activation")
	public ResponseEntity<Map<String, String>> triggerAcademicYearActivation() {
		academicYearActivationScheduler.manualTrigger();
		return ResponseEntity.ok(Map.of(
				"status", "success",
				"message", "Academic year activation check has been triggered successfully. Check logs for details."
		));
	}
}

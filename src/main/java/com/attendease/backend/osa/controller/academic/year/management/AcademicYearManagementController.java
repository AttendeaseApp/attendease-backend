package com.attendease.backend.osa.controller.academic.year.management;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.academic.info.AcademicYearResponse;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * {@code AcademicYearManagementController} is used for managing academic years.
 * <p>
 * This controller provides CRUD operations for academic years and semester management,
 * ensuring that all endpoints are secured for OSA (Office of Student Affairs) role users only.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-24
 */
@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AcademicYearManagementController {

	private final AcademicYearManagementService academicYearManagementService;

	/**
	 * Creates a new academic year.
	 * <p>
	 * Validates dates, checks for overlaps with existing years, and handles activation if requested.
	 * </p>
	 *
	 * @param academicYear the academic year to create
	 * @return the created academic year response with calculated status and progress
	 */
	@PostMapping
	public ResponseEntity<?> createAcademicYear(
			@Valid
			@RequestBody Academic academicYear) {
		try {
			AcademicYearResponse response = academicYearManagementService.createAcademicYear(academicYear);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
		}
	}

	/**
	 * Retrieves all academic years.
	 *
	 * @return list of all academic year responses with calculated status and progress
	 */
	@GetMapping
	public ResponseEntity<List<AcademicYearResponse>> getAllAcademicYears() {
		List<AcademicYearResponse> academicYears = academicYearManagementService.getAllAcademicYears();
		return ResponseEntity.ok(academicYears);
	}

	/**
	 * Retrieves the currently active academic year.
	 *
	 * @return the active academic year response, or 404 if none is active
	 */
	@GetMapping("/active")
	public ResponseEntity<AcademicYearResponse> getActiveAcademicYear() {
		return academicYearManagementService.getActiveAcademicYear()
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Retrieves an academic year by its ID.
	 *
	 * @param id the academic year ID
	 * @return the academic year response
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> getAcademicYearById(@PathVariable String id) {
		try {
			AcademicYearResponse response = academicYearManagementService.getAcademicYearById(id);
			return ResponseEntity.ok(response);
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
		}
	}

	/**
	 * Updates an existing academic year.
	 * <p>
	 * Validates dates, checks for overlaps (excluding itself), and handles activation changes.
	 * </p>
	 *
	 * @param id the academic year ID
	 * @param academicYear the updated academic year data
	 * @return the updated academic year response
	 */
	@PutMapping("/{id}")
	public ResponseEntity<?> updateAcademicYear(
			@PathVariable String id,
			@Valid
			@RequestBody Academic academicYear) {
		try {
			AcademicYearResponse response = academicYearManagementService.updateAcademicYear(id, academicYear);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
		}
	}

	/**
	 * Deletes an academic year.
	 * <p>
	 * Cannot delete an active academic year or one with dependencies.
	 * </p>
	 *
	 * @param id the academic year ID
	 * @return 204 No Content if successful
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteAcademicYear(@PathVariable String id) {
		try {
			academicYearManagementService.deleteAcademicYear(id);
			return ResponseEntity.noContent().build();
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
		}
	}

	/**
	 * Sets an academic year as active.
	 * <p>
	 * Validates that the year can be activated (not finished, not too far in future, no other year in progress)
	 * and deactivates all other academic years.
	 * </p>
	 *
	 * @param id the academic year ID to activate
	 * @return the activated academic year response
	 */
	@PutMapping("/{id}/activate")
	public ResponseEntity<?> setActiveAcademicYear(@PathVariable String id) {
		try {
			AcademicYearResponse response = academicYearManagementService.setActiveAcademicYear(id);
			return ResponseEntity.ok(response);
		} catch (IllegalStateException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
		} catch (RuntimeException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
		}
	}

	/**
	 * Gets the current semester number (1 or 2).
	 *
	 * @return the current semester number, or 204 if no academic year is active or between semesters
	 */
	@GetMapping("/current-semester")
	public ResponseEntity<?> getCurrentSemester() {
		Integer semester = academicYearManagementService.getCurrentSemester();
		if (semester != null) {
			return ResponseEntity.ok(Map.of("currentSemester", semester));
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(Map.of("message", "No active academic year or currently between semesters"));
	}

	/**
	 * Gets the display name of the current semester (e.g., "First Semester", "Second Semester").
	 *
	 * @return the current semester name, or appropriate message if none is active
	 */
	@GetMapping("/current-semester-name")
	public ResponseEntity<?> getCurrentSemesterName() {
		String semesterName = academicYearManagementService.getCurrentSemesterName();
		if (semesterName != null) {
			return ResponseEntity.ok(Map.of("currentSemesterName", semesterName));
		}
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(Map.of("message", "No active academic year or currently between semesters"));
	}

	/**
	 * Gets semester status information.
	 *
	 * @return information about which semesters are active
	 */
	@GetMapping("/semester-status")
	public ResponseEntity<Map<String, Object>> getSemesterStatus() {
		return ResponseEntity.ok(Map.of(
				"isFirstSemesterActive", academicYearManagementService.isFirstSemesterActive(),
				"isSecondSemesterActive", academicYearManagementService.isSecondSemesterActive(),
				"currentSemester", academicYearManagementService.getCurrentSemester() != null
						? academicYearManagementService.getCurrentSemester()
						: "None",
				"currentSemesterName", academicYearManagementService.getCurrentSemesterName() != null
						? academicYearManagementService.getCurrentSemesterName()
						: "None"
		));
	}
}
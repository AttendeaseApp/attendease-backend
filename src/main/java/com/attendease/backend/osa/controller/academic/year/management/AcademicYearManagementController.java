package com.attendease.backend.osa.controller.academic.year.management;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AcademicYearManagementController {

	private final AcademicYearManagementService academicYearManagementService;

	@PostMapping
	public ResponseEntity<Academic> createAcademicYear(
			@Valid
			@RequestBody Academic academicYear)
	{
		return ResponseEntity.ok(academicYearManagementService.createAcademicYear(academicYear));
	}

	@GetMapping
	public ResponseEntity<List<Academic>> getAllAcademicYears() {
		return ResponseEntity.ok(academicYearManagementService.getAllAcademicYears());
	}

	@GetMapping("/active")
	public ResponseEntity<Academic> getActiveAcademicYear() {
		return academicYearManagementService.getActiveAcademicYear()
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Academic> getAcademicYearById(@PathVariable String id) {
		return ResponseEntity.ok(academicYearManagementService.getAcademicYearById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Academic> updateAcademicYear(
			@PathVariable String id,
			@RequestBody Academic academicYear)
	{
		return ResponseEntity.ok(academicYearManagementService.updateAcademicYear(id, academicYear));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAcademicYear(
			@PathVariable String id)
	{
		academicYearManagementService.deleteAcademicYear(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/activate")
	public ResponseEntity<Academic> setActiveAcademicYear(
			@PathVariable String id)
	{
		return ResponseEntity.ok(academicYearManagementService.setActiveAcademicYear(id));
	}

	@GetMapping("/current-semester")
	public ResponseEntity<Integer> getCurrentSemester() {
		Integer semester = academicYearManagementService.getCurrentSemester();
		return semester != null ? ResponseEntity.ok(semester) : ResponseEntity.noContent().build();
	}

	@GetMapping("/current-semester-name")
	public String getCurrentSemesterName() {
		return academicYearManagementService.getCurrentSemesterName();
	}
}

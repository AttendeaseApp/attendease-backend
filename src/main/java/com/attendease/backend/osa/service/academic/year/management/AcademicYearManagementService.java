package com.attendease.backend.osa.service.academic.year.management;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.academic.info.AcademicYearResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing academic years.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-25
 */
public interface AcademicYearManagementService {

	/**
	 * Creates a new academic year.
	 * <p>
	 * Validates dates, checks for overlaps with existing years, and handles activation if requested.
	 * </p>
	 *
	 * @param academicYear the academic year to create
	 * @return the created academic year response
	 */
	AcademicYearResponse createAcademicYear(Academic academicYear);

	/**
	 * Retrieves all academic years.
	 *
	 * @return list of all academic year responses with calculated status and progress
	 */
	List<AcademicYearResponse> getAllAcademicYears();

	/**
	 * Retrieves the currently active academic year.
	 *
	 * @return optional containing the active academic year response, or empty if none is active
	 */
	Optional<AcademicYearResponse> getActiveAcademicYear();

	/**
	 * Retrieves an academic year by its ID.
	 *
	 * @param id the academic year ID
	 * @return the academic year response
	 */
	AcademicYearResponse getAcademicYearById(String id);

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
	AcademicYearResponse updateAcademicYear(String id, Academic academicYear);

	/**
	 * Deletes an academic year.
	 * <p>
	 * Cannot delete an active academic year or one with dependencies.
	 * </p>
	 *
	 * @param id the academic year ID
	 */
	void deleteAcademicYear(String id);

	/**
	 * Sets an academic year as active.
	 * <p>
	 * Validates that the year can be activated and deactivates all other academic years.
	 * </p>
	 *
	 * @param id the academic year ID to activate
	 * @return the activated academic year response
	 */
	AcademicYearResponse setActiveAcademicYear(String id);

	/**
	 * Gets the display name of the current semester (e.g., "First Semester", "Second Semester").
	 *
	 * @return the current semester name, or null if no academic year is active or between semesters
	 */
	String getCurrentSemesterName();

	/**
	 * Gets the number of the current semester (1 or 2).
	 *
	 * @return the current semester number, or null if no academic year is active or between semesters
	 */
	Integer getCurrentSemester();

	/**
	 * Checks if the first semester is currently active.
	 *
	 * @return true if first semester is active, false otherwise
	 */
	boolean isFirstSemesterActive();

	/**
	 * Checks if the second semester is currently active.
	 *
	 * @return true if second semester is active, false otherwise
	 */
	boolean isSecondSemesterActive();
}
package com.attendease.backend.domain.enums.attendance;

/**
 * Enumeration defining the sorting criteria for attendance records.
 * Used to group and organize attendees by academic hierarchy.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-12
 */
public enum AttendanceSortCriteria {
	/**
	 * Sort by cluster (top-level academic grouping)
	 */
	CLUSTER,
	/**
	 * Sort by course (mid-level academic grouping)
	 */
	COURSE,
	/**
	 * Sort by section (lowest-level academic grouping)
	 */
	SECTION,
	/**
	 * Sort by year level
	 */
	YEAR_LEVEL
}

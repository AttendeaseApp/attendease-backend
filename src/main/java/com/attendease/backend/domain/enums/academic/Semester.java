package com.attendease.backend.domain.enums.academic;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing academic semesters.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-26
 */
@Getter
@RequiredArgsConstructor
public enum Semester {

	FIRST("First Semester", 1),
	SECOND("Second Semester", 2);

	@JsonValue
	private final String displayName;
	private final int number;

	/**
	 * Get semester by number.
	 */
	public static Semester fromNumber(int number) {
		for (Semester semester : values()) {
			if (semester.number == number) {
				return semester;
			}
		}
		throw new IllegalArgumentException("Invalid semester number: " + number);
	}

	/**
	 * Get semester by display name.
	 */
	public static Semester fromDisplayName(String displayName) {
		if (displayName == null) {
			return null;
		}
		for (Semester semester : values()) {
			if (semester.displayName.equals(displayName)) {
				return semester;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return displayName;
	}
}

package com.attendease.backend.osa.service.utility.academic.section.management;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SectionHelper {

	/**
	 * Get year level from section number
	 * @param sectionNumber e.g., "101", "301", "501"
	 * @return year level (1-4)
	 */
	public static Integer getYearLevelFromSectionNumber(String sectionNumber) {
		if (sectionNumber == null || sectionNumber.length() != 3) {
			return null;
		}

		int firstDigit = Character.getNumericValue(sectionNumber.charAt(0));

		return switch (firstDigit) {
			case 1, 2 -> 1;
			case 3, 4 -> 2;
			case 5, 6 -> 3;
			case 7, 8 -> 4;
			default -> null;
		};
	}

	/**
	 * Get semester from section number
	 * @param sectionNumber e.g., "101", "201", "301"
	 * @return semester (1 or 2)
	 */
	public static Integer getSemesterFromSectionNumber(String sectionNumber) {
		if (sectionNumber == null || sectionNumber.length() != 3) {
			return null;
		}

		int firstDigit = Character.getNumericValue(sectionNumber.charAt(0));

		// Odd numbers = First semester, Even numbers = Second semester
		return (firstDigit % 2 == 1) ? 1 : 2;
	}

	/**
	 * Generate section number based on year level, semester, and sub-number
	 * @param yearLevel 1-4
	 * @param semester 1-2
	 * @param subNumber 1-99
	 * @return section number e.g., "101", "202", "315"
	 */
	public static String generateSectionNumber(Integer yearLevel, Integer semester, Integer subNumber) {
		if (yearLevel < 1 || yearLevel > 4) {
			throw new IllegalArgumentException("Year level must be between 1 and 4");
		}
		if (semester < 1 || semester > 2) {
			throw new IllegalArgumentException("Semester must be 1 or 2");
		}
		if (subNumber < 1 || subNumber > 99) {
			throw new IllegalArgumentException("Sub-number must be between 1 and 99");
		}

		int firstDigit = switch (yearLevel) {
			case 1 -> semester == 1 ? 1 : 2;
			case 2 -> semester == 1 ? 3 : 4;
			case 3 -> semester == 1 ? 5 : 6;
			case 4 -> semester == 1 ? 7 : 8;
			default -> throw new IllegalArgumentException("Invalid year level");
		};

		return String.format("%d%02d", firstDigit, subNumber);
	}

	/**
	 * Validate if section number format is correct
	 * @param sectionNumber e.g., "101", "815"
	 * @return true if valid, false otherwise
	 */
	public static boolean isValidSectionNumber(String sectionNumber) {
		if (sectionNumber == null || sectionNumber.length() != 3) {
			return false;
		}

		try {
			int number = Integer.parseInt(sectionNumber);
			int firstDigit = number / 100;
			return firstDigit >= 1 && firstDigit <= 8;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Get descriptive text for section
	 * @param sectionNumber e.g., "101"
	 * @return e.g., "Year 1, First Semester"
	 */
	public static String getSectionDescription(String sectionNumber) {
		Integer yearLevel = getYearLevelFromSectionNumber(sectionNumber);
		Integer semester = getSemesterFromSectionNumber(sectionNumber);

		if (yearLevel == null || semester == null) {
			return "Invalid section";
		}

		String semesterName = semester == 1 ? "First Semester" : "Second Semester";
		return "Year " + yearLevel + ", " + semesterName;
	}
}

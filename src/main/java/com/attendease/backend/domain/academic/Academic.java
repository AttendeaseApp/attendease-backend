package com.attendease.backend.domain.academic;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing an academic year with two semesters.
 * <p>
 * Defines the academic calendar structure with distinct first and second semesters.
 * Each semester has start/end dates and associated year levels with corresponding
 * section numbers.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "academic_year")
public class Academic {

	@Id
	private String id;

	@NotBlank
	@Indexed(unique = true)
	private String academicYearName;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate firstSemesterStart;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate firstSemesterEnd;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate secondSemesterStart;

	@NotNull
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate secondSemesterEnd;

	@Builder.Default
	private boolean isActive = false;

	@CreatedDate
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@LastModifiedDate
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	/**
	 * Determines current semester based on the current date.
	 *
	 * @param currentDate the date to check
	 * @return 1 for first semester, 2 for second semester, 0 if outside both semesters
	 */
	public int getCurrentSemester(LocalDate currentDate) {
		if (!currentDate.isBefore(firstSemesterStart) && !currentDate.isAfter(firstSemesterEnd)) {
			return 1;
		} else if (!currentDate.isBefore(secondSemesterStart) && !currentDate.isAfter(secondSemesterEnd)) {
			return 2;
		}
		return 0;
	}
}

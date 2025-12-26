package com.attendease.backend.domain.sections;

import com.attendease.backend.domain.academic.Academic;
import com.attendease.backend.domain.course.Course;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain entity representing a course section (e.g., "BSIT-101") in the Attendease system.
 * <p>
 * Sections are the lowest-level grouping: children of {@link Course} and linked to {@link Academic},
 * representing year-level or specialized cohorts (e.g., 101 = first year). Full name format: "COURSE_NAME-SECTION_NUMBER".
 * Used for precise eligibility in events (e.g., section-specific registrations).
 * </p>
 * <p>Self validating domain :)</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sections")
public class Sections {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sectionName;

    private Integer yearLevel;

    private Integer semester;

    @DBRef
    private Course course;

    @DBRef
    private Academic academicYear;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Extracts year level and semester from section number
     * Logic:
     * - First semester (X01): 101, 301, 501, 701
     * - Second semester (X02): 201, 401, 601, 801
     * where X represents the year level
     */
    public void calculateYearLevelAndSemester() {
        if (this.sectionName == null || !this.sectionName.contains("-")) {
            return;
        }

        String[] parts = this.sectionName.split("-");
        if (parts.length != 2) {
            return;
        }

        String sectionNumber = parts[1];
        if (sectionNumber.length() != 3) {
            return;
        }

        int firstDigit = Character.getNumericValue(sectionNumber.charAt(0));

        // Determine year level and semester based on first digit
        switch (firstDigit) {
            case 1: // 1XX
                this.yearLevel = 1;
                this.semester = 1;
                break;
            case 2: // 2XX
                this.yearLevel = 1;
                this.semester = 2;
                break;
            case 3: // 3XX
                this.yearLevel = 2;
                this.semester = 1;
                break;
            case 4: // 4XX
                this.yearLevel = 2;
                this.semester = 2;
                break;
            case 5: // 5XX
                this.yearLevel = 3;
                this.semester = 1;
                break;
            case 6: // 6XX
                this.yearLevel = 3;
                this.semester = 2;
                break;
            case 7: // 7XX
                this.yearLevel = 4;
                this.semester = 1;
                break;
            case 8: // 8XX
                this.yearLevel = 4;
                this.semester = 2;
                break;
            default:
                throw new IllegalArgumentException("Invalid section number: " + sectionNumber);
        }
    }

    // TODO: CUSTOM EXCEPTION
    /**
     * Validates that this section's semester matches the academic year's current semester
     * @throws IllegalStateException if semester doesn't match
     */
    public void validateSemesterMatchesAcademicYear() {
        if (this.academicYear == null) {
            throw new IllegalStateException("Section must be linked to an academic year");
        }

        if (this.academicYear.getCurrentSemester() == null) {
            throw new IllegalStateException("Academic year does not have an active semester");
        }

        Integer currentAcademicSemester = this.academicYear.getCurrentSemester().getNumber();
        if (!currentAcademicSemester.equals(this.semester)) {
            throw new IllegalStateException(
                    "Section semester (" + this.semester + ") does not match academic year's current semester ("
                            + currentAcademicSemester + ")"
            );
        }
    }
}
package com.attendease.backend.domain.section;

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
@Document(collection = "section")
public class Section {

    @Id
    private String id;

    @Indexed(unique = true)
    private String sectionName;

    private Integer yearLevel;

    private Integer semester;

    @DBRef
    private Course course;

    @Builder.Default
    private Boolean isActive = true;

    @DBRef
    private Academic academicYear;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * activates this section, making it available for student enrollment
     */
    public void activate() {
        this.isActive = true;
    }


    /**
     * deactivates this section, preventing new enrollments while preserving historical data
     */
    public void deactivate() {
        this.isActive = false;
    }


    /**
     * checks if this section is currently active
     */
    public boolean isCurrentlyActive() {
        return Boolean.TRUE.equals(this.isActive);
    }


    /**
     * gets the next progression section name for a student advancing to the next semester or year
     */
    public String getNextSectionName(boolean isNewAcademicYear) {
        if (this.sectionName == null || this.course == null) {
            throw new IllegalStateException("Section must have name and course to calculate next section");
        }
        int nextYearLevel;
        int nextSemester;
        if (isNewAcademicYear) {
            nextYearLevel = this.yearLevel + 1;
            nextSemester = 1;
            if (nextYearLevel > 4) {
                throw new IllegalStateException(
                        "Student has completed all year levels (currently in " + this.sectionName + ")"
                );
            }
        } else {
            nextYearLevel = this.yearLevel;
            nextSemester = 2;
        }
        int nextSectionNumber = calculateSectionNumberForYearAndSemester(nextYearLevel, nextSemester);
        return this.course.getCourseName() + "-" + nextSectionNumber;
    }


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

    /**
     * helper method to calculate section number from year level and semester
     */
    private int calculateSectionNumberForYearAndSemester(int yearLevel, int semester) {
        int firstDigit;
        if (yearLevel == 1) {
            firstDigit = semester == 1 ? 1 : 2;
        } else if (yearLevel == 2) {
            firstDigit = semester == 1 ? 3 : 4;
        } else if (yearLevel == 3) {
            firstDigit = semester == 1 ? 5 : 6;
        } else if (yearLevel == 4) {
            firstDigit = semester == 1 ? 7 : 8;
        } else {
            throw new IllegalArgumentException("Invalid year level: " + yearLevel);
        }
        return firstDigit * 100 + 1;
    }
}
package com.attendease.backend.domain.student;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.user.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain entity representing a student in the Attendease system.
 * <p>
 * Links user profile, biometrics, and academic section for comprehensive student data.
 * Used as the core entity for attendance, events, and eligibility checks. Unique index
 * on {@code studentNumber} for institutional ID lookups.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "student")
public class Students {

    @Id
    private String id;

    @DBRef
    @NotNull(message = "User reference is required")
    private User user;

    @Indexed
    private String userId;

    @DBRef
    private BiometricData facialData;

    @NotBlank(message = "Student number is required")
    @Indexed(unique = true)
    private String studentNumber;

    @DBRef
    private Section section;

    @Min(value = 1, message = "Year level must be between 1 and 4")
    @Max(value = 4, message = "Year level must be between 1 and 4")
    private Integer yearLevel;

    private String sectionName;
    private String courseName;
    private String clusterName;

    /**
     * Updates student's section and caches related info
     */
    public void updateSection(Section newSection) {
        this.section = newSection;
        this.sectionName = newSection.getSectionName();
        this.courseName = newSection.getCourse().getCourseName();
        this.clusterName = newSection.getCourse().getCluster().getClusterName();
        this.yearLevel = newSection.getYearLevel();
    }

    /**
     * Gets admission year from user account creation date
     */
    public Integer getAdmissionYear() {
        if (user != null && user.getCreatedAt() != null) {
            return user.getCreatedAt().getYear();
        }
        return null;
    }

    /**
     * Gets expected graduation year based on admission year (4-year program)
     */
    public Integer getExpectedGraduationYear() {
        Integer admissionYear = getAdmissionYear();
        if (admissionYear != null) {
            return admissionYear + 4;
        }
        return null;
    }
}

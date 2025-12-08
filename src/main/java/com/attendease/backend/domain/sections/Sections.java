package com.attendease.backend.domain.sections;

import com.attendease.backend.domain.courses.Courses;
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
 * Sections are the lowest-level grouping: children of {@link com.attendease.backend.domain.courses.Courses},
 * representing year-level or specialized cohorts (e.g., 101 = first year). Full name format: "COURSE_NAME-SECTION_NUMBER".
 * Used for precise eligibility in events (e.g., section-specific registrations).
 * </p>
 * <p><b>Usage Notes:</b> Unique index on {@code sectionName}. Auto-generated from parent course creation.</p>
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

    @DBRef
    private Courses course;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

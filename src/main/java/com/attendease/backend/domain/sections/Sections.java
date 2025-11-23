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
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain entity representing a course sections (e.g., "BSIT-101").
 *
 * <p>Sections are child entities of courses, representing year-level or specialized groups
 * (e.g., 101 for first year). Names use full format "COURSE_NAME-SECTION_NUMBER" (e.g., "BSIT-101").</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-11-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sections")
public class Sections {

    @Id
    private String id;

    private String name;

    @DBRef
    private Courses course;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

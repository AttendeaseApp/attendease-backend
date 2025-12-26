package com.attendease.backend.domain.course;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.section.Section;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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
 * Domain entity representing an academic course in the Attendease system (e.g., "BSIT" for Bachelor of Science in Information Technology).
 * <p>
 * Courses are mid-level entities: children of {@link com.attendease.backend.domain.clusters.Clusters} and parents
 * to {@link Section}. Upon creation, the system can auto-generate default
 * sections (e.g., "BSIT-101" to "BSIT-801" for year levels). Used for fine-grained eligibility in events.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "course")
public class Course {

    @Id
    private String id;

    @NotBlank(message = "Course name is required")
    @Indexed(unique = true)
    private String courseName;

    @DBRef
    private Clusters cluster;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

package com.attendease.backend.domain.courses;

import com.attendease.backend.domain.clusters.Clusters;
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
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain entity representing an academic course (e.g., "BSIT").
 *
 * <p>Courses are child entities of clusters and parent entities of sections.
 * Upon creation, default sections (e.g., "BSIT-101" to "BSIT-801") are automatically generated.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "courses")
public class Courses {

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

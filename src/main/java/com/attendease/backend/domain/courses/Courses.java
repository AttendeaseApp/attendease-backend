package com.attendease.backend.domain.courses;

import com.attendease.backend.domain.clusters.Clusters;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
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

import java.time.LocalDateTime;

/**
 * Class representing a course.
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
    @Indexed
    private String courseName;

    @DBRef
    private Clusters cluster;

    private String createdByUserId;
    private String updatedByUserId;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
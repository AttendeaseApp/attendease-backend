package com.attendease.backend.domain.clusters;

import com.attendease.backend.domain.course.Course;
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
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Domain entity representing an academic cluster.
 * <p>
 * Clusters act as top-level groupings for related courses (e.g., "CETE" for engineering programs).
 * They serve as parent entities to {@link Course}, enabling
 * hierarchical organization for event eligibility (e.g., cluster-wide registrations).
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "clusters")
public class Clusters {

    @Id
    private String clusterId;

    @NotBlank(message = "Cluster name is required")
    @Indexed(unique = true)
    private String clusterName;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

package com.attendease.backend.domain.clusters;

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
 * Domain entity representing a clusters.
 *
 * <p>Clusters serve as parent entities for courses, grouping related academic programs.
 * Each cluster has a unique name, and auditing timestamps are automatically managed
 * via Spring Data MongoDB (@CreatedDate, @LastModifiedDate). Enable {@code @EnableMongoAuditing}
 * in the application configuration for timestamp population.</p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-09-16
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

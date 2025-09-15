package com.attendease.backend.domain.students;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Class representing a student cluster.
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

    private String createdByUserId;
    private String updatedByUserId;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

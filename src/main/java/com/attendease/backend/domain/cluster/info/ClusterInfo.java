package com.attendease.backend.domain.cluster.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for cluster information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClusterInfo {
	private String id;
	private String clusterName;
}

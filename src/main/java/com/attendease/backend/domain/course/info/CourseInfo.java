package com.attendease.backend.domain.course.info;

import com.attendease.backend.domain.cluster.info.ClusterInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nested DTO for course information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class CourseInfo {
	private String id;
	private String courseName;
	private ClusterInfo cluster;
}

package com.attendease.backend.domain.section.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSectionRequest {

	private String sectionName;
	private Integer yearLevel;
	private Integer semester;
}

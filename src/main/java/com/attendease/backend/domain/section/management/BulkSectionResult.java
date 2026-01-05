package com.attendease.backend.domain.section.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSectionResult {
	private List<SectionResponse> successful;
	private List<BulkSectionError> errors;
	private Integer totalProcessed;
	private Integer successCount;
	private Integer errorCount;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BulkSectionError {
		private Integer index;
		private String sectionName;
		private String errorMessage;
	}
}

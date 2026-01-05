package com.attendease.backend.domain.exception.error.csv;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvImportErrorResponse {
	private String errorCode;
	private String message;
	private ErrorSummary summary;
	private List<RowError> details;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ErrorSummary {
		private Integer totalRows;
		private Integer successfulRows;
		private Integer failedRows;
		private Map<String, Integer> errorTypes;
		private List<String> missingSections;
		private List<String> duplicateStudentNumbers;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RowError {
		private Integer row;
		private List<String> errors;
	}

	public static CsvImportErrorResponse fromErrors(List<RowError> errors, int totalRows) {
		Set<String> missingSections = new LinkedHashSet<>();
		Set<String> duplicateStudents = new LinkedHashSet<>();
		Map<String, Integer> errorTypeCounts = new HashMap<>();

		for (RowError error : errors) {
			for (String errorMsg : error.getErrors()) {
				if (errorMsg.contains("does not exist")) {
					String sectionName = extractSectionName(errorMsg);
					if (sectionName != null) {
						missingSections.add(sectionName);
						errorTypeCounts.merge("missing_section", 1, Integer::sum);
					}
				} else if (errorMsg.contains("Duplicate student number")) {
					String studentNum = extractStudentNumber(errorMsg);
					if (studentNum != null) {
						duplicateStudents.add(studentNum);
						errorTypeCounts.merge("duplicate_student", 1, Integer::sum);
					}
				} else {
					errorTypeCounts.merge("other", 1, Integer::sum);
				}
			}
		}

		ErrorSummary summary = ErrorSummary.builder()
				.totalRows(totalRows)
				.successfulRows(totalRows - errors.size())
				.failedRows(errors.size())
				.errorTypes(errorTypeCounts)
				.missingSections(new ArrayList<>(missingSections))
				.duplicateStudentNumbers(new ArrayList<>(duplicateStudents))
				.build();

		return CsvImportErrorResponse.builder()
				.errorCode("CSV_IMPORT_ERROR")
				.message(buildUserFriendlyMessage(summary))
				.summary(summary)
				.details(errors)
				.timestamp(LocalDateTime.now())
				.build();
	}

	private static String buildUserFriendlyMessage(ErrorSummary summary) {
		StringBuilder msg = new StringBuilder();
		msg.append("CSV import failed: ")
				.append(summary.getFailedRows())
				.append(" of ")
				.append(summary.getTotalRows())
				.append(" rows had errors. ");

		if (!summary.getMissingSections().isEmpty()) {
			msg.append("Missing sections: ")
					.append(String.join(", ", summary.getMissingSections()))
					.append(". Create these sections first. ");
		}

		if (!summary.getDuplicateStudentNumbers().isEmpty()) {
			msg.append("Duplicate student numbers found: ")
					.append(String.join(", ", summary.getDuplicateStudentNumbers()))
					.append(". ");
		}

		return msg.toString();
	}

	private static String extractSectionName(String errorMsg) {
		int start = errorMsg.indexOf("'");
		int end = errorMsg.indexOf("'", start + 1);
		if (start != -1 && end != -1) {
			return errorMsg.substring(start + 1, end);
		}
		return null;
	}

	private static String extractStudentNumber(String errorMsg) {
		int colonIndex = errorMsg.indexOf(":");
		if (colonIndex != -1) {
			return errorMsg.substring(colonIndex + 1).trim();
		}
		return null;
	}
}

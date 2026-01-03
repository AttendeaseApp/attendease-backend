package com.attendease.backend.domain.section.management;

import com.attendease.backend.domain.academic.info.AcademicYearInfo;
import com.attendease.backend.domain.cluster.info.ClusterInfo;
import com.attendease.backend.domain.course.info.CourseInfo;
import com.attendease.backend.domain.section.Section;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Section entity.
 * <p>
 * Provides a flattened view of section data with embedded course, cluster, and academic year information
 * to avoid circular references and improve API performance.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class SectionResponse {

	private String id;
	private String sectionName;
	private Integer yearLevel;
	private Integer semester;
	private CourseInfo course;
	private Boolean isActive;
	private AcademicYearInfo academicYear;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedAt;

	/**
	 * converts the section entity to a response dto
	 */
	public static SectionResponse fromEntity(Section section) {
		if (section == null) {
			return null;
		}

		SectionResponseBuilder builder = SectionResponse.builder()
				.id(section.getId())
				.sectionName(section.getSectionName())
				.yearLevel(section.getYearLevel())
				.semester(section.getSemester())
				.isActive(section.getIsActive())
				.createdAt(section.getCreatedAt())
				.updatedAt(section.getUpdatedAt());

		if (section.getCourse() != null) {
			CourseInfo.CourseInfoBuilder courseBuilder = CourseInfo.builder().id(section.getCourse().getId())
					.courseName(section.getCourse().getCourseName());

			if (section.getCourse().getCluster() != null) {
				courseBuilder.cluster(ClusterInfo.builder().id(section.getCourse().getCluster().getClusterId())
						.clusterName(section.getCourse().getCluster().getClusterName())
						.build());
			}
			builder.course(courseBuilder.build());
		}

		if (section.getAcademicYear() != null) {
			AcademicYearInfo.AcademicYearInfoBuilder academicBuilder = AcademicYearInfo.builder().id(section.getAcademicYear().getId())
					.academicYearName(section.getAcademicYear().getAcademicYearName())
					.isActive(section.getAcademicYear().isActive());

			if (section.getAcademicYear().getCurrentSemester() != null) {
				academicBuilder.currentSemester(section.getAcademicYear().getCurrentSemester().getDisplayName())
						.currentSemesterNumber(section.getAcademicYear().getCurrentSemester().getNumber());
			}
			builder.academicYear(academicBuilder.build());
		}
		return builder.build();
	}
}

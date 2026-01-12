package com.attendease.backend.domain.attendance.sorted;

import com.attendease.backend.domain.attendance.Monitoring.Records.Attendees.Response.AttendeesResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response object for attendance records grouped and sorted by academic hierarchy.
 * Supports sorting by cluster, course, or section.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2026-Jan-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortedAttendanceRecordsResponse {

	private String eventId;
	private String eventName;
	private String sortBy;
	private int totalAttendees;

	private Map<String, GroupedAttendees> groupedAttendees;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class GroupedAttendees {
		private String groupName;
		private int count;
		private List<AttendeesResponse> attendees;
		private String clusterName;
		private String courseName;
		private String sectionName;
		private Integer yearLevel;
	}
}

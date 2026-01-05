package com.attendease.backend.domain.user.account.management.users.information;

import lombok.Data;

import java.util.List;

@Data
public class BulkStudentSectionUpdateRequest {

	private String sectionId;
	private List<String> studentIds;
}

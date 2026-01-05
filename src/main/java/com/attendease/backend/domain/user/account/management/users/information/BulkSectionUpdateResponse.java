package com.attendease.backend.domain.user.account.management.users.information;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BulkSectionUpdateResponse {

	private int updatedCount;
	private String message;
}

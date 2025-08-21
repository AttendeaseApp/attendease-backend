package com.attendease.backend.userManagement.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkUserOperationsDTO {
    @NotEmpty(message = "User IDs list cannot be empty")
    private List<String> userIds;
}
package com.attendease.backend.data.dto.users;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkUserOperationsDTO {
    @NotEmpty(message = "User IDs list cannot be empty")
    private List<String> userIds;

    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
}
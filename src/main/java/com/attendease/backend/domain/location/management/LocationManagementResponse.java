package com.attendease.backend.domain.location.management;

import com.attendease.backend.domain.enums.location.LocationEnvironment;
import com.attendease.backend.domain.enums.location.LocationPurpose;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * {@code LocationManagementResponse} is a final object used for responses
 * for creating, reading, and updating locations in locations management.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class LocationManagementResponse {

    private String locationId;
    private String locationName;
    private String description;
    private LocationEnvironment locationEnvironment;
    private LocationPurpose locationPurposeType;
    private String geometryType;
    private Double latitude;
    private Double longitude;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

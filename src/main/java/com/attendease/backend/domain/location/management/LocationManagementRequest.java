package com.attendease.backend.domain.location.management;

import com.attendease.backend.domain.location.geometry.LocationGeometry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code LocationManagementRequest} is a final object used
 * in creating or updating requests in locations management.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class LocationManagementRequest {

    @NotBlank(message = "Location name is required and cannot be blank")
    @Size(max = 200, message = "Location name must not exceed 200 characters")
    private String locationName;

    @NotBlank(message = "Location type is required and cannot be blank")
    private String locationType;

    @NotBlank(message = "Location purpose is required and cannot be blank")
    private String locationPurpose;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Geometry data is required")
    private LocationGeometry locationGeometry;
}
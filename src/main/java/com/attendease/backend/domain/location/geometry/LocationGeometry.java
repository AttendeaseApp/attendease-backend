package com.attendease.backend.domain.location.geometry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@code LocationGeometry} is a final object used
 * in mapping converted longitude and latitude to a compatible geo json data.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16, last refactored in 2025-Dec-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class LocationGeometry {

    @NotBlank(message = "Geometry type is required")
    @Builder.Default
    private String type = "Polygon";

    @NotNull(message = "Coordinates are required")
    private List<List<List<Double>>> coordinates;
}

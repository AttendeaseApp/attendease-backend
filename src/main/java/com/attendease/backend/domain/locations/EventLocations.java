package com.attendease.backend.domain.locations;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Domain entity representing physical or virtual event locations with geospatial support.
 * <p>
 * Stores venue details, including GeoJSON polygons for geofencing (e.g., campus buildings). Enables location-based
 * attendance validation during events.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event_locations")
public class EventLocations {

    @Id
    private String locationId;

    @NotBlank
    @Indexed
    private String locationName;

    @NotBlank
    private String locationType;

    private String geometryType;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @Field("geometry")
    private GeoJsonPolygon geometry;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}


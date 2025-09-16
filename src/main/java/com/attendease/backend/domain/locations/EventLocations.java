package com.attendease.backend.domain.locations;

import com.attendease.backend.domain.locations.Geofencing.GeofenceData;
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
 * Class representing event locations with geospatial data.
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

    private GeofenceData geofenceParameters;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}


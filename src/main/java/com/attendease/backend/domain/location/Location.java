package com.attendease.backend.domain.location;

import com.attendease.backend.domain.enums.location.LocationEnvironment;
import com.attendease.backend.domain.enums.location.LocationPurpose;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * {@code Location} is a domain entity representing a geofenced area defined by {@link GeoJsonPolygon}.
 * <p>
 * Each location is classified by its {@link LocationPurpose} to determine if it can be assigned
 * as a gateway for registration or as the main venue for an event. It also tracks the
 * {@link LocationEnvironment} to assist in signal accuracy expectations when creating event.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "location")
public class Location {

    @Id
    private String locationId;

    @NotBlank
    @Indexed
    private String locationName;

    private String description;

    @NotNull
    private LocationEnvironment environment;

    @NotNull
    private LocationPurpose purpose;

    @NotNull
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @Field("geometry")
    private GeoJsonPolygon locationGeometry;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
package com.attendease.backend.domain.event;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.location.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Domain entity representing a scheduled event session.
 * <p>
 * Captures event details like timing, locations (separate for registration and venue), and eligibility rules.
 * <p>
 * Supports states (e.g., UPCOMING, ONGOING) and geofencing verification per purpose.
 * <p>
 * {@link EventEligibility} defines target students. Post-event, triggers attendance finalization.
 * <p>
 * This also supports a dual-location model to facilitate precise attendance tracking:
 * <ul>
 * <li><b>Registration Location:</b> The specific area where students must physically
 * be present to register for the event.
 * </li>
 * <li><b>Venue Location:</b> The actual space where the event takes place.
 * This is used for ongoing location monitoring to ensure students remain
 * within the geofence during the session.</li>
 * </ul>
 * <p>
 *
 * @see com.attendease.backend.domain.enums.location.LocationEnvironment
 * @see com.attendease.backend.domain.enums.location.LocationPurpose
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-22 (Updated for dual locations)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "event")
public class Event {

    @Id
    private String eventId;

    @NotBlank
    @Indexed
    private String eventName;

    @DBRef
    @NotNull
    private Location registrationLocation;

    @Field("registrationLocationId")
    @Indexed
    @NotBlank
    private String registrationLocationId;

    @Field("registrationLocationName")
    @NotBlank
    private String registrationLocationName;

    @DBRef
    @NotNull
    private Location venueLocation;

    @Field("venueLocationId")
    @Indexed
    @NotBlank
    private String venueLocationId;

    @Field("venueLocationName")
    @NotBlank
    private String venueLocationName;

    @NotNull
    private String description;

    @Indexed
    @NotNull
    @Field("eligibleStudents")
    private EventEligibility eligibleStudents;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationDateTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startingDateTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endingDateTime;

    @NotNull
    private EventStatus eventStatus;

    @Field("facialVerificationEnabled")
    @Builder.Default
    private Boolean facialVerificationEnabled = false;

    @Field("attendanceLocationMonitoringEnabled")
    @Builder.Default
    private Boolean attendanceLocationMonitoringEnabled = false;

    // TODO: IMPLEMENT THIS, STILL HAS DEPENDENCIES
    private String academicYear;
    private String semester;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;
}
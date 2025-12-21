package com.attendease.backend.domain.event;

import com.attendease.backend.domain.enums.EventStatus;
import com.attendease.backend.domain.event.eligibility.EventEligibility;
import com.attendease.backend.domain.enums.location.LocationPurpose;
import com.attendease.backend.domain.location.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * Supports states (e.g., UPCOMING, ONGOING) and geofencing verification per purpose.
 * {@link EventEligibility} defines target students. Post-event, triggers attendance finalization.
 *
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

    @DBRef
    @NotNull
    private Location venueLocation;

    @Field("venueLocationId")
    @Indexed
    @NotBlank
    private String venueLocationId;

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

    @Field("isFacialVerificationEnabled")
    @Builder.Default
    private boolean isFacialVerificationEnabled = false;

    @Field("isAttendanceLocationMonitoringEnabled")
    @Builder.Default
    private boolean isAttendanceLocationMonitoringEnabled = false;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModified;

    /**
     * Validates that the registration location has purpose REGISTRATION_AREA
     * and the venue location has purpose EVENT_VENUE.
     * Throws IllegalArgumentException if mismatched.
     */
    public void validateLocationPurposes() {
        if (registrationLocation != null && !LocationPurpose.REGISTRATION_AREA.equals(registrationLocation.getPurpose())) {
            throw new IllegalArgumentException("Registration location must have purpose REGISTRATION_AREA (current: " + registrationLocation.getPurpose() + ")");
        }
        if (venueLocation != null && !LocationPurpose.EVENT_VENUE.equals(venueLocation.getPurpose())) {
            throw new IllegalArgumentException("Venue location must have purpose EVENT_VENUE (current: " + venueLocation.getPurpose() + ")");
        }
    }
}

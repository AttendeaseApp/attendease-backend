package com.attendease.backend.model.locations;

import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

@Data
public class EventLocations {

    @DocumentId
    private String locationId;
    private String locationName;
    private String locationType;
    private GeoPoint geomPoints;
}

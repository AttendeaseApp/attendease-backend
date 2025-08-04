package com.attendease.attendease_backend.data.locations;

import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

@Data
public class Location {

    @DocumentId
    private String locationId;
    private String locationName;
    private String locationType;
    private GeoPoint geomPoints;
}

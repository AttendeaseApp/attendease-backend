package com.attendease.backend.model.locations;

import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;

import java.util.List;

@Data
public class EventLocations {

    @DocumentId
    private String locationId;
    private String locationName;
    private String locationType;
    private String geometryType;
    private GeoPoint geomPoints;

    private List<GeoPoint> polygonCoordinates;
    private List<Integer> ringBreaks;
    private GeofenceData geofenceData;
}

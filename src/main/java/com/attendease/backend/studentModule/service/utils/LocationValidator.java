package com.attendease.backend.studentModule.service.utils;

import com.attendease.backend.domain.locations.EventLocations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LocationValidator {

    public boolean isWithinLocationBoundary(EventLocations location, double latitude, double longitude) {
        GeoJsonPolygon polygon = location.getGeometry();
        if (polygon == null) {
            log.warn("No polygon geometry found for location: {}, {}", location.getLocationId(), location.getLocationName());
            return false;
        }

        List<GeoJsonLineString> lineStrings = polygon.getCoordinates();
        if (lineStrings.isEmpty()) {
            log.warn("No coordinates found in polygon for location: {}, {}", location.getLocationId(), location.getLocationName());
            return false;
        }

        GeoJsonLineString outerRing = lineStrings.getFirst();
        if (outerRing == null) {
            log.warn("No outer ring found in polygon for location: {}, {}", location.getLocationId(), location.getLocationName());
            return false;
        }

        List<Point> points = outerRing.getCoordinates();
        boolean isInside = isPointInPolygon(latitude, longitude, points);

        log.info("Student location check: [{}, {}] is {} the polygon boundary for location: {}, {}", latitude, longitude, isInside ? "INSIDE" : "OUTSIDE", location.getLocationId(), location.getLocationName());

        return isInside;
    }

    private boolean isPointInPolygon(double latitude, double longitude, List<Point> polygonPoints) {
        int n = polygonPoints.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygonPoints.get(i);
            Point pj = polygonPoints.get(j);

            double xi = pi.getX(); //longitude
            double yi = pi.getY(); //latitude
            double xj = pj.getX(); //longitude
            double yj = pj.getY(); //latitude

            if (((yi > latitude) != (yj > latitude)) && (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi)) {
                inside = !inside;
            }
        }
        return inside;
    }
}

package com.attendease.backend.student.service.utils;

import com.attendease.backend.domain.location.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Utility class for validating geographical locations and checking if a given point
 * lies within a defined polygon boundary of an {@link Location} object.
 * <p>
 * This is primarily used to verify if a student's current location is within the
 * allowed event or campus boundaries.
 * </p>
 */
@Slf4j
@Component
public class LocationValidator {

    /**
     * Checks if a given latitude and longitude lies within the polygon boundary of an event location.
     *
     * @param location the {@link Location} object containing the polygon geometry
     * @param latitude the latitude of the point to check
     * @param longitude the longitude of the point to check
     * @return {@code true} if the point lies within the polygon boundary, {@code false} otherwise
     */
    public boolean isWithinLocationBoundary(Location location, double latitude, double longitude) {
        GeoJsonPolygon polygon = location.getLocationGeometry();
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

    /**
     * Implements the ray-casting algorithm to determine if a point is inside a polygon.
     *
     * @param latitude the latitude of the point to check
     * @param longitude the longitude of the point to check
     * @param polygonPoints the list of {@link Point} objects defining the polygon vertices
     * @return {@code true} if the point is inside the polygon, {@code false} otherwise
     */
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

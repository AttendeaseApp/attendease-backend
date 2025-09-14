package com.attendease.backend.model.locations.Geofencing;

import lombok.Data;

import java.util.List;

@Data
public class Geometry {
    private String type;
    private List<List<List<Double>>> coordinates;
}

package com.attendease.backend.eventLocationManagement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeometryDTO {
    private String type;
    private List<List<List<Double>>> coordinates;
    private List<Double> pointCoordinates;
}

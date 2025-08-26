package com.attendease.backend.eventLocationManagement.dto.request;

import com.attendease.backend.eventLocationManagement.dto.GeoJsonFeatureDTO;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoJsonRequestDTO {
    private String type;
    private List<GeoJsonFeatureDTO> features;
}

package com.attendease.backend.attendanceTrackingService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationInfoDto {
    private boolean presentAtLocation;
    private Date lastExitTime;
    private Date lastReturnTime;
}

package com.attendease.backend.domain.locations.Response;

import lombok.Data;

@Data
public class CheckCurrentLocationResponse {
    private boolean isInside;
    private String message;
}

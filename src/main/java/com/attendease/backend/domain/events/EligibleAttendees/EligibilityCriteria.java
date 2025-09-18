package com.attendease.backend.domain.events.EligibleAttendees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityCriteria {
    private boolean allStudents;
    private List<String> course;
    private List<String> sections;
}

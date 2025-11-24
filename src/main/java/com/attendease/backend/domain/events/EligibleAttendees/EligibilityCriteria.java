package com.attendease.backend.domain.events.EligibleAttendees;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibilityCriteria {

    private boolean allStudents;
    private List<String> cluster;
    private List<String> course;
    private List<String> sections;
}

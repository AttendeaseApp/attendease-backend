package com.attendease.backend.domain.event.eligibility;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public final class EventEligibility {

    private boolean allStudents;

    // what user actually selected when creating and updating (use this for eligibility checks)
    private List<String> selectedClusters;
    private List<String> selectedCourses;
    private List<String> selectedSections;
    private List<Integer> targetYearLevels;

    // auto-populated for reference only (do not use for eligibility)
    private List<String> clusters;
    private List<String> clusterNames;
    private List<String> courses;
    private List<String> courseNames;
    private List<String> sections;
    private List<String> sectionNames;
}

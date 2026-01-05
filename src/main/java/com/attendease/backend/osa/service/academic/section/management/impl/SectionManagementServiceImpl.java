package com.attendease.backend.osa.service.academic.section.management.impl;

import com.attendease.backend.domain.course.Course;
import com.attendease.backend.domain.enums.academic.Semester;
import com.attendease.backend.domain.section.Section;
import com.attendease.backend.domain.section.management.SectionResponse;
import com.attendease.backend.domain.section.management.BulkSectionRequest;
import com.attendease.backend.domain.section.management.BulkSectionResult;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;
import com.attendease.backend.osa.service.academic.year.management.AcademicYearManagementService;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.section.SectionRepository;
import com.attendease.backend.repository.students.StudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing academic sections.
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Dec-24
 */
@Service
@RequiredArgsConstructor
public final class SectionManagementServiceImpl implements SectionManagementService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentsRepository;

    private final AcademicYearManagementService academicYearManagementService;

    /*
     * these values are for fallback or default :)
     */
    @Value("${academic.year-level.min:1}")
    private Integer minYearLevel;

    @Value("${academic.year-level.max:4}")
    private Integer maxYearLevel;


    @Override
    @Transactional
    public SectionResponse addNewSection(String courseId, Section section) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        String newSectionName = section.getSectionName().trim();

        if (newSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }
        if (sectionRepository.findBySectionName(newSectionName).isPresent()) {
            throw new IllegalArgumentException("Section with name '" + newSectionName + "' already exists");
        }

        validateBasicSectionFormat(newSectionName, course.getCourseName());

        section.setSectionName(newSectionName);
        section.setCourse(course);

        if (section.getYearLevel() == null) {
            throw new IllegalArgumentException(
                    "Year level must be specified. Section name '" + newSectionName + "' does not follow standard format for auto-detection."
            );
        }
        if (section.getSemester() == null) {
            throw new IllegalArgumentException(
                    "Semester must be specified. Section name '" + newSectionName + "' does not follow standard format for auto-detection."
            );
        }
        validateYearLevelAndSemester(section.getYearLevel(), section.getSemester());

        Section savedSection = sectionRepository.save(section);
        return SectionResponse.fromEntity(savedSection);
    }

    @Transactional
    @Override
    public BulkSectionResult addSectionsBulk(String courseId, List<BulkSectionRequest> requests) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        List<SectionResponse> successful = new ArrayList<>();
        List<BulkSectionResult.BulkSectionError> errors = new ArrayList<>();

        for (int index = 0; index < requests.size(); index++) {
            BulkSectionRequest request = requests.get(index);

			try {
                String sectionName = request.getSectionName().trim();

                if (sectionName.isEmpty()) {
                    errors.add(new BulkSectionResult.BulkSectionError(index,sectionName,"Section name cannot be blank"));
                    continue;
                }
                if (sectionRepository.findBySectionName(sectionName).isPresent()) {
                    errors.add(new BulkSectionResult.BulkSectionError(index,sectionName,"Section with name '" + sectionName + "' already exists"));
                    continue;
                }
                validateBasicSectionFormat(sectionName, course.getCourseName());

                if (request.getYearLevel() == null) {
                    errors.add(new BulkSectionResult.BulkSectionError(index,sectionName, "Year level must be specified"));
                    continue;
                }
                if (request.getSemester() == null) {
                    errors.add(new BulkSectionResult.BulkSectionError(index,sectionName,"Semester must be specified"));
                    continue;
                }
                validateYearLevelAndSemester(request.getYearLevel(), request.getSemester());

                Section section = Section.builder()
                        .sectionName(sectionName)
                        .course(course)
                        .yearLevel(request.getYearLevel())
                        .semester(request.getSemester())
                        .build();

                Section savedSection = sectionRepository.save(section);
                successful.add(SectionResponse.fromEntity(savedSection));

            } catch (IllegalArgumentException e) {
                errors.add(new BulkSectionResult.BulkSectionError(index,request.getSectionName(),e.getMessage()));
            } catch (Exception e) {
                errors.add(new BulkSectionResult.BulkSectionError(index,request.getSectionName(),"Unexpected error: " + e.getMessage()));
            }
        }
        return BulkSectionResult.builder()
                .successful(successful)
                .errors(errors)
                .totalProcessed(requests.size())
                .successCount(successful.size())
                .errorCount(errors.size())
                .build();
    }


    @Transactional
    @Override
    public SectionResponse activateSection(String sectionId) {

        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new RuntimeException("Section not found."));

        if (Boolean.TRUE.equals(section.getIsActive())) {
            throw new IllegalStateException(
                    "Section '" + section.getSectionName() + "' is already active."
            );
        }

        if (section.getSemester() == null) {
            throw new IllegalArgumentException(
                    "Section '" + section.getSectionName() + "' does not have a semester set."
            );
        }

        final Integer currentSemester = getCurrentSemsterInteger(section);

        List<Section> activeSections = sectionRepository.findByCourseAndSemesterAndIsActive(
                section.getCourse(), currentSemester, true
        );
        for (Section active : activeSections) {
            active.setIsActive(false);
            sectionRepository.save(active);
        }

        section.setIsActive(true);
        Section savedSection = sectionRepository.save(section);
        return SectionResponse.fromEntity(savedSection);
    }

    @Override
    public List<SectionResponse> getSectionsByCourse(String courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        List<Section> sections = sectionRepository.findByCourse(course);
        return sections.stream().map(SectionResponse::fromEntity).collect(Collectors.toList());
    }


    @Override
    public List<SectionResponse> getSectionsByYearLevel(Integer yearLevel) {
        List<Section> sections = sectionRepository.findByYearLevel(yearLevel);
        return sections.stream().map(SectionResponse::fromEntity).collect(Collectors.toList());
    }


    @Override
    public List<SectionResponse> getSectionsBySemester(Integer semester) {
        List<Section> sections = sectionRepository.findBySemester(semester);
        return sections.stream().map(SectionResponse::fromEntity).collect(Collectors.toList());
    }


    @Override
    public List<SectionResponse> getSectionsByYearLevelAndSemester(Integer yearLevel, Integer semester) {
        List<Section> sections = sectionRepository.findByYearLevelAndSemester(yearLevel, semester);
        return sections.stream().map(SectionResponse::fromEntity).collect(Collectors.toList());
    }


    @Override
    public List<SectionResponse> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return sections.stream().map(SectionResponse::fromEntity).collect(Collectors.toList());
    }


    @Override
    public SectionResponse getSectionById(String id) {
        Section section = sectionRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
        return SectionResponse.fromEntity(section);
    }


    @Override
    public Optional<SectionResponse> getSectionByFullName(String fullName) {
        return sectionRepository.findBySectionName(fullName).map(SectionResponse::fromEntity);
    }


    @Override
    @Transactional
    public SectionResponse updateSection(String id, Section updatedSection) {

        Section existing = sectionRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
        String updatedSectionName = updatedSection.getSectionName().trim();

        if (updatedSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (existing.getSectionName().equals(updatedSectionName)) {
            return SectionResponse.fromEntity(existing);
        }

        sectionRepository.findBySectionName(updatedSectionName).ifPresent(s -> {
            if (!s.getId().equals(existing.getId())) {
                throw new IllegalArgumentException("A section with the name '" + updatedSectionName + "' already exists");
            }
        });

        validateBasicSectionFormat(updatedSectionName, existing.getCourse().getCourseName());
        existing.setSectionName(updatedSectionName);

        if (updatedSection.getYearLevel() != null && updatedSection.getSemester() != null) {
            validateYearLevelAndSemester(updatedSection.getYearLevel(), updatedSection.getSemester());
            existing.setYearLevel(updatedSection.getYearLevel());
            existing.setSemester(updatedSection.getSemester());
        } else {
            if (existing.getYearLevel() == null) {
                throw new IllegalArgumentException(
                        "Year level must be specified. Section name '" + updatedSectionName + "' does not follow standard format."
                );
            }
            if (existing.getSemester() == null) {
                throw new IllegalArgumentException(
                        "Semester must be specified. Section name '" + updatedSectionName + "' does not follow standard format."
                );
            }
        }

        Section savedSection = sectionRepository.save(existing);
        return SectionResponse.fromEntity(savedSection);
    }


    @Override
    @Transactional
    public void deleteSection(String id) {

        Section section = sectionRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));

        if (Boolean.TRUE.equals(section.getIsActive())) {
            throw new IllegalStateException(
                    "Cannot delete section '" + section.getSectionName() + "' because it is currently active."
            );
        }

        Long studentCount = studentsRepository.countBySection(section);
        Long eventCountById = eventRepository.countByEligibleStudentsSectionsContaining(section.getId());
        Long eventCountByName = eventRepository.countByEligibleStudentsSectionNamesContaining(section.getSectionName());
        Long totalEventCount = eventCountById + eventCountByName;

        if (studentCount > 0 || totalEventCount > 0) {
            String sectionName = section.getSectionName();
            StringBuilder message = new StringBuilder(
                    "Cannot delete section '" + sectionName + "' due to existing dependencies ("
            ).append(studentCount).append(" student");

            if (studentCount != 1) {
                message.append("s");
            }

            if (totalEventCount > 0) {
                message.append(", ").append(totalEventCount).append(" event");
                if (totalEventCount != 1) {
                    message.append("s");
                }
                message.append(")");
            } else {
                message.append(")");
            }

            message.append(". Reassign or remove dependencies first.");
            throw new IllegalStateException(message.toString());
        }

        sectionRepository.deleteById(id);
    }


    @Override
    @Transactional
    public void updateSectionsForCourseNameChange(String courseId, String newCourseName) {

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        List<Section> sections = sectionRepository.findByCourse(course);
        String oldCourseName = course.getCourseName();

        for (Section section : sections) {
            String oldSectionName = section.getSectionName();

            try {
                String sectionNumber = extractSectionNumberIfExists(oldSectionName, oldCourseName);
                String newSectionName = newCourseName + "-" + sectionNumber;
                section.setSectionName(newSectionName);
                sectionRepository.save(section);
            } catch (IllegalArgumentException e) {
                if (oldSectionName.startsWith(oldCourseName)) {
                    String remainder = oldSectionName.substring(oldCourseName.length());
                    String newSectionName = newCourseName + remainder;
                    section.setSectionName(newSectionName);
                    sectionRepository.save(section);
                }
            }
        }
    }

    /*
     * PRIVATE HELPERS
     */

    private void validateBasicSectionFormat(String sectionName, String courseName) {
        if (!sectionName.startsWith(courseName)) {
            throw new IllegalArgumentException(
                    "Section name must start with course name '" + courseName + "'. " + "Example: " + courseName + "-101 or " + courseName + "-A"
            );
        }

        if (sectionName.length() <= courseName.length()) {
            throw new IllegalArgumentException(
                    "Section name must have an identifier after the course name. " + "Example: " + courseName + "-101 or " + courseName + "-A"
            );
        }
    }


    private String extractSectionNumberIfExists(String fullSectionName, String courseName) {
        String expectedPrefix = courseName + "-";
        if (!fullSectionName.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "Section name '" + fullSectionName + "' does not follow standard format"
            );
        }
        return fullSectionName.substring(expectedPrefix.length());
    }


    private void validateYearLevelAndSemester(Integer yearLevel, Integer semester) {
        if (yearLevel == null || yearLevel < minYearLevel || yearLevel > maxYearLevel) {
            throw new IllegalArgumentException(
                    "Year level must be between " + minYearLevel + " and " + maxYearLevel + ". Provided: " + yearLevel
            );
        }
        if (semester == null) {
            throw new IllegalArgumentException("Semester must be specified.");
        }

        boolean validSemester = false;
        for (Semester s : Semester.values()) {
            if (s.getNumber() == semester) {
                validSemester = true;
                break;
            }
        }

        if (!validSemester) {
            throw new IllegalArgumentException(
                    "Semester " + semester + " does not exist. Valid values are: " + java.util.Arrays.toString(Semester.values())
            );
        }
    }


    @Nonnull
    private Integer getCurrentSemsterInteger(Section section) {
        Integer currentSemester = academicYearManagementService.getCurrentSemester();
        if (currentSemester == null) {
            throw new IllegalStateException("No active academic year found. Cannot determine current semester.");
        }

        if (!section.getSemester().equals(currentSemester)) {
            throw new IllegalArgumentException(
                    "Cannot activate section '" + section.getSectionName() +
                            "' because its semester (" + section.getSemester() +
                            ") does not match the current semester (" + currentSemester + ")."
            );
        }
        return currentSemester;
    }

}
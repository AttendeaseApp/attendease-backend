package com.attendease.backend.osa.service.academic.section.management.impl;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public final class SectionManagementServiceImpl implements SectionManagementService {

    private final CourseRepository courseRepository;
    private final SectionsRepository sectionsRepository;
    private final EventRepository eventRepository;
    private final StudentRepository studentsRepository;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public Sections createNewSection(String courseId, Sections section) {
        Courses course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        String newSectionName = section.getSectionName().trim();

        if (newSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (sectionsRepository.findBySectionName(newSectionName).isPresent()) {
            throw new IllegalArgumentException("Section with name '" + newSectionName + "' already exists");
        }

        validateFullSectionName(newSectionName, course.getCourseName());
        section.setSectionName(newSectionName);
        section.setCourse(course);
        return sectionsRepository.save(section);
    }

    @Override
    public List<Sections> getSectionsByCourse(String courseId) {
        Courses course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        return sectionsRepository.findByCourse(course);
    }

    @Override
    public List<Sections> getAllSections() {
        return sectionsRepository.findAll();
    }

    @Override
    public Sections getSectionById(String id) {
        return sectionsRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
    }

    @Override
    public Optional<Sections> getSectionByFullName(String fullName) {
        userValidator.validateFullCourseSectionFormat(fullName);
        return sectionsRepository.findBySectionName(fullName);
    }

    @Override
    @Transactional
    public Sections updateSection(String id, Sections updatedSection) {
        Sections existing = getSectionById(id);
        String updatedSectionName = updatedSection.getSectionName().trim();

        if (updatedSectionName.isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be blank");
        }

        if (existing.getSectionName().equals(updatedSectionName)) {
            return existing;
        }

        sectionsRepository.findBySectionName(updatedSectionName).ifPresent(s -> {
            if (!s.getId().equals(existing.getId())) {
                throw new IllegalArgumentException("A section with the name '" + updatedSectionName + "' already exists. " + "Each section name must be unique.");
            }
        });

        validateFullSectionName(updatedSectionName, existing.getCourse().getCourseName());
        existing.setSectionName(updatedSectionName);
        return sectionsRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteSection(String id) {
        Sections section = getSectionById(id);
        Long studentCount = studentsRepository.countBySection(section);
        Long eventCountById = eventRepository.countByEligibleStudentsSectionsContaining(section.getId());
        Long eventCountByName = eventRepository.countByEligibleStudentsSectionNamesContaining(section.getSectionName());
        Long totalEventCount = eventCountById + eventCountByName;

        if (studentCount > 0 || eventCountById > 0 || eventCountByName > 0) {
            String sectionName = section.getSectionName();
            StringBuilder message = new StringBuilder("You cannot delete section '" + sectionName + "' due to existing dependencies (").append(studentCount).append(" student");
            if (eventCountById > 0 || eventCountByName > 0) {
                message.append(", ").append(totalEventCount).append(" event sessions (").append(eventCountById).append(" by ID, ").append(eventCountByName).append(" by name; possible overlap)").append(")");
            } else {
                message.append(")");
            }
            message.append(". This action is prevented to protect data integrity and avoid orphaned references. ").append("Reassign or remove dependencies first (e.g., re-enroll student or update event eligibility criteria).");
            throw new IllegalStateException(message.toString());
        }
        sectionsRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void createDefaultSections(Courses course) {
        List<String> defaultSectionNumbers = Arrays.asList("101", "201", "301", "401", "501", "601", "701", "801");
        String coursePrefix = course.getCourseName() + "-";
        for (String sectionNumber : defaultSectionNumbers) {
            String fullSectionName = coursePrefix + sectionNumber;
            if (sectionsRepository.findBySectionName(fullSectionName).isEmpty()) {
                Sections defaultSection = Sections.builder().sectionName(fullSectionName).course(course).build();
                createNewSection(course.getId(), defaultSection);
            }
        }
    }

    @Override
    @Transactional
    public void updateSectionsForCourseNameChange(String courseId, String newCourseName) {
        Courses course = courseRepository.findById(courseId).orElseThrow();
        List<Sections> sections = getSectionsByCourse(courseId);
        String newPrefix = newCourseName + "-";
        for (Sections section : sections) {
            String oldNumber = section.getSectionName().substring(course.getCourseName().length() + 1);
            section.setSectionName(newPrefix + oldNumber);
            sectionsRepository.save(section);
        }
    }

    /*
    * PRIVATE HELPERS
    */

    private void validateFullSectionName(String fullSectionName, String courseName) {
        userValidator.validateFullCourseSectionFormat(fullSectionName);
        final String sectionNumber = getString(fullSectionName, courseName);
        if (!Arrays.asList("101", "201", "301", "401", "501", "601", "701", "801").contains(sectionNumber)) {
            throw new IllegalArgumentException(
                    "Invalid section number. Allowed section numbers are: 101, 201, 301, 401, 501, 601, 701, 801."
            );
        }
    }

    private String getString(String fullSectionName, String courseName) {
        String expectedPrefix = courseName + "-";
        if (!fullSectionName.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "Section name '" + fullSectionName + "' does not belong to course '" + courseName + "'. " +
                            "All section names for this course must start with '" + expectedPrefix + "'. " +
                            "Example: " + expectedPrefix + "101"
            );
        }
        return fullSectionName.substring(expectedPrefix.length());
    }
}
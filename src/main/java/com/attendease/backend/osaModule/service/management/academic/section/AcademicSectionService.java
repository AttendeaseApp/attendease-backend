package com.attendease.backend.osaModule.service.management.academic.section;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.repository.course.CourseRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcademicSectionService {

    private final CourseRepository courseRepository;
    private final SectionsRepository sectionsRepository;

    public Sections createSection(String courseId, Sections section) {
        Courses course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        validateFullSectionName(section.getName(), course.getCourseName());
        section.setCourse(course);
        return sectionsRepository.save(section);
    }

    public List<Sections> getSectionsByCourse(String courseId) {
        Courses course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found."));
        return sectionsRepository.findByCourse(course);
    }

    public List<Sections> getAllSections() {
        return sectionsRepository.findAll();
    }

    public Sections getSectionById(String id) {
        return sectionsRepository.findById(id).orElseThrow(() -> new RuntimeException("Section not found."));
    }

    public Optional<Sections> getSectionByFullName(String fullName) {
        validateFullCourseSectionFormat(fullName);
        return sectionsRepository.findByName(fullName);
    }

    public Sections updateSection(String id, Sections updatedSection) {
        Sections existing = getSectionById(id);
        validateFullSectionName(updatedSection.getName(), existing.getCourse().getCourseName());
        existing.setName(updatedSection.getName());
        return sectionsRepository.save(existing);
    }

    public void deleteSection(String id) {
        sectionsRepository.deleteById(id);
    }

    public void createDefaultSections(Courses course) {
        List<String> defaultSectionNumbers = Arrays.asList("101", "201", "301", "401", "501", "601", "701", "801");
        String coursePrefix = course.getCourseName() + "-";
        for (String sectionNumber : defaultSectionNumbers) {
            String fullSectionName = coursePrefix + sectionNumber;
            if (sectionsRepository.findByName(fullSectionName).isEmpty()) {
                Sections defaultSection = Sections.builder().name(fullSectionName).course(course).build();
                createSection(course.getId(), defaultSection);
            }
        }
    }

    public void deleteSectionsByCourse(String courseId) {
        List<Sections> sections = getSectionsByCourse(courseId);
        sectionsRepository.deleteAll(sections);
    }

    public void updateSectionsForCourseNameChange(String courseId, String newCourseName) {
        Courses course = courseRepository.findById(courseId).orElseThrow();
        List<Sections> sections = getSectionsByCourse(courseId);
        String newPrefix = newCourseName + "-";
        for (Sections section : sections) {
            String oldNumber = section.getName().substring(course.getCourseName().length() + 1);
            section.setName(newPrefix + oldNumber);
            sectionsRepository.save(section);
        }
    }

    public void validateFullCourseSectionFormat(String fullIdentifier) {
        if (fullIdentifier == null || !fullIdentifier.matches("^[A-Z0-9]+-[0-9]{3}$")) {
            throw new IllegalArgumentException("Invalid format. Expected: COURSE_NAME-SECTION_NUMBER (e.g., BSECE-101). " + "COURSE_NAME: uppercase letters/digits; SECTION_NUMBER: exactly 3 digits.");
        }
    }

    private void validateFullSectionName(String fullSectionName, String courseName) {
        validateFullCourseSectionFormat(fullSectionName);
        String expectedPrefix = courseName + "-";
        if (!fullSectionName.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("Section name must start with '" + expectedPrefix + "' (e.g., '" + expectedPrefix + "101'). Mismatch detected.");
        }
        String sectionNumber = fullSectionName.substring(expectedPrefix.length());
        if (!Arrays.asList("101", "201", "301", "401", "501", "601", "701", "801").contains(sectionNumber)) {
            throw new IllegalArgumentException("Section number must be one of: 101,201,301,401,501,601,701,801.");
        }
    }
}

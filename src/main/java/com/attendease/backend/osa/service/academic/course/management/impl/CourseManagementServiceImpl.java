package com.attendease.backend.osa.service.academic.course.management.impl;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.osa.service.academic.course.management.CourseManagementService;
import com.attendease.backend.osa.service.academic.section.management.SectionManagementService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import java.util.List;

import com.attendease.backend.repository.event.EventRepository;
import com.attendease.backend.repository.sections.SectionsRepository;
import com.attendease.backend.repository.students.StudentRepository;
import com.attendease.backend.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public final class CourseManagementServiceImpl implements CourseManagementService {

    private final SectionManagementService sectionManagementService;
    private final CourseRepository courseRepository;
    private final ClustersRepository clusterRepository;
    private final SectionsRepository sectionsRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public Courses createCourse(String clusterId, Courses course) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));

        String courseName = course.getCourseName().trim();
        if (courseName.isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }

        userValidator.validateCourseNameFormat(courseName);

        if (courseRepository.findByCourseName   (courseName).isPresent()) {
            throw new IllegalArgumentException("Course name '" + courseName + "' already exists.");
        }

        course.setCourseName(courseName);
        course.setCluster(cluster);
        Courses savedCourse = courseRepository.save(course);
        sectionManagementService.createDefaultSections(savedCourse.getId());
        return savedCourse;
    }

    @Override
    public List<Courses> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Courses getCourseById(String id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found."));
    }

    @Override
    public List<Courses> getCoursesByCluster(String clusterId) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));
        return courseRepository.findByCluster(cluster);
    }

    @Override
    @Transactional
    public Courses updateCourse(String id, Courses updatedCourse) {
        Courses existing = getCourseById(id);
        String newCourseName = updatedCourse.getCourseName().trim();

        if (newCourseName.isEmpty()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }

        userValidator.validateCourseNameFormat(newCourseName);

        courseRepository.findByCourseName(newCourseName).filter(c -> !c.getId().equals(id)).ifPresent(c -> {
                    throw new IllegalArgumentException("Course name '" + newCourseName + "' already exists.");});

        existing.setCourseName(newCourseName);
        sectionManagementService.updateSectionsForCourseNameChange(existing.getId(), newCourseName);
        return courseRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteCourse(String id) {
        Courses course = getCourseById(id);
        long eventCountById = eventRepository.countByEligibleStudentsCoursesContaining(course.getId());
        long eventCountByName = eventRepository.countByEligibleStudentsCourseNamesContaining(course.getCourseName());
        long totalEventCount = eventCountById + eventCountByName;

        long totalStudentCount = 0;
        long sectionCount = sectionsRepository.countByCourse(course);
        if (sectionCount > 0) {
            List<Sections> sections = sectionsRepository.findByCourse(course);
            for (Sections section : sections) {
                totalStudentCount += studentRepository.countBySection(section);
            }
        }

        if (eventCountById > 0 || eventCountByName > 0) {
            String courseName = course.getCourseName();
            String message = "Cannot delete course '" + courseName +
                    "' as it is in use by " + totalEventCount + " event sessions (" +
                    eventCountById + " by ID, " + eventCountByName + " by name; possible overlap)" +
                    ") across " + sectionCount + " sections, potentially impacting " +
                    totalStudentCount + " enrolled student overall (via section eligibility). " +
                    "These events may restrict access for those student. To proceed, update event eligibility criteria first. This prevents data inconsistencies.";
            throw new IllegalStateException(message);
        }
        if (sectionCount > 0) {
            List<Sections> sections = sectionsRepository.findByCourse(course);
            for (Sections section : sections) {
                sectionManagementService.deleteSection(section.getId());
            }
        }
        courseRepository.deleteById(id);
    }
}

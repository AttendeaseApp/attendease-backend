package com.attendease.backend.osaModule.service.management.academic.course;

import com.attendease.backend.domain.clusters.Clusters;
import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.osaModule.service.management.academic.section.AcademicSectionService;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcademicCourseService {

    private final AcademicSectionService academicSectionService;
    private final CourseRepository courseRepository;
    private final ClustersRepository clusterRepository;

    public Courses createCourse(String clusterId, Courses course) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));

        if (courseRepository.findByCourseNameAndCluster(course.getCourseName(), cluster).isPresent()) {
            throw new IllegalArgumentException("Course name '" + course.getCourseName() + "' already exists in this cluster.");
        }

        course.setCluster(cluster);
        Courses savedCourse = courseRepository.save(course);
        academicSectionService.createDefaultSections(savedCourse);
        return savedCourse;
    }

    public List<Courses> getAllCourses() {
        return courseRepository.findAll();
    }

    public Courses getCourseById(String id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found."));
    }

    public List<Courses> getCoursesByCluster(String clusterId) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));
        return courseRepository.findByCluster(cluster);
    }

    public Courses updateCourse(String id, Courses updatedCourse) {
        Courses existing = getCourseById(id);
        existing.setCourseName(updatedCourse.getCourseName());
        academicSectionService.updateSectionsForCourseNameChange(existing.getId(), existing.getCourseName());
        return courseRepository.save(existing);
    }

    public void deleteCourse(String id) {
        academicSectionService.deleteSectionsByCourse(id);
        courseRepository.deleteById(id);
    }
}

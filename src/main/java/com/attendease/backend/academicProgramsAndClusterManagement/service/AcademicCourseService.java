package com.attendease.backend.academicProgramsAndClusterManagement.service;

import com.attendease.backend.domain.students.Clusters;
import com.attendease.backend.domain.students.Courses;
import com.attendease.backend.repository.clusters.ClustersRepository;
import com.attendease.backend.repository.course.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicCourseService {

    private final CourseRepository courseRepository;
    private final ClustersRepository clusterRepository;

    public Courses createCourse(String clusterId, Courses course) {
        Clusters cluster = clusterRepository.findById(clusterId).orElseThrow(() -> new RuntimeException("Cluster not found."));
        course.setCluster(cluster);
        return courseRepository.save(course);
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
        return courseRepository.save(existing);
    }

    public void deleteCourse(String id) {
        courseRepository.deleteById(id);
    }
}


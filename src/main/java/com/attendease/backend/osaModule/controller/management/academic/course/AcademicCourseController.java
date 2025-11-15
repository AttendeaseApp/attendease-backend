package com.attendease.backend.osaModule.controller.management.academic.course;

import com.attendease.backend.domain.courses.Courses;
import com.attendease.backend.osaModule.service.management.academic.course.AcademicCourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OSA')")
public class AcademicCourseController {

    private final AcademicCourseService courseService;

    @PostMapping
    public ResponseEntity<Courses> create(@RequestParam String clusterId, @RequestBody @Valid Courses course) {
        return ResponseEntity.ok(courseService.createCourse(clusterId, course));
    }

    @GetMapping
    public ResponseEntity<List<Courses>> getAll() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Courses> getById(@PathVariable String id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/cluster/{clusterId}")
    public ResponseEntity<List<Courses>> getByCluster(@PathVariable String clusterId) {
        return ResponseEntity.ok(courseService.getCoursesByCluster(clusterId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Courses> update(@PathVariable String id, @RequestBody Courses course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}


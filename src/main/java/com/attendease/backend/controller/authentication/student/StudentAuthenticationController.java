package com.attendease.backend.controller.authentication.student;

import com.attendease.backend.data.model.students.Students;
import com.attendease.backend.data.dto.request.StudentLoginRequest;
import com.attendease.backend.services.authentication.student.StudentAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("v1/api/auth/students")
@Slf4j
public class StudentAuthenticationController {

    private final StudentAuthenticationService studentAuthenticationService;

    public StudentAuthenticationController(StudentAuthenticationService studentAuthenticationService) {
        this.studentAuthenticationService = studentAuthenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@Valid @RequestBody Students student) {
        try {
            String result = studentAuthenticationService.registerNewStudentAccount(student);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            log.error("Error registering StudentService: {}", e.getMessage());
            return new ResponseEntity<>("Error registering StudentService: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginStudent(@Valid @RequestBody StudentLoginRequest loginRequest) {
        try {
            Students tempStudent = new Students();
            tempStudent.setStudentNumber(loginRequest.getStudentNumber());
            tempStudent.setPassword(loginRequest.getPassword());

            String customToken = studentAuthenticationService.loginStudent(tempStudent);
            return new ResponseEntity<>(customToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Login attempt failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Internal server error during students login service: {}", e.getMessage());
            return new ResponseEntity<>("Authentication failed due to server error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            log.error("Unexpected error during students login service: {}", e.getMessage());
            return new ResponseEntity<>("An unexpected error occurred during students login service.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

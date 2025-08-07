package com.attendease.attendease_backend.controller.authentication.student;

import com.attendease.attendease_backend.data.student.Student;
import com.attendease.attendease_backend.payload.request.StudentLoginRequest;
import com.attendease.attendease_backend.services.authentication.student.impl.StudentAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("v1/api/auth/student")
@Slf4j
public class StudentAuthenticationController {

    private final StudentAuthenticationService studentLoginService;

    public StudentAuthenticationController(StudentAuthenticationService studentLoginService) {
        this.studentLoginService = studentLoginService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(@Valid @RequestBody Student student) {
        try {
            String result = studentLoginService.registerNewStudentAccount(student);
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
            Student tempStudent = new Student();
            tempStudent.setStudentNumber(loginRequest.getStudentNumber());
            tempStudent.setPassword(loginRequest.getPassword());

            String customToken = studentLoginService.loginStudent(tempStudent);
            return new ResponseEntity<>(customToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Login attempt failed: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Internal server error during student login service: {}", e.getMessage());
            return new ResponseEntity<>("Authentication failed due to server error.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            log.error("Unexpected error during student login service: {}", e.getMessage());
            return new ResponseEntity<>("An unexpected error occurred during student login service.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

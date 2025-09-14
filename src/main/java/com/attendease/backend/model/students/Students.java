package com.attendease.backend.model.students;

import com.attendease.backend.model.biometrics.BiometricData;
import com.attendease.backend.model.users.Users;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Class representing a student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
@Document(collection = "students")
public class Students {

    @Id
    private String id;

    @DBRef
    @NotNull(message = "User reference is required")
    private Users user;

    @DBRef
    private BiometricData facialData;

    @NotBlank(message = "Student number is required")
    @Indexed(unique = true)
    private String studentNumber;

    private String section;

    private String yearLevel;

    @DBRef
    private Courses course;

    @DBRef
    private Clusters cluster;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
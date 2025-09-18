package com.attendease.backend.domain.students;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.users.Users;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Class representing a student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    private String sectionId;
    private String courseId;

    private String cluster;
}
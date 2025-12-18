package com.attendease.backend.domain.students;

import com.attendease.backend.domain.biometrics.BiometricData;
import com.attendease.backend.domain.sections.Sections;
import com.attendease.backend.domain.user.User;
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
 * Domain entity representing a student in the Attendease system.
 * <p>
 * Links user profile, biometrics, and academic section for comprehensive student data.
 * Used as the core entity for attendance, events, and eligibility checks. Unique index
 * on {@code studentNumber} for institutional ID lookups.
 * </p>
 *
 * @author jakematthewviado204@gmail.com
 * @since 2025-Sep-16
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
    private User user;

    @DBRef
    private BiometricData facialData;

    @NotBlank(message = "Student number is required")
    @Indexed(unique = true)
    private String studentNumber;

    @DBRef
    private Sections section;
}

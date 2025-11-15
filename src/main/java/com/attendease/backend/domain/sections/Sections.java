package com.attendease.backend.domain.sections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.attendease.backend.domain.courses.Courses;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "sections")
public class Sections {
    @Id
    private String id;

    private String name;
    private int yearLevel;
    private int semester;

    @DBRef
    private Courses course;
}


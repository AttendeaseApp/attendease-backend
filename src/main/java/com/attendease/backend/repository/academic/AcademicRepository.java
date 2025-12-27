package com.attendease.backend.repository.academic;

import com.attendease.backend.domain.academic.Academic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicRepository extends MongoRepository<Academic, String> {

	Optional<Academic> findByAcademicYearName(String academicYearName);
	Optional<Academic> findByIsActive(boolean isActive);
}

package com.attendease.backend.security;

import com.attendease.backend.model.students.Students;
import com.attendease.backend.repository.students.StudentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentsRepository;

    public CustomUserDetailsService(StudentRepository studentsRepository) {
        this.studentsRepository = studentsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Students student = studentsRepository.findByStudentNumber(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with student number: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(student.getStudentNumber())
                .authorities("ROLE_STUDENT")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}


package com.attendease.backend.repository.students.StudentBiometrics;

import java.util.concurrent.ExecutionException;

public interface StudentBiometrics {
    Long deleteAllStudentsAndAssociatedUserAndFacialData() throws ExecutionException, InterruptedException;
}

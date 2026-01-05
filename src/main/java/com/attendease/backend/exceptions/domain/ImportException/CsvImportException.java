package com.attendease.backend.exceptions.domain.ImportException;

import com.attendease.backend.domain.exception.error.csv.CsvImportErrorResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class CsvImportException extends RuntimeException {
    private final CsvImportErrorResponse errorResponse;

    public CsvImportException(String message, CsvImportErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }

}

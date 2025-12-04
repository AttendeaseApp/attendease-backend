package com.attendease.backend.exceptions.domain.ImportException;

import lombok.Getter;

import java.util.List;

@Getter
public class CsvImportException extends RuntimeException {
    private final List<CsvImportError> errors;

    public CsvImportException(String message, List<CsvImportError> errors) {
        super(message);
        this.errors = errors;
    }

}

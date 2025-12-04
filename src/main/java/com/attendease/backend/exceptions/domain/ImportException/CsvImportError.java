package com.attendease.backend.exceptions.domain.ImportException;

import lombok.Data;

import java.util.List;

@Data
public class CsvImportError {
    private int row;
    private List<String> errors;

    public CsvImportError(int row, List<String> errors) {
        this.row = row;
        this.errors = errors;
    }
}

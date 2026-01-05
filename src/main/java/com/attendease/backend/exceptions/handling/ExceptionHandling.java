package com.attendease.backend.exceptions.handling;

import com.attendease.backend.domain.exception.error.ErrorResponse;
import com.attendease.backend.domain.exception.error.csv.CsvImportErrorResponse;
import com.attendease.backend.domain.exception.validation.ValidationErrorResponse;
import com.attendease.backend.exceptions.domain.Event.*;
import com.attendease.backend.exceptions.domain.ImportException.CsvImportException;
import com.attendease.backend.exceptions.domain.Location.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandling {

    /**
     * Handles general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllGenericExceptions(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "ERROR_OCCURRED",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handles invalid arguments (thrown manually)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                "INVALID_REQUEST",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles invalid state exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
                "ILLEGAL_STATE",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     *  Handles validation errors thrown by @Valid annotated inputs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                errors,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CsvImportException.class)
    public ResponseEntity<CsvImportErrorResponse> handleCsvImportException(CsvImportException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrorResponse());
    }

    /*
    * LOCATION MANAGEMENT RELATED EXCEPTIONS
    */

    @ExceptionHandler(LocationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLocationAlreadyExists(LocationAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(
                "LOCATION_EXISTS",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFound(LocationNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "LOCATION_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(LocationInUseException.class)
    public ResponseEntity<ErrorResponse> handleLocationInUse(LocationInUseException ex) {
        ErrorResponse error = new ErrorResponse(
                "LOCATION_IN_USE",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(LocationHasActiveEventsException.class)
    public ResponseEntity<ErrorResponse> handleLocationHasActiveEvents(LocationHasActiveEventsException ex) {
        ErrorResponse error = new ErrorResponse(
                "LOCATION_HAS_ACTIVE_EVENTS",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidGeometryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidGeometry(InvalidGeometryException ex) {
        ErrorResponse error = new ErrorResponse(
                "INVALID_GEOMETRY",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /*
    * EVENT MANAGEMENT RELATED EXCEPTIONS
    */

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventNotFound(EventNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "EVENT_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EventLocationConflictException.class)
    public ResponseEntity<ErrorResponse> handleEventLocationConflict(EventLocationConflictException ex) {
        ErrorResponse error = new ErrorResponse(
                "EVENT_LOCATION_CONFLICT",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EventStatusException.class)
    public ResponseEntity<ErrorResponse> handleEventStatus(EventStatusException ex) {
        ErrorResponse error = new ErrorResponse(
                "EVENT_STATUS_ERROR",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EventDeletionNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleEventDeletionNotAllowed(EventDeletionNotAllowedException ex) {
        ErrorResponse error = new ErrorResponse(
                "EVENT_DELETION_NOT_ALLOWED",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(EventUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleEventUpdateNotAllowed(EventUpdateNotAllowedException ex) {
        ErrorResponse error = new ErrorResponse(
                "EVENT_UPDATE_NOT_ALLOWED",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(InvalidDateRangeException ex) {
        ErrorResponse error = new ErrorResponse(
                "INVALID_DATE_RANGE",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidEligibilityCriteriaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEligibilityCriteria(InvalidEligibilityCriteriaException ex) {
        ErrorResponse error = new ErrorResponse(
                "INVALID_ELIGIBILITY_CRITERIA",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidLocationPurposeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLocationPurpose(InvalidLocationPurposeException ex) {
        ErrorResponse error = new ErrorResponse(
                "INVALID_LOCATION_PURPOSE",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

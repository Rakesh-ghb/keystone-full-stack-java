package com.key_stone.controller;

import com.key_stone.dto.ApiDtos.ErrorResponse;
import jakarta.servlet.ServletException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------
    // VALIDATION ERROR - 400
    // ---------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(
            MethodArgumentNotValidException e) {

        var errors = new LinkedHashMap<String, String>();

        e.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                400,
                                "Validation failed",
                                errors
                        )
                );
    }

    // ---------------------------------------------------------
    // BAD REQUEST - 400
    // ---------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> badRequest(
            IllegalArgumentException e) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                400,
                                e.getMessage(),
                                new LinkedHashMap<>()
                        )
                );
    }

    // ---------------------------------------------------------
    // CONFLICT - 409
    // ---------------------------------------------------------

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ErrorResponse> conflict(
            IllegalStateException e) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                409,
                                e.getMessage(),
                                new LinkedHashMap<>()
                        )
                );
    }

    // ---------------------------------------------------------
    // FORBIDDEN - 403
    // ---------------------------------------------------------

    @ExceptionHandler(SecurityException.class)
    ResponseEntity<ErrorResponse> forbidden(
            SecurityException e) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                403,
                                e.getMessage(),
                                new LinkedHashMap<>()
                        )
                );
    }

    // ---------------------------------------------------------
    // SERVLET EXCEPTION
    // Handles SecurityException wrapped by Spring MVC
    // ---------------------------------------------------------

    @ExceptionHandler(ServletException.class)
    ResponseEntity<ErrorResponse> servletException(
            ServletException e) {

        Throwable cause = e.getCause();

        if (cause instanceof SecurityException) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            new ErrorResponse(
                                    LocalDateTime.now(),
                                    403,
                                    cause.getMessage(),
                                    new LinkedHashMap<>()
                            )
                    );
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                500,
                                "Unexpected server error",
                                new LinkedHashMap<>()
                        )
                );
    }

    // ---------------------------------------------------------
    // UNEXPECTED ERROR - 500
    // ---------------------------------------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(
            Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse(
                                LocalDateTime.now(),
                                500,
                                "Unexpected server error",
                                new LinkedHashMap<>()
                        )
                );
    }
}
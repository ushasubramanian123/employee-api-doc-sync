package com.example.employeeapidocsync;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final ErrorResponse INVALID_EMPLOYEE_DATA =
            new ErrorResponse("Invalid employee data");

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleInvalidEmployeeData(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_EMPLOYEE_DATA);
    }
}
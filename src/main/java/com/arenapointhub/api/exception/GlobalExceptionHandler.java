package com.arenapointhub.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = e.getBindingResult().getFieldError() != null 
                ? e.getBindingResult().getFieldError().getDefaultMessage() 
                : "Erro de validação nos campos.";

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Dados inválidos",
                errorMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> business(BusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Regra de negócio violada",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }
}
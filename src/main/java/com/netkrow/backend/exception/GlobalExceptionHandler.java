package com.netkrow.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

/**
 * Manejo global de errores.
 *
 * MVP:
 * - Devuelve JSON estándar: { status, message }
 * - No depende de JPA ni de spring-dao (por eso NO usamos DataIntegrityViolationException).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : "Error";
        int code = ex.getStatusCode().value();
        return ResponseEntity.status(code).body(new ErrorResponse(code, msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        int code = HttpStatus.BAD_REQUEST.value();
        String msg = "Request inválido: valida los campos requeridos.";
        return ResponseEntity.status(code).body(new ErrorResponse(code, msg));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSql(SQLException ex) {
        int code = HttpStatus.BAD_GATEWAY.value();
        return ResponseEntity.status(code).body(new ErrorResponse(code, "Oracle error: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        return ResponseEntity.status(code).body(new ErrorResponse(code, "Error interno del servidor"));
    }
}

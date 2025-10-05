package org.example.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonParseError(HttpMessageNotReadableException ex,
                                                  HttpServletRequest request) {
        log.warn("JSON parse error at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON or invalid value type. Please ensure all fields have the correct type.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation error at {}: {}", request.getRequestURI(), fieldErrors);
        return buildDetailedErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields.",
                request.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException ex,
                                                           HttpServletRequest request) {
        log.warn("Entity not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<?> handleInvalidJwtToken(InvalidJwtTokenException ex,
                                                   HttpServletRequest request) {
        log.warn("Invalid JWT token at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid authentication token",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ExpiredJwtTokenException.class)
    public ResponseEntity<?> handleExpiredJwtToken(ExpiredJwtTokenException ex,
                                                   HttpServletRequest request) {
        log.warn("Expired JWT token at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication token has expired",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MalformedJwtTokenException.class)
    public ResponseEntity<?> handleMalformedJwtToken(MalformedJwtTokenException ex,
                                                     HttpServletRequest request) {
        log.warn("Malformed JWT token at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Malformed authentication token",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<?> handleJwtAuthenticationException(JwtAuthenticationException ex,
                                                              HttpServletRequest request) {
        log.warn("JWT authentication error at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex,
                                                  HttpServletRequest request) {
        log.warn("Bad credentials attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException ex,
                                                    HttpServletRequest request) {
        log.warn("Username not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleAccountDisabled(DisabledException ex,
                                                   HttpServletRequest request) {
        log.warn("Disabled account access attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Account is disabled",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleAccountLocked(LockedException ex,
                                                 HttpServletRequest request) {
        log.warn("Locked account access attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Account is locked",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex,
                                                            HttpServletRequest request) {
        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: ", request.getRequestURI(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }

    private ResponseEntity<Map<String, Object>> buildDetailedErrorResponse(HttpStatus status, String message, String path, Map<String, String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        body.put("path", path);
        body.put("errors", details);
        return new ResponseEntity<>(body, status);
    }
}

package org.example.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
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

        String message = "Malformed JSON or invalid value type. Please ensure all fields have the correct type.";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Cannot deserialize value of type")) {
                message = "Invalid field type in JSON request";
            } else if (ex.getMessage().contains("Required request body is missing")) {
                message = "Request body is required but not provided";
            }
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
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

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                fieldErrors.put(error.getObjectName(), error.getDefaultMessage())
        );

        log.warn("Validation error at {}: {}", request.getRequestURI(), fieldErrors);
        return buildDetailedErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields.",
                request.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                HttpServletRequest request) {
        String message = String.format(
                "Method parameter '%s': Failed to convert value of type '%s' to required type '%s'",
                ex.getName(),
                ex.getValue() != null ? ex.getValue().getClass().getSimpleName() : "null",
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        log.warn("Type mismatch at {}: {}", request.getRequestURI(), message);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParameter(MissingServletRequestParameterException ex,
                                                    HttpServletRequest request) {
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

        log.warn("Missing parameter at {}: {}", request.getRequestURI(), message);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                      HttpServletRequest request) {
        String message = String.format(
                "HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(),
                String.join(", ", ex.getSupportedMethods() != null ? ex.getSupportedMethods() : new String[]{})
        );

        log.warn("Method not supported at {}: {}", request.getRequestURI(), message);
        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                         HttpServletRequest request) {
        String message = String.format(
                "Media type '%s' is not supported. Supported media types: %s",
                ex.getContentType(),
                ex.getSupportedMediaTypes()
        );

        log.warn("Media type not supported at {}: {}", request.getRequestURI(), message);
        return buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<?> handleNoHandlerFound(Exception ex, HttpServletRequest request) {
        log.warn("No handler found for {}", request.getRequestURI());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                String.format("No endpoint found for URI: %s", request.getRequestURI()),
                request.getRequestURI()
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        String message = "Data integrity violation";

        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("duplicate key") || ex.getMessage().contains("unique constraint")) {
                message = "Duplicate entry - this record already exists";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Cannot perform operation - referenced by other records";
            } else if (ex.getMessage().contains("not-null constraint")) {
                message = "Required field is missing";
            }
        }

        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                message,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                  HttpServletRequest request) {
        log.warn("Optimistic lock failure at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "The resource was modified by another transaction. Please refresh and try again.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex,
                                                HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage() != null ? ex.getMessage() : "Access denied",
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex,
                                                         HttpServletRequest request) {
        log.warn("Illegal state at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
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

    private ResponseEntity<Map<String, Object>> buildDetailedErrorResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> details) {

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

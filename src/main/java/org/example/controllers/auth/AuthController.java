package org.example.controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.security.JwtService;
import org.example.model.dto.AuthRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication operations for user login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and generate JWT token",
            description = """
                    Authenticates a user with username and password credentials.
                    Upon successful authentication, returns a JWT token that can be used for accessing protected endpoints.
                    The token should be included in the Authorization header as: Bearer {token}
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials (username and password)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Admin Login",
                                            summary = "Login as admin user",
                                            description = "Use this to get an admin token for accessing admin endpoints",
                                            value = """
                                            {
                                              "username": "admin_first",
                                              "password": "admin123password"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Customer Login",
                                            summary = "Login as customer user",
                                            description = "Use this to get a customer token for accessing customer endpoints",
                                            value = """
                                            {
                                              "username": "SarSmi",
                                              "password": "customer123"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful - JWT token generated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    summary = "Login successful response",
                                    description = "Returns a JWT token valid for accessing protected endpoints",
                                    value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbl9maXJzdCIsImF1dGhvcml0aWVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzAzMzMxMDAwLCJleHAiOjE3MDM0MTc0MDB9.signature"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed - Invalid credentials or user not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Credentials",
                                            summary = "Wrong username or password",
                                            description = "Returned when username doesn't exist or password is incorrect",
                                            value = """
                                            {
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Invalid username or password",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/auth/login"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Authentication Failed",
                                            summary = "General authentication error",
                                            description = "Returned when authentication process encounters an unexpected error",
                                            value = """
                                            {
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Authentication failed",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/auth/login"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation errors or malformed request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Missing Username",
                                            summary = "Username field is required",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "username": "must not be blank"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/auth/login"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Missing Password",
                                            summary = "Password field is required",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "errors": {
                                                "password": "must not be blank"
                                              },
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/auth/login"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Malformed JSON",
                                            summary = "Invalid JSON format",
                                            value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Malformed JSON request",
                                              "timestamp": "2024-12-23T10:30:00",
                                              "path": "/auth/login"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDto authRequest,
                                   HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");

        log.info("Login attempt - Username: {}, User-Agent: {}",
                authRequest.getUsername(), userAgent);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails user = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String token = jwtService.generateToken(user);

            log.info("Successful login - Username: {}, Authorities: {}",
                    authRequest.getUsername(), user.getAuthorities());

            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt - Username: {}, Reason: Invalid credentials",
                    authRequest.getUsername());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid username or password",
                            request.getRequestURI()
                    ));

        } catch (UsernameNotFoundException e) {
            log.warn("Failed login attempt - Username: {}, Reason: User not found",
                    authRequest.getUsername());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid username or password",
                            request.getRequestURI()
                    ));

        } catch (Exception e) {
            log.error("Authentication error - Username: {}, Error: {}",
                    authRequest.getUsername(), e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse(
                            HttpStatus.UNAUTHORIZED,
                            "Authentication failed",
                            request.getRequestURI()
                    ));
        }
    }

    private Map<String, Object> buildErrorResponse(HttpStatus status, String message, String path) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", path);
        return errorResponse;
    }
}

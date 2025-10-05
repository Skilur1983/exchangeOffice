package org.example.controllers;

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
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
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

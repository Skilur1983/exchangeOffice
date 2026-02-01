package org.example.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.exceptions.ExpiredJwtTokenException;
import org.example.exceptions.InvalidJwtTokenException;
import org.example.exceptions.JwtAuthenticationException;
import org.example.exceptions.MalformedJwtTokenException;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.model.UserPrincipal;
import org.example.service.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No JWT token found in request headers for URI: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(token, username)) {

                    String role = jwtService.extractRole(token);
                    Integer userId = jwtService.extractUserId(token);

                    User user = new User();
                    user.setId(userId);
                    user.setUsername(username);
                    user.setRole(RoleName.valueOf(role.replace("ROLE_", "")));

                    UserPrincipal userPrincipal = new UserPrincipal(user);

                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userPrincipal,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Successfully authenticated user: {} for URI: {} (no DB lookup)",
                            username, request.getRequestURI());
                } else {
                    log.warn("Invalid JWT token for user: {} at URI: {}", username, request.getRequestURI());
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtTokenException e) {
            log.warn("Expired JWT token at URI: {}", request.getRequestURI());
            handleJwtException(response, e, "Authentication token has expired", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtTokenException e) {
            log.warn("Malformed JWT token at URI: {}", request.getRequestURI());
            handleJwtException(response, e, "Malformed authentication token", HttpStatus.UNAUTHORIZED);
        } catch (InvalidJwtTokenException e) {
            log.warn("Invalid JWT token at URI: {}", request.getRequestURI());
            handleJwtException(response, e, "Invalid authentication token", HttpStatus.UNAUTHORIZED);
        } catch (JwtAuthenticationException e) {
            log.warn("JWT authentication error at URI: {}", request.getRequestURI());
            handleJwtException(response, e, "Authentication failed", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter for URI: {}", request.getRequestURI(), e);
            handleJwtException(response, e, "Authentication error", HttpStatus.UNAUTHORIZED);
        }
    }

    private void handleJwtException(HttpServletResponse response,
                                    Exception e,
                                    String message,
                                    HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", "JWT Filter");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") ||
                path.equals("/error") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger-") ||
                path.startsWith("/v3/api-docs");
    }
}

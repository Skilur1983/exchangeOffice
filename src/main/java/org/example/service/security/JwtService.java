package org.example.service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.example.config.security.SecurityConstants;
import org.example.exceptions.ExpiredJwtTokenException;
import org.example.exceptions.InvalidJwtTokenException;
import org.example.exceptions.MalformedJwtTokenException;
import org.example.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    private final Key key;
    private final long jwtExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpirationMs
    ) {
        if (secret.length() < SecurityConstants.JWT_SECRET_MIN_LENGTH) {
            throw new IllegalArgumentException("JWT secret must be at least " +
                    SecurityConstants.JWT_SECRET_MIN_LENGTH + " characters long");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
        log.info("JWT service initialized with expiration: {} ms", jwtExpirationMs);
    }

    public String generateToken(UserDetails userDetails) {
        try {
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            Integer userId = null;
            if (userDetails instanceof UserPrincipal) {
                userId = ((UserPrincipal) userDetails).getUser().getId();
            }

            Claims claims = Jwts.claims();
            claims.put("role", role);
            if (userId != null) {
                claims.put("userId", userId);
            }

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            log.debug("Generated JWT token for user: {}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("Error generating JWT token for user: {}", userDetails.getUsername(), e);
            throw new InvalidJwtTokenException("Failed to generate JWT token", e);
        }
    }

    public String extractUsername(String token) {
        try {
            String username = parseClaims(token).getSubject();
            log.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            log.warn("Failed to extract username from token: {}", e.getMessage());
            throw handleJwtException(e);
        }
    }

    public Integer extractUserId(String token) {
        try {
            Integer userId = (Integer) parseClaims(token).get("userId");
            log.debug("Extracted userId from token: {}", userId);
            return userId;
        } catch (Exception e) {
            log.warn("Failed to extract userId from token: {}", e.getMessage());
            throw handleJwtException(e);
        }
    }

    public String extractRole(String token) {
        try {
            String role = (String) parseClaims(token).get("role");
            log.debug("Extracted role from token: {}", role);
            return role;
        } catch (Exception e) {
            log.warn("Failed to extract role from token: {}", e.getMessage());
            throw handleJwtException(e);
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            boolean isValid = username.equals(tokenUsername) && !isTokenExpired(token);
            log.debug("Token validation result for user {}: {}", username, isValid);
            return isValid;
        } catch (ExpiredJwtTokenException e) {
            log.debug("Token expired for user: {}", username);
            return false;
        } catch (Exception e) {
            log.warn("Token validation failed for user: {}: {}", username, e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            boolean expired = parseClaims(token).getExpiration().before(new Date());
            if (expired) {
                log.debug("Token is expired");
            }
            return expired;
        } catch (ExpiredJwtException e) {
            log.debug("Token is expired (caught ExpiredJwtException)");
            return true;
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            throw handleJwtException(e);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired");
            throw new ExpiredJwtTokenException("JWT token has expired", e);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token");
            throw new MalformedJwtTokenException("Malformed JWT token", e);
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature");
            throw new InvalidJwtTokenException("Invalid JWT signature", e);
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new InvalidJwtTokenException("Invalid JWT token", e);
        } catch (Exception e) {
            log.error("Unexpected error parsing JWT token", e);
            throw new InvalidJwtTokenException("Failed to parse JWT token", e);
        }
    }

    private RuntimeException handleJwtException(Exception e) {
        if (e instanceof ExpiredJwtTokenException ||
                e instanceof MalformedJwtTokenException ||
                e instanceof InvalidJwtTokenException) {
            return (RuntimeException) e;
        } else if (e instanceof ExpiredJwtException) {
            return new ExpiredJwtTokenException("JWT token has expired", e);
        } else if (e instanceof MalformedJwtException) {
            return new MalformedJwtTokenException("Malformed JWT token", e);
        } else if (e instanceof SignatureException) {
            return new InvalidJwtTokenException("Invalid JWT signature", e);
        } else if (e instanceof JwtException) {
            return new InvalidJwtTokenException("Invalid JWT token", e);
        } else {
            return new InvalidJwtTokenException("Failed to process JWT token", e);
        }
    }
}

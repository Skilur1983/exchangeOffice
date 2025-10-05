package org.example.exceptions;

public class ExpiredJwtTokenException extends JwtAuthenticationException {

    public ExpiredJwtTokenException(String message) {
        super(message);
    }

    public ExpiredJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

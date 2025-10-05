package org.example.exceptions;

public class MalformedJwtTokenException extends JwtAuthenticationException {

    public MalformedJwtTokenException(String message) {
        super(message);
    }

    public MalformedJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

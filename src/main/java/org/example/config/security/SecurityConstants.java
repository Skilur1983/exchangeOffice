package org.example.config.security;

public final class SecurityConstants {

    public static final int JWT_SECRET_MIN_LENGTH = 32;

    public static final int BCRYPT_STRENGTH = 12;

    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;

    private SecurityConstants() {
    }
}

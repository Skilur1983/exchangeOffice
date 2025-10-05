package org.example.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.config.security.SecurityConstants;

@Data
public class AuthRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = SecurityConstants.MAX_USERNAME_LENGTH,
            message = "Username must be between 3 and " + SecurityConstants.MAX_USERNAME_LENGTH + " characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = SecurityConstants.PASSWORD_MIN_LENGTH, max = SecurityConstants.PASSWORD_MAX_LENGTH,
            message = "Password must be between " + SecurityConstants.PASSWORD_MIN_LENGTH +
                    " and " + SecurityConstants.PASSWORD_MAX_LENGTH + " characters")
    private String password;

    @Override
    public String toString() {
        return "AuthRequestDto{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}

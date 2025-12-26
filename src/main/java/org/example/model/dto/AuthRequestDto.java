package org.example.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.config.security.SecurityConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication request containing user credentials")
public class AuthRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = SecurityConstants.MAX_USERNAME_LENGTH,
            message = "Username must be between 3 and " + SecurityConstants.MAX_USERNAME_LENGTH + " characters")
    @Schema(
            description = "Username for authentication",
            example = "admin_first",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = SecurityConstants.PASSWORD_MIN_LENGTH, max = SecurityConstants.PASSWORD_MAX_LENGTH,
            message = "Password must be between " + SecurityConstants.PASSWORD_MIN_LENGTH +
                    " and " + SecurityConstants.PASSWORD_MAX_LENGTH + " characters")
    @Schema(
            description = "Password for authentication",
            example = "admin123password",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    private String password;

    @Override
    public String toString() {
        return "AuthRequestDto{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}

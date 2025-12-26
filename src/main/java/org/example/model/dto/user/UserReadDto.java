package org.example.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.RoleName;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information (password excluded)")
public class UserReadDto {

    @Schema(description = "Unique user identifier", example = "1")
    private Integer id;

    @Schema(description = "Username", example = "admin_first")
    private String username;

    @Schema(description = "User role", example = "ADMIN", allowableValues = {"ADMIN", "CUSTOMER"})
    private RoleName role;

    @Schema(description = "Account creation timestamp", example = "2024-12-23T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-12-23T10:00:00")
    private LocalDateTime updatedAt;
}

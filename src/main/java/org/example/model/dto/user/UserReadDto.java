package org.example.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.RoleName;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReadDto {

    private Integer id;
    private RoleName role;
    private String username;
    private String password;
}

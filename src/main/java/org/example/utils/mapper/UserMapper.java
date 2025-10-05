package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserMapper {

    public UserReadDto toReadDto(User user) {
        UserReadDto userReadDto = new UserReadDto();

        userReadDto.setId(user.getId());
        userReadDto.setUsername(user.getUsername());
        userReadDto.setPassword(user.getPassword());
        userReadDto.setRole(user.getRole());

        return userReadDto;
    }

    public User toEntity(UserCreateDto dto) {
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());

        return user;
    }
}

package org.example.service;

import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserUpdateDto;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserReadDto getById(int id);
    UserReadDto getByUsername(String login);
    PageDto<UserReadDto> getAll(Pageable pageable);
    User getEntityById(int id);
    void save(UserCreateDto userCreateDto);
    void update(int id, UserUpdateDto userUpdateDto);
    void deleteById(int id);
}

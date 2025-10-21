package org.example.service;

import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserPasswordChangeDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserUpdateDto;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserReadDto getById(int id);
    UserReadDto getByUsername(String login);
    PageDto<UserReadDto> getAll(Pageable pageable);
    User getEntityById(int id);
    UserReadDto save(UserCreateDto userCreateDto);
    UserReadDto update(int id, UserUpdateDto userUpdateDto);
    void updatePassword(int id, UserPasswordChangeDto passwordChangeDto);
    void deleteById(int id);
}

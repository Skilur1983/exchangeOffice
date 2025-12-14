package org.example.service;

import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.*;

import java.util.Map;

public interface UserService {
    UserReadDto getById(Integer id);
    UserReadDto getByUsername(String login);
    UserWithCurrencyBalanceReadDto getByIdWithBalances(Integer id);
    PageDto<UserReadDto> getAll(Map<String, String> params);
    UserReadDto create(UserCreateDto userCreateDto);
    UserReadDto update(Integer id, UserUpdateDto userUpdateDto);
    void updatePassword(Integer id, UserPasswordChangeDto passwordChangeDto);
    void deleteById(Integer id);
    User getEntityById(Integer id);
}

package org.example.service;

import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.user.*;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserReadDto getById(int id);
    UserReadDto getByUsername(String login);
    UserWithCurrencyBalanceReadDto getByIdWithBalances(Integer id);
    PageDto<UserReadDto> getAll(Pageable pageable);
    UserReadDto create(UserCreateDto userCreateDto);
    UserReadDto update(int id, UserUpdateDto userUpdateDto);
    void updatePassword(int id, UserPasswordChangeDto passwordChangeDto);
    void deleteById(int id);
    User getEntityById(int id);
}

package org.example.service;

import org.example.model.Currency;
import org.example.model.dto.user.UserPasswordChangeDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserWithCurrencyBalanceReadDto;

import java.util.List;

public interface UserAccountService {

    void changePassword(Integer userId, UserPasswordChangeDto dto);
    UserWithCurrencyBalanceReadDto getUserWithBalances(Integer userId);
    void activateUser(Integer userId);
    void deactivateUser(Integer userId);
    List<UserReadDto> findUsersWithInsufficientBalance(Currency currency);
}

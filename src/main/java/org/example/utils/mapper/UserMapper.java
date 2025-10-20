package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.example.model.dto.user.UserWithCurrencyBalanceReadDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class UserMapper {

    private final CurrencyBalanceMapper currencyBalanceMapper;

    public UserReadDto toReadDto(User user) {

        return UserReadDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserWithCurrencyBalanceReadDto toWithCurrencyBalanceReadDto(User user) {

        List<CurrencyBalanceReadDto> balances = user.getBalances() != null ?
                user.getBalances().stream()
                        .map(currencyBalanceMapper::toReadDto)
                        .toList()
                : Collections.emptyList();

        return UserWithCurrencyBalanceReadDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .balances(balances)
                .build();
    }

    public User toEntity(UserCreateDto userCreateDto, String hashedPassword) {

        return User.builder()
                .username(userCreateDto.getUsername())
                .password(hashedPassword)
                .role(userCreateDto.getRole())
                .build();
    }
}

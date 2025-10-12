package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.model.dto.user.UserCreateDto;
import org.example.model.dto.user.UserReadDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class UserMapper {

    public UserReadDto toReadDto(User user) {

        List<CurrencyBalanceReadDto> balances = user.getBalances() != null ?
                user.getBalances().stream()
                        .map(balance -> CurrencyBalanceReadDto.builder()
                                .id(balance.getId())
                                .currency(balance.getCurrency())
                                .amount(balance.getAmount())
                                .createdAt(balance.getCreatedAt())
                                .updatedAt(balance.getUpdatedAt())
                                .username(balance.getUser().getUsername())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return UserReadDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .currencyBalances(balances)
                .build();
    }

    public User toEntity(UserCreateDto userCreateDto) {

        return User.builder()
                .username(userCreateDto.getUsername())
                .password(userCreateDto.getPassword())
                .role(userCreateDto.getRole())
                .build();
    }
}

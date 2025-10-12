package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.CurrencyBalance;
import org.example.model.User;
import org.example.model.dto.currencybalance.CurrencyBalanceCreateDto;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CurrencyBalanceMapper {

    public CurrencyBalanceReadDto toReadDto(CurrencyBalance currencyBalance) {
        Integer userId = null;
        String username = null;


        if (currencyBalance.getUser() != null) {
            userId = currencyBalance.getUser().getId();
            username = currencyBalance.getUser().getUsername();
        }

        return CurrencyBalanceReadDto.builder()
                .id(currencyBalance.getId())
                .currency(currencyBalance.getCurrency())
                .amount(currencyBalance.getAmount())
                .createdAt(currencyBalance.getCreatedAt())
                .updatedAt(currencyBalance.getUpdatedAt())
                .userId(userId)
                .username(username)
                .build();
    }

    public CurrencyBalance toEntity(CurrencyBalanceCreateDto currencyBalanceCreateDto, User user) {
        return CurrencyBalance.builder()
                .currency(currencyBalanceCreateDto.getCurrency())
                .amount(currencyBalanceCreateDto.getAmount())
                .user(user)
                .build();
    }
}

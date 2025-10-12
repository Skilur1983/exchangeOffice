package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.CurrencyBalance;
import org.example.model.DayRate;
import org.example.model.Deal;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DealMapper {

    private final CurrencyBalanceMapper currencyBalanceMapper;
    private final DayRateMapper dayRateMapper;

    public DealReadDto toReadDto(Deal deal) {
        CurrencyBalanceReadDto sellerCurrencyBalance = null;
        CurrencyBalanceReadDto buyerCurrencyBalance = null;
        DayRateReadDto dayRateReadDto = null;

        if (deal.getSellerCurrencyBalance() != null) {
            sellerCurrencyBalance = currencyBalanceMapper.toReadDto(deal.getSellerCurrencyBalance());
        }

        if (deal.getBuyerCurrencyBalance() != null) {
            buyerCurrencyBalance = currencyBalanceMapper.toReadDto(deal.getBuyerCurrencyBalance());
        }

        if (deal.getDayRate() != null) {
            dayRateReadDto = dayRateMapper.toReadDto(deal.getDayRate());
        }

        return DealReadDto.builder()
                .id(deal.getId())
                .sellerCurrencyBalance(sellerCurrencyBalance)
                .buyerCurrencyBalance(buyerCurrencyBalance)
                .dayRate(dayRateReadDto)
                .baseAmount(deal.getBaseAmount())
                .dealType(deal.getDealType())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
    }

    public Deal toEntity(DealCreateDto dealCreateDto,
                         CurrencyBalance sellerCurrencyBalance,
                         CurrencyBalance buyerCurrencyBalance,
                         DayRate dayRate) {
        return Deal.builder()
                .sellerCurrencyBalance(sellerCurrencyBalance)
                .buyerCurrencyBalance(buyerCurrencyBalance)
                .dayRate(dayRate)
                .baseAmount(dealCreateDto.getBaseAmount())
                .dealType(dealCreateDto.getDealType())
                .build();
    }
}

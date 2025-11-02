package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.DayRate;
import org.example.model.Deal;
import org.example.model.User;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.user.UserReadDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DealMapper {

    private final UserMapper userMapper;
    private final DayRateMapper dayRateMapper;

    public DealReadDto toReadDto(Deal deal) {
        UserReadDto seller = userMapper.toReadDto(deal.getSeller());
        UserReadDto buyer = userMapper.toReadDto(deal.getBuyer());
        DayRateReadDto dayRate = dayRateMapper.toReadDto(deal.getDayRate());

        return DealReadDto.builder()
                .id(deal.getId())
                .seller(seller)
                .sellerCurrency(deal.getSellerCurrency())
                .buyer(buyer)
                .buyerCurrency(deal.getBuyerCurrency())
                .dayRate(dayRate)
                .soldAmount(deal.getSoldAmount())
                .purchasedAmount(deal.getPurchasedAmount())
                .exchangeRateUsed(deal.getExchangeRateUsed())
                .dealType(deal.getDealType())
                .status(deal.getStatus())
                .statusReason(deal.getStatusReason())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .pausedAt(deal.getPausedAt())
                .completedAt(deal.getCompletedAt())
                .cancelledAt(deal.getCancelledAt())
                .build();
    }

    public Deal toEntity(DealCreateDto dealCreateDto,
                         User seller,
                         User buyer,
                         DayRate dayRate) {

        return Deal.builder()
                .seller(seller)
                .sellerCurrency(dealCreateDto.getSellerCurrency())
                .buyer(buyer)
                .buyerCurrency(dealCreateDto.getBuyerCurrency())
                .soldAmount(dealCreateDto.getSoldAmount())
                .purchasedAmount(dealCreateDto.getPurchasedAmount())
                .dealType(dealCreateDto.getDealType())
                .dayRate(dayRate)
                .build();
    }
}

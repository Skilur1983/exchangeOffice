package org.example.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.example.model.DayRate;
import org.example.model.dto.dayrate.DayRateCreateDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DayRateMapper {

    public DayRateReadDto toReadDto(DayRate dayRate) {
        return DayRateReadDto.builder()
                .id(dayRate.getId())
                .baseCurrency(dayRate.getBaseCurrency())
                .quoteCurrency(dayRate.getQuoteCurrency())
                .rateDate(dayRate.getRateDate())
                .createdAt(dayRate.getCreatedAt())
                .updatedAt(dayRate.getUpdatedAt())
                .buyRate(dayRate.getBuyRate())
                .sellRate(dayRate.getSellRate())
                .build();
    }

    public DayRate toEntity(DayRateCreateDto dayRateCreateDto) {
        return DayRate.builder()
                .baseCurrency(dayRateCreateDto.getBaseCurrency())
                .quoteCurrency(dayRateCreateDto.getQuoteCurrency())
                .rateDate(dayRateCreateDto.getRateDate())
                .buyRate(dayRateCreateDto.getBuyRate())
                .sellRate(dayRateCreateDto.getSellRate())
                .build();
    }
}

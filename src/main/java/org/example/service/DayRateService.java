package org.example.service;

import org.example.model.Currency;
import org.example.model.DayRate;
import org.example.model.dto.PageDto;
import org.example.model.dto.dayrate.DayRateCreateDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.dayrate.DayRateUpdateDto;

import java.time.LocalDate;
import java.util.Map;

public interface DayRateService {

    DayRateReadDto getById(Integer id);
    DayRateReadDto getCurrentRate(Currency baseCurrency, Currency quoteCurrency);
    DayRateReadDto getRateForDate(Currency baseCurrency, Currency quoteCurrency, LocalDate date);

    PageDto<DayRateReadDto> getAll(Map<String, String> params);

    DayRateReadDto create(DayRateCreateDto dto);
    DayRateReadDto update(Integer id, DayRateUpdateDto dto);
    void deleteById(Integer id);

    DayRate getEntityById(Integer id);
    DayRate getCurrentRateEntity(Currency baseCurrency, Currency quoteCurrency);
}

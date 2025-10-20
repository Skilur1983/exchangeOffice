package org.example.service;

import org.example.model.Currency;
import org.example.model.DayRate;
import org.example.model.dto.PageDto;
import org.example.model.dto.dayrate.DayRateCreateDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.dayrate.DayRateUpdateDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DayRateService {

    DayRateReadDto getById(Integer id);
    DayRateReadDto getCurrentRate(Currency baseCurrency, Currency quoteCurrency);
    DayRateReadDto getRateForDate(Currency baseCurrency, Currency quoteCurrency, LocalDate date);
    List<DayRateReadDto> getRatesForDateRange(Currency baseCurrency, Currency quoteCurrency,
                                              LocalDate startDate, LocalDate endDate);
    PageDto<DayRateReadDto> getAll(Pageable pageable);

    DayRateReadDto create(DayRateCreateDto dto);
    DayRateReadDto update(Integer id, DayRateUpdateDto dto);
    void deleteById(Integer id);

    DayRate getEntityById(Integer id);
    DayRate getCurrentRateEntity(Currency baseCurrency, Currency quoteCurrency);
}

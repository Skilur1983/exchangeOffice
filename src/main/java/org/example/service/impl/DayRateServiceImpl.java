package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.model.Currency;
import org.example.model.DayRate;
import org.example.model.dto.PageDto;
import org.example.model.dto.dayrate.DayRateCreateDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.dayrate.DayRateUpdateDto;
import org.example.repository.DayRateRepository;
import org.example.service.DayRateService;
import org.example.utils.mapper.DayRateMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DayRateServiceImpl implements DayRateService {

    private final DayRateRepository dayRateRepository;
    private final DayRateMapper dayRateMapper;

    @Override
    public DayRateReadDto getById(Integer id) {
        return null;
    }

    @Override
    public DayRateReadDto getCurrentRate(Currency baseCurrency, Currency quoteCurrency) {
        return null;
    }

    @Override
    public DayRateReadDto getRateForDate(Currency baseCurrency, Currency quoteCurrency, LocalDate date) {
        return null;
    }

    @Override
    public List<DayRateReadDto> getRatesForDateRange(Currency baseCurrency, Currency quoteCurrency, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }

    @Override
    public PageDto<DayRateReadDto> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public DayRateReadDto create(DayRateCreateDto dto) {
        return null;
    }

    @Override
    public DayRateReadDto update(Integer id, DayRateUpdateDto dto) {
        return null;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public DayRate getEntityById(Integer id) {
        return null;
    }

    @Override
    public DayRate getCurrentRateEntity(Currency baseCurrency, Currency quoteCurrency) {
        return null;
    }
}

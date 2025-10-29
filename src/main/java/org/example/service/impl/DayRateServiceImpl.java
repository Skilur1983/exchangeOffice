package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DayRateServiceImpl implements DayRateService {

    private final DayRateRepository dayRateRepository;
    private final DayRateMapper dayRateMapper;

    @Override
    @Transactional(readOnly = true)
    public DayRateReadDto getById(Integer id) {
        return dayRateRepository.findById(id)
                .map(dayRateMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("Day Rate with ID: " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DayRateReadDto getCurrentRate(Currency baseCurrency, Currency quoteCurrency) {
        return dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(baseCurrency, quoteCurrency, LocalDate.now())
                .map(dayRateMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("Day Rate for: "
                        + baseCurrency + " / " + quoteCurrency + " for today not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DayRateReadDto getRateForDate(Currency baseCurrency, Currency quoteCurrency, LocalDate date) {
        return dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(baseCurrency, quoteCurrency, date)
                .map(dayRateMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("Day Rate for: "
                        + baseCurrency + " / " + quoteCurrency + " for "
                        + date + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<DayRateReadDto> getAll(Pageable pageable) {
        Page<DayRate> dayRatePage = dayRateRepository.findAll(pageable);
        List<DayRateReadDto> dayRateReadDtos = dayRatePage.stream()
                .map(dayRateMapper::toReadDto)
                .toList();

        return PageDto.<DayRateReadDto>builder()
                .content(dayRateReadDtos)
                .pageNumber(dayRatePage.getNumber())
                .pageSize(dayRatePage.getSize())
                .totalElements(dayRatePage.getTotalElements())
                .totalPages(dayRatePage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public DayRateReadDto create(DayRateCreateDto dto) {
        if (dayRateRepository.existsByBaseCurrencyAndQuoteCurrencyAndRateDate(dto.getBaseCurrency(), dto.getQuoteCurrency(), dto.getRateDate())) {
            throw new IllegalArgumentException("Day Rate for: "
                    + dto.getBaseCurrency() + " / " + dto.getQuoteCurrency() + " for "
                    + dto.getRateDate() +" already exists.");
        }

        DayRate dayRate = dayRateMapper.toEntity(dto);

        DayRate savedDayRate = dayRateRepository.save(dayRate);
        return dayRateMapper.toReadDto(savedDayRate);
    }

    @Override
    @Transactional
    public DayRateReadDto update(Integer id, DayRateUpdateDto dto) {
        DayRate dayRate = getEntityById(id);

        dayRateMapper.updateEntity(dayRate, dto);

        DayRate updatedDayRate = dayRateRepository.save(dayRate);
        return dayRateMapper.toReadDto(updatedDayRate);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!dayRateRepository.existsById(id)) {
            throw new EntityNotFoundException("Day Rate with ID " + id + " not found");
        }
        dayRateRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DayRate getEntityById(Integer id) {
        return dayRateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Day Rate with ID " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DayRate getCurrentRateEntity(Currency baseCurrency, Currency quoteCurrency) {
        return dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(baseCurrency, quoteCurrency, LocalDate.now())
                .orElseThrow(() -> new EntityNotFoundException("Day Rate for: "
                        + baseCurrency + " / " + quoteCurrency + " for today not found"));
    }
}

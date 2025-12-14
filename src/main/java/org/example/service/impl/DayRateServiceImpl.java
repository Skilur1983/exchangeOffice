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
import org.example.repository.specification.SpecificationManager;
import org.example.service.DayRateService;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.DayRateMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DayRateServiceImpl implements DayRateService {

    private final DayRateRepository dayRateRepository;
    private final DayRateMapper dayRateMapper;
    private final PageableBuilder pageableBuilder;
    private final SpecificationManager<DayRate> specificationManager;

    private static final String SPLIT_TO_ARRAY = ",";

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
    public PageDto<DayRateReadDto> getAll(Map<String, String> params) {
        Pageable pageRequest = pageableBuilder.buildFromFilters(params);
        Specification<DayRate> specification = null;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<DayRate> sp = specificationManager.get(entry.getKey(), entry.getValue().split(SPLIT_TO_ARRAY));
            specification = specification == null ? Specification.where(sp) : specification.and(sp);
        }

        Page<DayRate> dayRatePagePage = dayRateRepository.findAll(specification, pageRequest);

        List<DayRateReadDto> rateReadDtos = dayRatePagePage.getContent().stream()
                .map(dayRateMapper::toReadDto)
                .collect(Collectors.toList());

        return PageDto.<DayRateReadDto>builder()
                .content(rateReadDtos)
                .pageNumber(dayRatePagePage.getNumber())
                .pageSize(dayRatePagePage.getSize())
                .totalElements(dayRatePagePage.getTotalElements())
                .totalPages(dayRatePagePage.getTotalPages())
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

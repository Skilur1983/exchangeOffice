package org.example.exchangeOffice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.model.Currency;
import org.example.model.DayRate;
import org.example.model.dto.PageDto;
import org.example.model.dto.dayrate.DayRateCreateDto;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.dayrate.DayRateUpdateDto;
import org.example.repository.DayRateRepository;
import org.example.repository.specification.SpecificationManager;
import org.example.service.impl.DayRateServiceImpl;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.DayRateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DayRateServiceTest {

    @Mock
    private DayRateRepository dayRateRepository;

    @Mock
    private DayRateMapper dayRateMapper;

    @Mock
    private PageableBuilder pageableBuilder;

    @Mock
    private SpecificationManager<DayRate> specificationManager;

    @InjectMocks
    private DayRateServiceImpl dayRateService;

    private DayRate testDayRate;
    private DayRateReadDto testDayRateReadDto;
    private DayRateCreateDto testDayRateCreateDto;
    private DayRateUpdateDto testDayRateUpdateDto;

    @BeforeEach
    void setUp() {
        testDayRate = new DayRate();
        testDayRate.setId(1);
        testDayRate.setBaseCurrency(Currency.USD);
        testDayRate.setQuoteCurrency(Currency.EUR);
        testDayRate.setBuyRate(BigDecimal.valueOf(0.94));
        testDayRate.setSellRate(BigDecimal.valueOf(0.97));
        testDayRate.setRateDate(LocalDate.of(2024, 12, 23));

        testDayRateReadDto = DayRateReadDto.builder()
                .id(1)
                .baseCurrency(Currency.USD)
                .quoteCurrency(Currency.EUR)
                .buyRate(BigDecimal.valueOf(0.94))
                .sellRate(BigDecimal.valueOf(0.97))
                .rateDate(LocalDate.of(2024, 12, 23))
                .build();

        testDayRateCreateDto = DayRateCreateDto.builder()
                .baseCurrency(Currency.USD)
                .quoteCurrency(Currency.EUR)
                .buyRate(BigDecimal.valueOf(0.94))
                .sellRate(BigDecimal.valueOf(0.97))
                .rateDate(LocalDate.of(2024, 12, 23))
                .build();

        testDayRateUpdateDto = DayRateUpdateDto.builder()
                .buyRate(BigDecimal.valueOf(0.95))
                .sellRate(BigDecimal.valueOf(0.98))
                .build();
    }

    @Test
    void getById_ExistingRate_ReturnsRateReadDto() {
        when(dayRateRepository.findById(1)).thenReturn(Optional.of(testDayRate));
        when(dayRateMapper.toReadDto(testDayRate)).thenReturn(testDayRateReadDto);

        DayRateReadDto result = dayRateService.getById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(result.getQuoteCurrency()).isEqualTo(Currency.EUR);
        verify(dayRateRepository).findById(1);
        verify(dayRateMapper).toReadDto(testDayRate);
    }

    @Test
    void getById_NonExistentRate_ThrowsEntityNotFoundException() {
        when(dayRateRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dayRateService.getById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate with ID: 999 not found");

        verify(dayRateRepository).findById(999);
        verify(dayRateMapper, never()).toReadDto(any());
    }

    @Test
    void getCurrentRate_ExistingRate_ReturnsRateReadDto() {
        LocalDate today = LocalDate.now();
        testDayRate.setRateDate(today);
        testDayRateReadDto = DayRateReadDto.builder()
                .id(1)
                .baseCurrency(Currency.USD)
                .quoteCurrency(Currency.EUR)
                .rateDate(today)
                .build();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today))
                .thenReturn(Optional.of(testDayRate));
        when(dayRateMapper.toReadDto(testDayRate)).thenReturn(testDayRateReadDto);

        DayRateReadDto result = dayRateService.getCurrentRate(Currency.USD, Currency.EUR);

        assertThat(result).isNotNull();
        assertThat(result.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(result.getQuoteCurrency()).isEqualTo(Currency.EUR);
        verify(dayRateRepository).findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today);
    }

    @Test
    void getCurrentRate_NonExistentRate_ThrowsEntityNotFoundException() {
        LocalDate today = LocalDate.now();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dayRateService.getCurrentRate(Currency.USD, Currency.EUR))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate for: USD / EUR for today not found");

        verify(dayRateRepository).findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today);
    }

    @Test
    void getRateForDate_ExistingRate_ReturnsRateReadDto() {
        LocalDate date = LocalDate.of(2024, 12, 23);

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, date))
                .thenReturn(Optional.of(testDayRate));
        when(dayRateMapper.toReadDto(testDayRate)).thenReturn(testDayRateReadDto);

        DayRateReadDto result = dayRateService.getRateForDate(Currency.USD, Currency.EUR, date);

        assertThat(result).isNotNull();
        assertThat(result.getRateDate()).isEqualTo(date);
        verify(dayRateRepository).findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, date);
    }

    @Test
    void getRateForDate_NonExistentRate_ThrowsEntityNotFoundException() {
        LocalDate date = LocalDate.of(2024, 12, 23);

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, date))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dayRateService.getRateForDate(Currency.USD, Currency.EUR, date))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate for: USD / EUR for 2024-12-23 not found");
    }

    @Test
    void getAll_WithFilters_ReturnsPagedRates() {
        Map<String, String> params = Map.of("baseCurrency", "USD", "page", "0", "size", "10");
        Pageable pageable = PageRequest.of(0, 10);
        List<DayRate> rates = List.of(testDayRate);
        Page<DayRate> ratePage = new PageImpl<>(rates, pageable, 1);

        when(pageableBuilder.buildFromFilters(params)).thenReturn(pageable);
        when(specificationManager.get(anyString(), any())).thenReturn(mock(Specification.class));
        when(dayRateRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ratePage);
        when(dayRateMapper.toReadDto(testDayRate)).thenReturn(testDayRateReadDto);

        PageDto<DayRateReadDto> result = dayRateService.getAll(params);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(dayRateRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void create_ValidRate_ReturnsCreatedRate() {
        DayRate newRate = new DayRate();
        newRate.setId(2);
        newRate.setBaseCurrency(Currency.USD);
        newRate.setQuoteCurrency(Currency.EUR);
        newRate.setRateDate(LocalDate.of(2024, 12, 23));

        DayRateReadDto createdRateDto = DayRateReadDto.builder()
                .id(2)
                .baseCurrency(Currency.USD)
                .quoteCurrency(Currency.EUR)
                .build();

        when(dayRateRepository.existsByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, LocalDate.of(2024, 12, 23)))
                .thenReturn(false);
        when(dayRateMapper.toEntity(testDayRateCreateDto)).thenReturn(newRate);
        when(dayRateRepository.save(newRate)).thenReturn(newRate);
        when(dayRateMapper.toReadDto(newRate)).thenReturn(createdRateDto);

        DayRateReadDto result = dayRateService.create(testDayRateCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getBaseCurrency()).isEqualTo(Currency.USD);
        verify(dayRateRepository).save(newRate);
    }

    @Test
    void create_DuplicateRate_ThrowsIllegalArgumentException() {
        when(dayRateRepository.existsByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, LocalDate.of(2024, 12, 23)))
                .thenReturn(true);

        assertThatThrownBy(() -> dayRateService.create(testDayRateCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Day Rate for: USD / EUR for 2024-12-23 already exists");

        verify(dayRateRepository, never()).save(any());
    }

    @Test
    void update_ValidData_ReturnsUpdatedRate() {
        DayRate updatedRate = new DayRate();
        updatedRate.setId(1);
        updatedRate.setBaseCurrency(Currency.USD);
        updatedRate.setQuoteCurrency(Currency.EUR);
        updatedRate.setBuyRate(BigDecimal.valueOf(0.95));
        updatedRate.setSellRate(BigDecimal.valueOf(0.98));

        DayRateReadDto updatedRateDto = DayRateReadDto.builder()
                .id(1)
                .baseCurrency(Currency.USD)
                .quoteCurrency(Currency.EUR)
                .buyRate(BigDecimal.valueOf(0.95))
                .sellRate(BigDecimal.valueOf(0.98))
                .build();

        when(dayRateRepository.findById(1)).thenReturn(Optional.of(testDayRate));
        when(dayRateRepository.save(testDayRate)).thenReturn(updatedRate);
        when(dayRateMapper.toReadDto(updatedRate)).thenReturn(updatedRateDto);

        DayRateReadDto result = dayRateService.update(1, testDayRateUpdateDto);

        assertThat(result).isNotNull();
        assertThat(result.getBuyRate()).isEqualByComparingTo(BigDecimal.valueOf(0.95));
        assertThat(result.getSellRate()).isEqualByComparingTo(BigDecimal.valueOf(0.98));
        verify(dayRateMapper).updateEntity(testDayRate, testDayRateUpdateDto);
        verify(dayRateRepository).save(testDayRate);
    }

    @Test
    void update_NonExistentRate_ThrowsEntityNotFoundException() {
        when(dayRateRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dayRateService.update(999, testDayRateUpdateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate with ID 999 not found");

        verify(dayRateRepository, never()).save(any());
    }

    @Test
    void deleteById_ExistingRate_DeletesSuccessfully() {
        when(dayRateRepository.existsById(1)).thenReturn(true);

        dayRateService.deleteById(1);

        verify(dayRateRepository).existsById(1);
        verify(dayRateRepository).deleteById(1);
    }

    @Test
    void deleteById_NonExistentRate_ThrowsEntityNotFoundException() {
        when(dayRateRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> dayRateService.deleteById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate with ID 999 not found");

        verify(dayRateRepository).existsById(999);
        verify(dayRateRepository, never()).deleteById(anyInt());
    }

    @Test
    void getEntityById_ExistingRate_ReturnsRate() {
        when(dayRateRepository.findById(1)).thenReturn(Optional.of(testDayRate));

        DayRate result = dayRateService.getEntityById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(dayRateRepository).findById(1);
    }

    @Test
    void getEntityById_NonExistentRate_ThrowsEntityNotFoundException() {
        when(dayRateRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dayRateService.getEntityById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate with ID 999 not found");
    }

    @Test
    void getCurrentRateEntity_ExistingRate_ReturnsRate() {
        LocalDate today = LocalDate.now();
        testDayRate.setRateDate(today);

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today))
                .thenReturn(Optional.of(testDayRate));

        DayRate result = dayRateService.getCurrentRateEntity(Currency.USD, Currency.EUR);

        assertThat(result).isNotNull();
        assertThat(result.getBaseCurrency()).isEqualTo(Currency.USD);
        verify(dayRateRepository).findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today);
    }

    @Test
    void getCurrentRateEntity_NonExistentRate_ThrowsEntityNotFoundException() {
        LocalDate today = LocalDate.now();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.USD, Currency.EUR, today))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dayRateService.getCurrentRateEntity(Currency.USD, Currency.EUR))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Day Rate for: USD / EUR for today not found");
    }
}
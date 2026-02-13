package org.example.exchangeOffice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.model.CurrencyBalance;
import org.example.model.RoleName;
import org.example.model.Currency;
import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.currencybalance.BalanceDepositDto;
import org.example.model.dto.currencybalance.BalanceWithdrawalDto;
import org.example.model.dto.currencybalance.CurrencyBalanceCreateDto;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.repository.CurrencyBalanceRepository;
import org.example.repository.UserRepository;
import org.example.repository.specification.SpecificationManager;
import org.example.service.impl.CurrencyBalanceServiceImpl;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.CurrencyBalanceMapper;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyBalanceServiceTest {

    @Mock
    private CurrencyBalanceRepository currencyBalanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrencyBalanceMapper currencyBalanceMapper;

    @Mock
    private PageableBuilder pageableBuilder;

    @Mock
    private SpecificationManager<CurrencyBalance> specificationManager;

    @InjectMocks
    private CurrencyBalanceServiceImpl currencyBalanceService;

    private User testUser;
    private CurrencyBalance testBalance;
    private CurrencyBalanceReadDto testBalanceReadDto;
    private CurrencyBalanceCreateDto testBalanceCreateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setRole(RoleName.CUSTOMER);

        testBalance = CurrencyBalance.builder()
                .id(1)
                .user(testUser)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();

        testBalanceReadDto = CurrencyBalanceReadDto.builder()
                .id(1)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();

        testBalanceCreateDto = CurrencyBalanceCreateDto.builder()
                .userId(1)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();
    }

    @Test
    void getById_ExistingBalance_ReturnsBalanceReadDto() {
        when(currencyBalanceRepository.findById(1)).thenReturn(Optional.of(testBalance));
        when(currencyBalanceMapper.toReadDto(testBalance)).thenReturn(testBalanceReadDto);

        CurrencyBalanceReadDto result = currencyBalanceService.getById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getCurrency()).isEqualTo(Currency.USD);
        verify(currencyBalanceRepository).findById(1);
        verify(currencyBalanceMapper).toReadDto(testBalance);
    }

    @Test
    void getById_NonExistentBalance_ThrowsEntityNotFoundException() {
        when(currencyBalanceRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyBalanceService.getById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Currency Balance with ID: 999 not found");

        verify(currencyBalanceRepository).findById(999);
        verify(currencyBalanceMapper, never()).toReadDto(any());
    }

    @Test
    void getByUserAndCurrency_ExistingBalance_ReturnsBalanceReadDto() {
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(testBalance));
        when(currencyBalanceMapper.toReadDto(testBalance)).thenReturn(testBalanceReadDto);

        CurrencyBalanceReadDto result = currencyBalanceService.getByUserAndCurrency(1, Currency.USD);

        assertThat(result).isNotNull();
        assertThat(result.getCurrency()).isEqualTo(Currency.USD);
        verify(currencyBalanceRepository).findByUserIdAndCurrency(1, Currency.USD);
    }

    @Test
    void getByUserAndCurrency_NonExistentBalance_ThrowsEntityNotFoundException() {
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.EUR))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyBalanceService.getByUserAndCurrency(1, Currency.EUR))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Currency Balance with userId: 1 and Currency: EUR not found");
    }

    @Test
    void getAllByUserId_ExistingBalances_ReturnsBalanceList() {
        CurrencyBalance eurBalance = CurrencyBalance.builder()
                .id(2)
                .user(testUser)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        List<CurrencyBalance> balances = List.of(testBalance, eurBalance);

        CurrencyBalanceReadDto eurBalanceDto = CurrencyBalanceReadDto.builder()
                .id(2)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        when(currencyBalanceRepository.findAllByUserId(1)).thenReturn(balances);
        when(currencyBalanceMapper.toReadDto(testBalance)).thenReturn(testBalanceReadDto);
        when(currencyBalanceMapper.toReadDto(eurBalance)).thenReturn(eurBalanceDto);

        List<CurrencyBalanceReadDto> result = currencyBalanceService.getAllByUserId(1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CurrencyBalanceReadDto::getCurrency)
                .containsExactlyInAnyOrder(Currency.USD, Currency.EUR);
        verify(currencyBalanceRepository).findAllByUserId(1);
    }

    @Test
    void getAllByUserId_NoBalances_ReturnsEmptyList() {
        when(currencyBalanceRepository.findAllByUserId(1)).thenReturn(List.of());

        List<CurrencyBalanceReadDto> result = currencyBalanceService.getAllByUserId(1);

        assertThat(result).isEmpty();
        verify(currencyBalanceRepository).findAllByUserId(1);
    }

    @Test
    void getAll_WithFilters_ReturnsPagedBalances() {
        Map<String, String> params = Map.of("currency", "USD", "page", "0", "size", "10");
        Pageable pageable = PageRequest.of(0, 10);
        List<CurrencyBalance> balances = List.of(testBalance);
        Page<CurrencyBalance> balancePage = new PageImpl<>(balances, pageable, 1);

        when(pageableBuilder.buildFromFilters(params)).thenReturn(pageable);
        when(specificationManager.get(anyString(), any())).thenReturn(mock(Specification.class));
        when(currencyBalanceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(balancePage);
        when(currencyBalanceMapper.toReadDto(testBalance)).thenReturn(testBalanceReadDto);

        PageDto<CurrencyBalanceReadDto> result = currencyBalanceService.getAll(params);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(currencyBalanceRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deposit_ExistingBalance_IncreasesAmount() {
        BalanceDepositDto depositDto = BalanceDepositDto.builder()
                .userId(1)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        CurrencyBalance updatedBalance = CurrencyBalance.builder()
                .id(1)
                .user(testUser)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1500.00))
                .build();

        CurrencyBalanceReadDto updatedDto = CurrencyBalanceReadDto.builder()
                .id(1)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1500.00))
                .build();

        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(testBalance));
        when(currencyBalanceRepository.save(testBalance)).thenReturn(updatedBalance);
        when(currencyBalanceMapper.toReadDto(updatedBalance)).thenReturn(updatedDto);

        CurrencyBalanceReadDto result = currencyBalanceService.deposit(depositDto);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
        verify(currencyBalanceRepository).save(testBalance);
    }

    @Test
    void deposit_NonExistentBalance_CreatesNewBalance() {
        BalanceDepositDto depositDto = BalanceDepositDto.builder()
                .userId(1)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        CurrencyBalance newBalance = CurrencyBalance.builder()
                .user(testUser)
                .currency(Currency.EUR)
                .amount(BigDecimal.ZERO)
                .build();

        CurrencyBalance savedBalance = CurrencyBalance.builder()
                .id(2)
                .user(testUser)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        CurrencyBalanceReadDto savedDto = CurrencyBalanceReadDto.builder()
                .id(2)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.EUR))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(currencyBalanceRepository.save(any(CurrencyBalance.class))).thenReturn(savedBalance);
        when(currencyBalanceMapper.toReadDto(savedBalance)).thenReturn(savedDto);

        CurrencyBalanceReadDto result = currencyBalanceService.deposit(depositDto);

        assertThat(result).isNotNull();
        assertThat(result.getCurrency()).isEqualTo(Currency.EUR);
        verify(userRepository).findById(1);
        verify(currencyBalanceRepository).save(any(CurrencyBalance.class));
    }

    @Test
    void deposit_NonExistentUser_ThrowsEntityNotFoundException() {
        BalanceDepositDto depositDto = BalanceDepositDto.builder()
                .userId(999)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(500.00))
                .build();

        when(currencyBalanceRepository.findByUserIdAndCurrency(999, Currency.USD))
                .thenReturn(Optional.empty());
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyBalanceService.deposit(depositDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID 999 not found");

        verify(currencyBalanceRepository, never()).save(any());
    }

    @Test
    void withdraw_SufficientFunds_DecreasesAmount() {
        BalanceWithdrawalDto withdrawalDto = BalanceWithdrawalDto.builder()
                .userId(1)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(300.00))
                .build();

        CurrencyBalance updatedBalance = CurrencyBalance.builder()
                .id(1)
                .user(testUser)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(700.00))
                .build();

        CurrencyBalanceReadDto updatedDto = CurrencyBalanceReadDto.builder()
                .id(1)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(700.00))
                .build();

        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(testBalance));
        when(currencyBalanceRepository.save(testBalance)).thenReturn(updatedBalance);
        when(currencyBalanceMapper.toReadDto(updatedBalance)).thenReturn(updatedDto);

        CurrencyBalanceReadDto result = currencyBalanceService.withdraw(withdrawalDto);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(700.00));
        verify(currencyBalanceRepository).save(testBalance);
    }

    @Test
    void withdraw_NonExistentBalance_ThrowsEntityNotFoundException() {
        BalanceWithdrawalDto withdrawalDto = BalanceWithdrawalDto.builder()
                .userId(1)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(300.00))
                .build();

        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.EUR))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyBalanceService.withdraw(withdrawalDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No balance found for user ID 1 and currency EUR");

        verify(currencyBalanceRepository, never()).save(any());
    }

    @Test
    void create_ValidBalance_ReturnsCreatedBalance() {
        CurrencyBalance newBalance = CurrencyBalance.builder()
                .user(testUser)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();

        CurrencyBalance savedBalance = CurrencyBalance.builder()
                .id(2)
                .user(testUser)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();

        CurrencyBalanceReadDto savedDto = CurrencyBalanceReadDto.builder()
                .id(2)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();

        when(currencyBalanceRepository.existsByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(currencyBalanceMapper.toEntity(testBalanceCreateDto, testUser))
                .thenReturn(newBalance);
        when(currencyBalanceRepository.save(newBalance)).thenReturn(savedBalance);
        when(currencyBalanceMapper.toReadDto(savedBalance)).thenReturn(savedDto);

        CurrencyBalanceReadDto result = currencyBalanceService.create(testBalanceCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getCurrency()).isEqualTo(Currency.USD);
        verify(currencyBalanceRepository).save(newBalance);
    }

    @Test
    void create_DuplicateBalance_ThrowsIllegalArgumentException() {
        when(currencyBalanceRepository.existsByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(true);

        assertThatThrownBy(() -> currencyBalanceService.create(testBalanceCreateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Balance already exists for user ID 1 and currency USD");

        verify(currencyBalanceRepository, never()).save(any());
    }

    @Test
    void create_NonExistentUser_ThrowsEntityNotFoundException() {
        when(currencyBalanceRepository.existsByUserIdAndCurrency(999, Currency.USD))
                .thenReturn(false);
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        CurrencyBalanceCreateDto invalidDto = CurrencyBalanceCreateDto.builder()
                .userId(999)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(1000.00))
                .build();

        assertThatThrownBy(() -> currencyBalanceService.create(invalidDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with ID 999 not found");

        verify(currencyBalanceRepository, never()).save(any());
    }

    @Test
    void deleteById_ExistingBalance_DeletesSuccessfully() {
        when(currencyBalanceRepository.existsById(1)).thenReturn(true);

        currencyBalanceService.deleteById(1);

        verify(currencyBalanceRepository).existsById(1);
        verify(currencyBalanceRepository).deleteById(1);
    }

    @Test
    void deleteById_NonExistentBalance_ThrowsEntityNotFoundException() {
        when(currencyBalanceRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> currencyBalanceService.deleteById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Balance with ID 999 not found");

        verify(currencyBalanceRepository).existsById(999);
        verify(currencyBalanceRepository, never()).deleteById(anyInt());
    }

    @Test
    void getEntityById_ExistingBalance_ReturnsBalance() {
        when(currencyBalanceRepository.findById(1)).thenReturn(Optional.of(testBalance));

        CurrencyBalance result = currencyBalanceService.getEntityById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(currencyBalanceRepository).findById(1);
    }

    @Test
    void getEntityById_NonExistentBalance_ThrowsEntityNotFoundException() {
        when(currencyBalanceRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currencyBalanceService.getEntityById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Balance with ID 999 not found");
    }
}

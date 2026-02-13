package org.example.exchangeOffice.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.model.*;
import org.example.model.dto.PageDto;
import org.example.model.dto.deal.CancelDealDto;
import org.example.model.dto.deal.DealCreateDto;
import org.example.model.dto.deal.DealReadDto;
import org.example.model.dto.deal.ResumeDealDto;
import org.example.repository.CurrencyBalanceRepository;
import org.example.repository.DayRateRepository;
import org.example.repository.DealRepository;
import org.example.repository.UserRepository;
import org.example.repository.specification.SpecificationManager;
import org.example.service.impl.DealServiceImpl;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.DealMapper;
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
public class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealMapper dealMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DayRateRepository dayRateRepository;

    @Mock
    private CurrencyBalanceRepository currencyBalanceRepository;

    @Mock
    private PageableBuilder pageableBuilder;

    @Mock
    private SpecificationManager<Deal> specificationManager;

    @InjectMocks
    private DealServiceImpl dealService;

    private User admin;
    private User customer;
    private DayRate dayRate;
    private CurrencyBalance adminUsdBalance;
    private CurrencyBalance adminEurBalance;
    private CurrencyBalance customerEurBalance;
    private CurrencyBalance customerUsdBalance;
    private Deal testDeal;
    private DealReadDto testDealReadDto;
    private DealCreateDto testDealCreateDto;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1);
        admin.setUsername("admin");
        admin.setRole(RoleName.ADMIN);

        customer = new User();
        customer.setId(2);
        customer.setUsername("customer");
        customer.setRole(RoleName.CUSTOMER);

        dayRate = new DayRate();
        dayRate.setId(1);
        dayRate.setBaseCurrency(Currency.EUR);
        dayRate.setQuoteCurrency(Currency.USD);
        dayRate.setBuyRate(BigDecimal.valueOf(1.05));
        dayRate.setSellRate(BigDecimal.valueOf(1.08));
        dayRate.setRateDate(LocalDate.now());

        adminUsdBalance = CurrencyBalance.builder()
                .id(1)
                .user(admin)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(10000.00))
                .build();

        adminEurBalance = CurrencyBalance.builder()
                .id(2)
                .user(admin)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(10000.00))
                .build();

        customerEurBalance = CurrencyBalance.builder()
                .id(3)
                .user(customer)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(5000.00))
                .build();

        customerUsdBalance = CurrencyBalance.builder()
                .id(4)
                .user(customer)
                .currency(Currency.USD)
                .amount(BigDecimal.valueOf(5000.00))
                .build();

        testDeal = new Deal();
        testDeal.setId(1);
        testDeal.setDealType(DealType.SELL);
        testDeal.setSeller(admin);
        testDeal.setBuyer(customer);
        testDeal.setSellerCurrency(Currency.USD);
        testDeal.setBuyerCurrency(Currency.EUR);
        testDeal.setSoldAmount(BigDecimal.valueOf(1000.00));
        testDeal.setPurchasedAmount(BigDecimal.valueOf(1080.00));
        testDeal.setStatus(DealStatus.COMPLETED);
        testDeal.setDayRate(dayRate);

        testDealReadDto = DealReadDto.builder()
                .id(1)
                .dealType(DealType.SELL)
                .status(DealStatus.COMPLETED)
                .build();

        testDealCreateDto = DealCreateDto.builder()
                .dealType(DealType.SELL)
                .sellerId(1)
                .buyerId(2)
                .sellerCurrency(Currency.USD)
                .buyerCurrency(Currency.EUR)
                .soldAmount(BigDecimal.valueOf(1000.00))
                .build();
    }

    @Test
    void getById_ExistingDeal_ReturnsDealReadDto() {
        when(dealRepository.findById(1)).thenReturn(Optional.of(testDeal));
        when(dealMapper.toReadDto(testDeal)).thenReturn(testDealReadDto);

        DealReadDto result = dealService.getById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(dealRepository).findById(1);
        verify(dealMapper).toReadDto(testDeal);
    }

    @Test
    void getById_NonExistentDeal_ThrowsEntityNotFoundException() {
        when(dealRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.getById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Deal with ID: 999 not found");

        verify(dealRepository).findById(999);
    }

    @Test
    void getEntityById_ExistingDeal_ReturnsDeal() {
        when(dealRepository.findById(1)).thenReturn(Optional.of(testDeal));

        Deal result = dealService.getEntityById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        verify(dealRepository).findById(1);
    }

    @Test
    void getEntityById_NonExistentDeal_ThrowsEntityNotFoundException() {
        when(dealRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.getEntityById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Deal with ID: 999 not found");
    }

    @Test
    void getAll_WithFilters_ReturnsPagedDeals() {
        Map<String, String> params = Map.of("status", "COMPLETED", "page", "0", "size", "10");
        Pageable pageable = PageRequest.of(0, 10);
        List<Deal> deals = List.of(testDeal);
        Page<Deal> dealPage = new PageImpl<>(deals, pageable, 1);

        when(pageableBuilder.buildFromFilters(params)).thenReturn(pageable);
        when(specificationManager.get(anyString(), any())).thenReturn(mock(Specification.class));
        when(dealRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(dealPage);
        when(dealMapper.toReadDto(testDeal)).thenReturn(testDealReadDto);

        PageDto<DealReadDto> result = dealService.getAll(params);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(dealRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void createDeal_SellDeal_SufficientFunds_ReturnsCompletedDeal() {
        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.EUR, Currency.USD, LocalDate.now()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(adminUsdBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(2, Currency.EUR))
                .thenReturn(Optional.of(customerEurBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.EUR))
                .thenReturn(Optional.of(adminEurBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(2, Currency.USD))
                .thenReturn(Optional.of(customerUsdBalance));
        when(dealMapper.toEntity(any(), any(), any(), any())).thenReturn(testDeal);
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);
        when(dealMapper.toReadDto(testDeal)).thenReturn(testDealReadDto);

        DealReadDto result = dealService.createDeal(testDealCreateDto);

        assertThat(result).isNotNull();
        verify(dealRepository, atLeastOnce()).save(any(Deal.class));
        verify(currencyBalanceRepository, times(4)).save(any(CurrencyBalance.class));
    }

    @Test
    void createDeal_InsufficientBuyerFunds_ReturnsPausedDeal() {
        CurrencyBalance insufficientCustomerBalance = CurrencyBalance.builder()
                .id(3)
                .user(customer)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(100.00))
                .build();

        Deal pausedDeal = new Deal();
        pausedDeal.setId(2);
        pausedDeal.setStatus(DealStatus.PAUSED);

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.EUR, Currency.USD, LocalDate.now()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(adminUsdBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(2, Currency.EUR))
                .thenReturn(Optional.of(insufficientCustomerBalance));
        when(dealMapper.toEntity(any(), any(), any(), any())).thenReturn(pausedDeal);
        when(dealRepository.save(any(Deal.class))).thenReturn(pausedDeal);
        when(dealMapper.toReadDto(pausedDeal)).thenReturn(testDealReadDto);

        DealReadDto result = dealService.createDeal(testDealCreateDto);

        assertThat(result).isNotNull();
        verify(dealRepository).save(any(Deal.class));
        verify(currencyBalanceRepository, never()).save(any(CurrencyBalance.class));
    }

    @Test
    void createDeal_NoRateAvailable_ThrowsEntityNotFoundException() {
        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.createDeal(testDealCreateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No rate found");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_SellerNotFound_ThrowsEntityNotFoundException() {
        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.EUR, Currency.USD, LocalDate.now()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.createDeal(testDealCreateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Seller with ID 1 not found");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_BuyerNotFound_ThrowsEntityNotFoundException() {
        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.EUR, Currency.USD, LocalDate.now()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.createDeal(testDealCreateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Buyer with ID 2 not found");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_BothCustomers_ThrowsIllegalArgumentException() {
        User customer2 = new User();
        customer2.setId(3);
        customer2.setRole(RoleName.CUSTOMER);

        DealCreateDto invalidDto = DealCreateDto.builder()
                .dealType(DealType.SELL)
                .sellerId(2)
                .buyerId(3)
                .sellerCurrency(Currency.USD)
                .buyerCurrency(Currency.EUR)
                .soldAmount(BigDecimal.valueOf(1000.00))
                .build();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                any(), any(), any()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));
        when(userRepository.findById(3)).thenReturn(Optional.of(customer2));

        assertThatThrownBy(() -> dealService.createDeal(invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deals must be between one ADMIN and one CUSTOMER");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_BothAdmins_ThrowsIllegalArgumentException() {
        User admin2 = new User();
        admin2.setId(3);
        admin2.setRole(RoleName.ADMIN);

        DealCreateDto invalidDto = DealCreateDto.builder()
                .dealType(DealType.SELL)
                .sellerId(1)
                .buyerId(3)
                .sellerCurrency(Currency.USD)
                .buyerCurrency(Currency.EUR)
                .soldAmount(BigDecimal.valueOf(1000.00))
                .build();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                any(), any(), any()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(3)).thenReturn(Optional.of(admin2));

        assertThatThrownBy(() -> dealService.createDeal(invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deals must be between one ADMIN and one CUSTOMER");
    }

    @Test
    void createDeal_SellDealCustomerAsSeller_ThrowsIllegalArgumentException() {
        DealCreateDto invalidDto = DealCreateDto.builder()
                .dealType(DealType.SELL)
                .sellerId(2)
                .buyerId(1)
                .sellerCurrency(Currency.USD)
                .buyerCurrency(Currency.EUR)
                .soldAmount(BigDecimal.valueOf(1000.00))
                .build();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                any(), any(), any()))
                .thenReturn(Optional.of(dayRate));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> dealService.createDeal(invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SELL deal: Admin must be the seller");
    }

    @Test
    void createDeal_BuyDealCustomerAsBuyer_ThrowsIllegalArgumentException() {
        DealCreateDto buyDealDto = DealCreateDto.builder()
                .dealType(DealType.BUY)
                .sellerId(1)
                .buyerId(2)
                .sellerCurrency(Currency.EUR)
                .buyerCurrency(Currency.USD)
                .purchasedAmount(BigDecimal.valueOf(100.00))
                .build();

        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.EUR, Currency.USD, LocalDate.now()))
                .thenReturn(Optional.of(dayRate));

        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> dealService.createDeal(buyDealDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BUY deal: Admin must be the buyer");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void resumeDeal_PausedDeal_SufficientFunds_CompletesSuccessfully() {
        Deal pausedDeal = new Deal();
        pausedDeal.setId(1);
        pausedDeal.setDealType(DealType.SELL);
        pausedDeal.setSeller(admin);
        pausedDeal.setBuyer(customer);
        pausedDeal.setSellerCurrency(Currency.USD);
        pausedDeal.setBuyerCurrency(Currency.EUR);
        pausedDeal.setSoldAmount(BigDecimal.valueOf(1000.00));
        pausedDeal.setPurchasedAmount(BigDecimal.valueOf(1080.00));
        pausedDeal.setStatus(DealStatus.PAUSED);
        pausedDeal.setDayRate(dayRate);

        ResumeDealDto resumeDto = ResumeDealDto.builder()
                .resumeReason("Funds deposited")
                .build();

        when(dealRepository.findById(1)).thenReturn(Optional.of(pausedDeal));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));
        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                Currency.EUR, Currency.USD, LocalDate.now()))
                .thenReturn(Optional.of(dayRate));
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(adminUsdBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(2, Currency.EUR))
                .thenReturn(Optional.of(customerEurBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.EUR))
                .thenReturn(Optional.of(adminEurBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(2, Currency.USD))
                .thenReturn(Optional.of(customerUsdBalance));
        when(dealRepository.save(any(Deal.class))).thenReturn(pausedDeal);
        when(dealMapper.toReadDto(pausedDeal)).thenReturn(testDealReadDto);

        DealReadDto result = dealService.resumeDeal(1, resumeDto);

        assertThat(result).isNotNull();
        verify(dealRepository, atLeast(1)).save(any(Deal.class));
        verify(currencyBalanceRepository, times(4)).save(any(CurrencyBalance.class));
    }

    @Test
    void resumeDeal_CompletedDeal_ThrowsIllegalStateException() {
        ResumeDealDto resumeDto = ResumeDealDto.builder()
                .resumeReason("Test")
                .build();

        when(dealRepository.findById(1)).thenReturn(Optional.of(testDeal));

        assertThatThrownBy(() -> dealService.resumeDeal(1, resumeDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PAUSED or FAILED deals can be resumed");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void resumeDeal_PausedDeal_StillInsufficientFunds_RemainsPaused() {
        Deal pausedDeal = new Deal();
        pausedDeal.setId(1);
        pausedDeal.setDealType(DealType.SELL);
        pausedDeal.setSeller(admin);
        pausedDeal.setBuyer(customer);
        pausedDeal.setSellerCurrency(Currency.USD);
        pausedDeal.setBuyerCurrency(Currency.EUR);
        pausedDeal.setSoldAmount(BigDecimal.valueOf(1000.00));
        pausedDeal.setPurchasedAmount(BigDecimal.valueOf(1080.00));
        pausedDeal.setStatus(DealStatus.PAUSED);
        pausedDeal.setDayRate(dayRate);

        CurrencyBalance insufficientBalance = CurrencyBalance.builder()
                .id(3)
                .user(customer)
                .currency(Currency.EUR)
                .amount(BigDecimal.valueOf(100.00))
                .build();

        ResumeDealDto resumeDto = ResumeDealDto.builder()
                .resumeReason("Attempting resume")
                .build();

        when(dealRepository.findById(1)).thenReturn(Optional.of(pausedDeal));
        when(userRepository.findById(1)).thenReturn(Optional.of(admin));
        when(userRepository.findById(2)).thenReturn(Optional.of(customer));
        when(dayRateRepository.findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                any(), any(), any()))
                .thenReturn(Optional.of(dayRate));
        when(currencyBalanceRepository.findByUserIdAndCurrency(1, Currency.USD))
                .thenReturn(Optional.of(adminUsdBalance));
        when(currencyBalanceRepository.findByUserIdAndCurrency(2, Currency.EUR))
                .thenReturn(Optional.of(insufficientBalance));
        when(dealRepository.save(any(Deal.class))).thenReturn(pausedDeal);
        when(dealMapper.toReadDto(pausedDeal)).thenReturn(testDealReadDto);

        DealReadDto result = dealService.resumeDeal(1, resumeDto);

        assertThat(result).isNotNull();
        verify(dealRepository, atLeast(1)).save(any(Deal.class));
        verify(currencyBalanceRepository, never()).save(any(CurrencyBalance.class));
    }

    @Test
    void cancelDeal_PausedDeal_CancelsSuccessfully() {
        Deal pausedDeal = new Deal();
        pausedDeal.setId(1);
        pausedDeal.setStatus(DealStatus.PAUSED);

        CancelDealDto cancelDto = CancelDealDto.builder()
                .cancellationReason("Customer changed mind")
                .build();

        when(dealRepository.findById(1)).thenReturn(Optional.of(pausedDeal));
        when(dealRepository.save(pausedDeal)).thenReturn(pausedDeal);
        when(dealMapper.toReadDto(pausedDeal)).thenReturn(testDealReadDto);

        DealReadDto result = dealService.cancelDeal(1, cancelDto);

        assertThat(result).isNotNull();
        verify(dealRepository).save(pausedDeal);
    }

    @Test
    void cancelDeal_CompletedDeal_ThrowsIllegalStateException() {
        CancelDealDto cancelDto = CancelDealDto.builder()
                .cancellationReason("Test")
                .build();

        when(dealRepository.findById(1)).thenReturn(Optional.of(testDeal));

        assertThatThrownBy(() -> dealService.cancelDeal(1, cancelDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel a completed deal");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void cancelDeal_AlreadyCancelled_ThrowsIllegalStateException() {
        Deal cancelledDeal = new Deal();
        cancelledDeal.setId(1);
        cancelledDeal.setStatus(DealStatus.CANCELLED);

        CancelDealDto cancelDto = CancelDealDto.builder()
                .cancellationReason("Test")
                .build();

        when(dealRepository.findById(1)).thenReturn(Optional.of(cancelledDeal));

        assertThatThrownBy(() -> dealService.cancelDeal(1, cancelDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Deal is already cancelled");

        verify(dealRepository, never()).save(any());
    }

    @Test
    void deleteById_ExistingDeal_DeletesSuccessfully() {
        when(dealRepository.existsById(1)).thenReturn(true);

        dealService.deleteById(1);

        verify(dealRepository).existsById(1);
        verify(dealRepository).deleteById(1);
    }

    @Test
    void deleteById_NonExistentDeal_ThrowsEntityNotFoundException() {
        when(dealRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> dealService.deleteById(999))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Deal with ID 999 not found");

        verify(dealRepository).existsById(999);
        verify(dealRepository, never()).deleteById(anyInt());
    }
}

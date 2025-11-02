package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
import org.example.service.DealService;
import org.example.utils.mapper.DealMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final DealMapper dealMapper;
    private final UserRepository userRepository;
    private final DayRateRepository dayRateRepository;
    private final CurrencyBalanceRepository currencyBalanceRepository;

    @Override
    @Transactional(readOnly = true)
    public DealReadDto getById(Integer id) {
        return dealRepository.findById(id)
                .map(dealMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("Deal with ID: " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Deal getEntityById(Integer id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Deal with ID: " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<DealReadDto> getAll(Pageable pageable) {
        Page<Deal> dealPage = dealRepository.findAll(pageable);
        List<DealReadDto> dealReadDtos = dealPage.stream()
                .map(dealMapper::toReadDto)
                .toList();

        return PageDto.<DealReadDto>builder()
                .content(dealReadDtos)
                .pageNumber(dealPage.getNumber())
                .pageSize(dealPage.getSize())
                .totalElements(dealPage.getTotalElements())
                .totalPages(dealPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public DealReadDto createDeal(DealCreateDto dto) {
        DayRate currentRate = fetchAndValidateDayRate(dto);
        BigDecimal exchangeRate = selectExchangeRate(currentRate, dto.getDealType());

        User seller = fetchUser(dto.getSellerId(), "Seller");
        User buyer = fetchUser(dto.getBuyerId(), "Buyer");
        validateRoles(seller, buyer, dto.getDealType());

        AmountPair amounts = calculateAmounts(dto, exchangeRate);

        CurrencyBalance sellerBalance = fetchBalance(dto.getSellerId(), dto.getSellerCurrency(), "Seller");
        CurrencyBalance buyerBalance = fetchBalance(dto.getBuyerId(), dto.getBuyerCurrency(), "Buyer");

        BalanceSufficiency sufficiency = checkBalanceSufficiency(
                sellerBalance, buyerBalance, amounts.soldAmount(), amounts.purchasedAmount());

        Deal deal = buildDeal(dto, seller, buyer, currentRate, exchangeRate, amounts, sufficiency);
        Deal savedDeal = dealRepository.save(deal);

        if (sufficiency.bothSufficient()) {
            executeDeal(
                    seller.getId(),
                    buyer.getId(),
                    dto.getSellerCurrency(),
                    dto.getBuyerCurrency(),
                    amounts);

            savedDeal.setStatus(DealStatus.COMPLETED);
            savedDeal.setCompletedAt(LocalDateTime.now());
            savedDeal.setStatusReason(null);
            savedDeal = dealRepository.save(savedDeal);
        }

        return dealMapper.toReadDto(savedDeal);
    }

    @Override
    @Transactional
    public DealReadDto resumeDeal(Integer dealId, ResumeDealDto dto) {
        return null;
    }

    @Override
    @Transactional
    public DealReadDto cancelDeal(Integer dealId, CancelDealDto dto) {
        return null;
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!dealRepository.existsById(id)) {
            throw new EntityNotFoundException("Deal with ID " + id + " not found");
        }
        dealRepository.deleteById(id);
    }

    // Fetches the appropriate DayRate for the deal based on deal type and currencies.
    private DayRate fetchAndValidateDayRate(DealCreateDto dto) {
        Currency baseCurrency;
        Currency quoteCurrency;

        if (dto.getDealType() == DealType.BUY) {
            baseCurrency = dto.getSellerCurrency();   // Bank buying this
            quoteCurrency = dto.getBuyerCurrency(); // Bank paying with this
        } else {
            baseCurrency = dto.getBuyerCurrency();  // Bank selling this
            quoteCurrency = dto.getSellerCurrency();  // Customer paying with this
        }

        LocalDate today = LocalDate.now();
        return dayRateRepository
                .findByBaseCurrencyAndQuoteCurrencyAndRateDate(baseCurrency, quoteCurrency, today)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No rate found for " + baseCurrency + "/" + quoteCurrency + " on " + today));
    }

    // Selects buy or sell rate based on deal type.
    private BigDecimal selectExchangeRate(DayRate rate, DealType dealType) {
        return dealType == DealType.BUY ? rate.getBuyRate() : rate.getSellRate();
    }

    //Fetches a user by ID with descriptive error message.
    private User fetchUser(Integer userId, String userType) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        userType + " with ID " + userId + " not found"));
    }

    /* Validates that one participant is ADMIN and one is CUSTOMER,
     and that the admin is on the correct side based on deal type. */
    private void validateRoles(User seller, User buyer, DealType dealType) {
        boolean sellerIsAdmin = seller.getRole() == RoleName.ADMIN;
        boolean buyerIsAdmin = buyer.getRole() == RoleName.ADMIN;

        if (sellerIsAdmin == buyerIsAdmin) {
            throw new IllegalArgumentException(
                    "Deals must be between one ADMIN and one CUSTOMER. " +
                            "Found: Seller=" + seller.getRole() + ", Buyer=" + buyer.getRole());
        }

        if (dealType == DealType.BUY && !buyerIsAdmin) {
            throw new IllegalArgumentException(
                    "BUY deal: Admin must be the buyer");
        }

        if (dealType == DealType.SELL && !sellerIsAdmin) {
            throw new IllegalArgumentException(
                    "SELL deal: Admin must be the seller");
        }
    }

    /* Calculates both sold and purchased amounts based on deal type.
     * For BUY: client provides purchased amount, calculate sold amount.
     * For SELL: client provides sold amount, calculate purchased amount. */
    private AmountPair calculateAmounts(DealCreateDto dto, BigDecimal exchangeRate) {
        BigDecimal purchasedAmount;
        BigDecimal soldAmount;

        if (dto.getDealType() == DealType.BUY) {
            purchasedAmount = dto.getPurchasedAmount(); // Base amount (provided)
            soldAmount = purchasedAmount
                    .multiply(exchangeRate)
                    .setScale(4, RoundingMode.HALF_UP); // Quote amount (calculated)
        } else {
            soldAmount = dto.getSoldAmount(); // Base amount (provided)
            purchasedAmount = soldAmount
                    .multiply(exchangeRate)
                    .setScale(4, RoundingMode.HALF_UP); // Quote amount (calculated)
        }

        return new AmountPair(soldAmount, purchasedAmount);
    }

    // Fetches a currency balance with descriptive error message.
    private CurrencyBalance fetchBalance(Integer userId, Currency currency, String userType) {
        return currencyBalanceRepository
                .findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new EntityNotFoundException(
                        userType + "'s balance for currency " + currency + " not found"));
    }

    // Checks if both parties have sufficient funds and returns detailed result.
    private BalanceSufficiency checkBalanceSufficiency(
            CurrencyBalance sellerBalance,
            CurrencyBalance buyerBalance,
            BigDecimal soldAmount,
            BigDecimal purchasedAmount) {

        boolean sellerSufficient = sellerBalance.getAmount().compareTo(soldAmount) >= 0;
        boolean buyerSufficient = buyerBalance.getAmount().compareTo(purchasedAmount) >= 0;
        boolean bothSufficient = sellerSufficient && buyerSufficient;
        DealStatus status;

        if (bothSufficient) {
            status = DealStatus.COMPLETED;
        } else {
            status = DealStatus.PAUSED;
        }
        String reason = getStatusReason(sellerSufficient, buyerSufficient);

        return new BalanceSufficiency(status, reason, sellerSufficient, buyerSufficient, bothSufficient);
    }

    // Generates appropriate status reason based on which party lacks funds.
    private String getStatusReason(boolean sellerSufficient, boolean buyerSufficient) {
        if (sellerSufficient && buyerSufficient) {
            return null;
        }
        if (!sellerSufficient && !buyerSufficient) {
            return "Insufficient funds: both parties";
        }
        return !sellerSufficient ?
                "Insufficient seller balance" :
                "Insufficient buyer balance";
    }

    // Builds the Deal entity with all calculated values.
    private Deal buildDeal(
            DealCreateDto dto,
            User seller,
            User buyer,
            DayRate currentRate,
            BigDecimal exchangeRate,
            AmountPair amounts,
            BalanceSufficiency sufficiency) {

        Deal deal = dealMapper.toEntity(dto, seller, buyer, currentRate);
        deal.setSoldAmount(amounts.soldAmount());
        deal.setPurchasedAmount(amounts.purchasedAmount());
        deal.setExchangeRateUsed(exchangeRate);
        deal.setStatus(sufficiency.status());
        deal.setStatusReason(sufficiency.reason());

        if (sufficiency.status() == DealStatus.PAUSED) {
            deal.setPausedAt(LocalDateTime.now());
        }

        return deal;
    }

    // Conducts the actual deal
    private void executeDeal(Integer sellerId,
                             Integer buyerId,
                             Currency sellerCurrency,
                             Currency buyerCurrency,
                             AmountPair amounts) {

        CurrencyBalance sellerDebitBalance = fetchBalance(sellerId, sellerCurrency, "Seller");
        sellerDebitBalance.debit(amounts.soldAmount());
        currencyBalanceRepository.save(sellerDebitBalance);

        CurrencyBalance sellerCreditBalance = getOrCreateBalance(sellerId, buyerCurrency);
        sellerCreditBalance.credit(amounts.purchasedAmount());
        currencyBalanceRepository.save(sellerCreditBalance);

        CurrencyBalance buyerDebitBalance  = fetchBalance(buyerId, buyerCurrency, "Buyer");
        buyerDebitBalance.debit(amounts.purchasedAmount());
        currencyBalanceRepository.save(buyerDebitBalance);

        CurrencyBalance buyerCreditBalance = getOrCreateBalance(buyerId, sellerCurrency);
        buyerCreditBalance.credit(amounts.soldAmount());
        currencyBalanceRepository.save(buyerCreditBalance);
    }

    private CurrencyBalance getOrCreateBalance(Integer userId, Currency currency) {
        return currencyBalanceRepository
                .findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    CurrencyBalance newBalance = CurrencyBalance.builder()
                            .user(user)
                            .currency(currency)
                            .amount(BigDecimal.ZERO)
                            .build();
                    return currencyBalanceRepository.save(newBalance);
                });
    }

    private record AmountPair(BigDecimal soldAmount, BigDecimal purchasedAmount) {}

    private record BalanceSufficiency(
            DealStatus status,
            String reason,
            boolean sellerSufficient,
            boolean buyerSufficient,
            boolean bothSufficient
    ) {}

}

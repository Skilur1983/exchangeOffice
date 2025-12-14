package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
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
import org.example.repository.specification.SpecificationManager;
import org.example.service.DealService;
import org.example.utils.PageableBuilder;
import org.example.utils.mapper.DealMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final DealMapper dealMapper;
    private final UserRepository userRepository;
    private final DayRateRepository dayRateRepository;
    private final CurrencyBalanceRepository currencyBalanceRepository;
    private final PageableBuilder pageableBuilder;
    private final SpecificationManager<Deal> specificationManager;

    private static final String SPLIT_TO_ARRAY = ",";
    private static final int AMOUNT_SCALE = 4;
    private static final RoundingMode AMOUNT_ROUNDING = RoundingMode.HALF_UP;

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
    public PageDto<DealReadDto> getAll(Map<String, String> params) {
        Pageable pageRequest = pageableBuilder.buildFromFilters(params);
        Specification<Deal> specification = null;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            Specification<Deal> sp = specificationManager.get(entry.getKey(), entry.getValue().split(SPLIT_TO_ARRAY));
            specification = specification == null ? Specification.where(sp) : specification.and(sp);
        }

        Page<Deal> dealPage = dealRepository.findAll(specification, pageRequest);

        List<DealReadDto> dealReadDtos = dealPage.getContent().stream()
                .map(dealMapper::toReadDto)
                .collect(Collectors.toList());

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
            try {
                executeDeal(
                        seller.getId(),
                        buyer.getId(),
                        dto.getSellerCurrency(),
                        dto.getBuyerCurrency(),
                        amounts);

                savedDeal.setStatus(DealStatus.COMPLETED);
                savedDeal.setCompletedAt(LocalDateTime.now());
                savedDeal.setStatusReason(null);
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                savedDeal.setStatus(DealStatus.FAILED);
                savedDeal.setStatusReason("Execution failed: " + e.getMessage());
            }
            savedDeal = dealRepository.save(savedDeal);
        }

        return dealMapper.toReadDto(savedDeal);
    }

    @Override
    @Transactional
    public DealReadDto resumeDeal(Integer dealId, ResumeDealDto dto) {
        Deal deal = getEntityById(dealId);
        validateResumableStatus(deal);

        DealStatus originalStatus = deal.getStatus();

        // Validate prerequisites - cancel if validation fails
        if (!validateDealPrerequisites(deal)) {
            Deal savedDeal = dealRepository.save(deal);
            return dealMapper.toReadDto(savedDeal);
        }

        // Update exchange rate for PAUSED deals - return early if no rate available
        if (originalStatus == DealStatus.PAUSED && !updateDealWithCurrentRate(deal)) {
            Deal savedDeal = dealRepository.save(deal);
            return dealMapper.toReadDto(savedDeal);
        }

        // Check balance sufficiency
        CurrencyBalance sellerBalance = fetchBalance(
                deal.getSeller().getId(),
                deal.getSellerCurrency(),
                "Seller"
        );
        CurrencyBalance buyerBalance = fetchBalance(
                deal.getBuyer().getId(),
                deal.getBuyerCurrency(),
                "Buyer"
        );

        BalanceSufficiency sufficiency = checkBalanceSufficiency(
                sellerBalance,
                buyerBalance,
                deal.getSoldAmount(),
                deal.getPurchasedAmount()
        );

        // Save deal before attempting execution
        Deal savedDeal = dealRepository.save(deal);

        if (sufficiency.bothSufficient()) {
            try {
                executeDeal(
                        deal.getSeller().getId(),
                        deal.getBuyer().getId(),
                        deal.getSellerCurrency(),
                        deal.getBuyerCurrency(),
                        new AmountPair(deal.getSoldAmount(), deal.getPurchasedAmount())
                );

                savedDeal.setStatus(DealStatus.COMPLETED);
                savedDeal.setCompletedAt(LocalDateTime.now());
                savedDeal.setStatusReason(dto.getResumeReason());
                savedDeal.setPausedAt(null);
            } catch (OptimisticLockException | DataIntegrityViolationException e) {
                savedDeal.setStatus(DealStatus.FAILED);
                savedDeal.setStatusReason("Execution failed on resume: " + e.getMessage());
            }
            savedDeal = dealRepository.save(savedDeal);
        } else {
            handleInsufficientBalance(savedDeal, originalStatus, sufficiency);
            savedDeal = dealRepository.save(savedDeal);
        }

        return dealMapper.toReadDto(savedDeal);
    }

    @Override
    @Transactional
    public DealReadDto cancelDeal(Integer dealId, CancelDealDto dto) {
        Deal deal = getEntityById(dealId);

        if (deal.getStatus() == DealStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Cannot cancel a completed deal");
        }
        if (deal.getStatus() == DealStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Deal is already cancelled");
        }

        deal.setStatus(DealStatus.CANCELLED);
        deal.setStatusReason(dto.getCancellationReason());

        Deal savedDeal = dealRepository.save(deal);
        return dealMapper.toReadDto(savedDeal);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!dealRepository.existsById(id)) {
            throw new EntityNotFoundException("Deal with ID " + id + " not found");
        }
        dealRepository.deleteById(id);
    }

    private DayRate fetchAndValidateDayRate(DealCreateDto dto) {
        return fetchDayRateForDealType(
                dto.getDealType(),
                dto.getSellerCurrency(),
                dto.getBuyerCurrency()
        );
    }

    private DayRate fetchCurrentDayRate(Deal deal) {
        return fetchDayRateForDealType(
                deal.getDealType(),
                deal.getSellerCurrency(),
                deal.getBuyerCurrency()
        );
    }

    private DayRate fetchDayRateForDealType(
            DealType dealType,
            Currency sellerCurrency,
            Currency buyerCurrency) {

        Currency baseCurrency = (dealType == DealType.BUY)
                ? sellerCurrency
                : buyerCurrency;
        Currency quoteCurrency = (dealType == DealType.BUY)
                ? buyerCurrency
                : sellerCurrency;

        LocalDate today = LocalDate.now();
        return dayRateRepository
                .findByBaseCurrencyAndQuoteCurrencyAndRateDate(
                        baseCurrency, quoteCurrency, today)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No rate found for " + baseCurrency + "/"
                                + quoteCurrency + " on " + today));
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
        if (dto.getDealType() == DealType.BUY) {
            return calculateAmountsFromPurchased(
                    dto.getPurchasedAmount(), exchangeRate);
        } else {
            return calculateAmountsFromSold(
                    dto.getSoldAmount(), exchangeRate);
        }
    }

    private AmountPair recalculateAmounts(Deal deal, BigDecimal exchangeRate) {
        if (deal.getDealType() == DealType.BUY) {
            return calculateAmountsFromPurchased(
                    deal.getPurchasedAmount(), exchangeRate);
        } else {
            return calculateAmountsFromSold(
                    deal.getSoldAmount(), exchangeRate);
        }
    }

    private AmountPair calculateAmountsFromPurchased(
            BigDecimal purchasedAmount, BigDecimal exchangeRate) {
        BigDecimal soldAmount = purchasedAmount
                .multiply(exchangeRate)
                .setScale(AMOUNT_SCALE, AMOUNT_ROUNDING);
        return new AmountPair(soldAmount, purchasedAmount);
    }

    private AmountPair calculateAmountsFromSold(
            BigDecimal soldAmount, BigDecimal exchangeRate) {
        BigDecimal purchasedAmount = soldAmount
                .multiply(exchangeRate)
                .setScale(AMOUNT_SCALE, AMOUNT_ROUNDING);
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

    private void validateResumableStatus(Deal deal) {
        if (deal.getStatus() != DealStatus.PAUSED && deal.getStatus() != DealStatus.FAILED) {
            throw new IllegalStateException("Only PAUSED or FAILED deals can be resumed");
        }
    }

    private boolean validateDealPrerequisites(Deal deal) {
        try {
            User seller = fetchUser(deal.getSeller().getId(), "Seller");
            User buyer = fetchUser(deal.getBuyer().getId(), "Buyer");
            validateRoles(seller, buyer, deal.getDealType());

            fetchBalance(deal.getSeller().getId(), deal.getSellerCurrency(), "Seller");
            fetchBalance(deal.getBuyer().getId(), deal.getBuyerCurrency(), "Buyer");

            return true;
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            deal.setStatus(DealStatus.CANCELLED);
            deal.setStatusReason("Validation failed: " + e.getMessage());
            return false;
        }
    }

    private boolean updateDealWithCurrentRate(Deal deal) {
        try {
            DayRate currentRate = fetchCurrentDayRate(deal);
            BigDecimal exchangeRate = selectExchangeRate(currentRate, deal.getDealType());
            AmountPair amounts = recalculateAmounts(deal, exchangeRate);

            deal.setDayRate(currentRate);
            deal.setExchangeRateUsed(exchangeRate);
            deal.setSoldAmount(amounts.soldAmount());
            deal.setPurchasedAmount(amounts.purchasedAmount());

            return true;
        } catch (EntityNotFoundException e) {
            deal.setStatus(DealStatus.CANCELLED);
            deal.setStatusReason("No day rate available for resume");
            return false;
        }
    }

    private void handleInsufficientBalance(Deal deal, DealStatus originalStatus, BalanceSufficiency sufficiency) {
        if (originalStatus == DealStatus.FAILED) {
            deal.setStatusReason("Still insufficient balance after resume attempt");
        } else {
            deal.setStatus(DealStatus.PAUSED);
            deal.setStatusReason(sufficiency.reason());
        }
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

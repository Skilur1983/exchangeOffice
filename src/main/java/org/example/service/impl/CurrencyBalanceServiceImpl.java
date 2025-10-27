package org.example.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.model.Currency;
import org.example.model.CurrencyBalance;
import org.example.model.User;
import org.example.model.dto.PageDto;
import org.example.model.dto.currencybalance.BalanceDepositDto;
import org.example.model.dto.currencybalance.BalanceWithdrawalDto;
import org.example.model.dto.currencybalance.CurrencyBalanceCreateDto;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.repository.CurrencyBalanceRepository;
import org.example.repository.UserRepository;
import org.example.service.CurrencyBalanceService;
import org.example.utils.mapper.CurrencyBalanceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyBalanceServiceImpl implements CurrencyBalanceService {

    private final CurrencyBalanceRepository currencyBalanceRepository;
    private final UserRepository userRepository;
    private final CurrencyBalanceMapper currencyBalanceMapper;

    @Override
    @Transactional(readOnly = true)
    public CurrencyBalanceReadDto getById(Integer id) {
        return currencyBalanceRepository.findById(id)
                .map(currencyBalanceMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("Currency Balance with ID: " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyBalanceReadDto getByUserAndCurrency(Integer userId, Currency currency) {
        return currencyBalanceRepository.findByUserIdAndCurrency(userId, currency)
                .map(currencyBalanceMapper::toReadDto)
                .orElseThrow(() -> new EntityNotFoundException("Currency Balance with userId: " + userId + " and Currency: " + currency.name() + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyBalanceReadDto> getAllByUserId(Integer userId) {
        return currencyBalanceRepository.findAllByUserId(userId)
                .stream()
                .map(currencyBalanceMapper::toReadDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<CurrencyBalanceReadDto> getAll(Pageable pageable) {
        Page<CurrencyBalance> currencyBalancePage = currencyBalanceRepository.findAll(pageable);
        List<CurrencyBalanceReadDto> currencyBalanceReadDtos = currencyBalancePage.stream()
                .map(currencyBalanceMapper::toReadDto)
                .toList();

        return PageDto.<CurrencyBalanceReadDto>builder()
                .content(currencyBalanceReadDtos)
                .pageNumber(currencyBalancePage.getNumber())
                .pageSize(currencyBalancePage.getSize())
                .totalElements(currencyBalancePage.getTotalElements())
                .totalPages(currencyBalancePage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public CurrencyBalanceReadDto deposit(BalanceDepositDto dto) {
        CurrencyBalance balance = currencyBalanceRepository
                .findByUserIdAndCurrency(dto.getUserId(), dto.getCurrency())
                .orElseGet(() -> createNewBalance(dto.getUserId(), dto.getCurrency()));

        balance.credit(dto.getAmount());

        CurrencyBalance savedBalance = currencyBalanceRepository.save(balance);
        return currencyBalanceMapper.toReadDto(savedBalance);
    }

    @Override
    @Transactional
    public CurrencyBalanceReadDto withdraw(BalanceWithdrawalDto dto) {
        CurrencyBalance balance = currencyBalanceRepository
                .findByUserIdAndCurrency(dto.getUserId(), dto.getCurrency())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No balance found for user ID " + dto.getUserId() + " and currency " + dto.getCurrency()));

        balance.debit(dto.getAmount());

        CurrencyBalance savedBalance = currencyBalanceRepository.save(balance);
        return currencyBalanceMapper.toReadDto(savedBalance);
    }

    @Override
    @Transactional
    public CurrencyBalanceReadDto create(CurrencyBalanceCreateDto dto) {
        if (currencyBalanceRepository.existsByUserIdAndCurrency(dto.getUserId(), dto.getCurrency())) {
            throw new IllegalArgumentException("Balance already exists for user ID " + dto.getUserId() + " and currency " + dto.getCurrency());
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + dto.getUserId() + " not found"));

        CurrencyBalance balance = currencyBalanceMapper.toEntity(dto, user);

        CurrencyBalance savedBalance = currencyBalanceRepository.save(balance);
        return currencyBalanceMapper.toReadDto(savedBalance);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        if (!currencyBalanceRepository.existsById(id)) {
            throw new EntityNotFoundException("Balance with ID " + id + " not found");
        }
        currencyBalanceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyBalance getEntityById(Integer id) {
        return currencyBalanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Balance with ID " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyBalance getEntityByUserAndCurrency(Integer userId, Currency currency) {
        return currencyBalanceRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new EntityNotFoundException("Balance not found for user ID " + userId + " and currency " + currency));
    }

    private CurrencyBalance createNewBalance(Integer userId, Currency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        return CurrencyBalance.builder()
                .user(user)
                .currency(currency)
                .amount(BigDecimal.ZERO)
                .build();
    }
}

package org.example.service;

import org.example.model.Currency;
import org.example.model.CurrencyBalance;
import org.example.model.dto.PageDto;
import org.example.model.dto.currencybalance.BalanceDepositDto;
import org.example.model.dto.currencybalance.BalanceWithdrawalDto;
import org.example.model.dto.currencybalance.CurrencyBalanceCreateDto;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CurrencyBalanceService {

    CurrencyBalanceReadDto getById(Integer id);
    CurrencyBalanceReadDto getByUserAndCurrency(Integer userId, Currency currency);
    List<CurrencyBalanceReadDto> getAllByUserId(Integer userId);
    PageDto<CurrencyBalanceReadDto> getAll(Pageable pageable);

    CurrencyBalanceReadDto deposit(BalanceDepositDto dto);
    CurrencyBalanceReadDto withdraw(BalanceWithdrawalDto dto);

    CurrencyBalanceReadDto create(CurrencyBalanceCreateDto dto);
    void deleteById(Integer id);

    CurrencyBalance getEntityById(Integer id);
    CurrencyBalance getEntityByUserAndCurrency(Integer userId, Currency currency);
}

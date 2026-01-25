package org.example.service;

import org.example.model.Currency;
import org.example.model.CurrencyBalance;
import org.example.model.dto.PageDto;
import org.example.model.dto.currencybalance.BalanceDepositDto;
import org.example.model.dto.currencybalance.BalanceWithdrawalDto;
import org.example.model.dto.currencybalance.CurrencyBalanceCreateDto;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;

import java.util.List;
import java.util.Map;

public interface CurrencyBalanceService {

    CurrencyBalanceReadDto getById(Integer id);
    CurrencyBalanceReadDto getByUserAndCurrency(Integer userId, Currency currency);
    List<CurrencyBalanceReadDto> getAllByUserId(Integer userId);
    PageDto<CurrencyBalanceReadDto> getAll(Map<String, String> params);

    CurrencyBalanceReadDto deposit(BalanceDepositDto dto);
    CurrencyBalanceReadDto withdraw(BalanceWithdrawalDto dto);

    CurrencyBalanceReadDto create(CurrencyBalanceCreateDto dto);
    void deleteById(Integer id);

    CurrencyBalance getEntityById(Integer id);
}

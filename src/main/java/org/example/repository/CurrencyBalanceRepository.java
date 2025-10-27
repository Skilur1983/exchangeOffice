package org.example.repository;

import org.example.model.Currency;
import org.example.model.CurrencyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyBalanceRepository extends JpaRepository<CurrencyBalance, Integer> {

    Optional<CurrencyBalance> findByUserIdAndCurrency(Integer user_id, Currency currency);
    List<CurrencyBalance> findAllByUserId(Integer userId);
    boolean existsByUserIdAndCurrency(Integer userId, Currency currency);
}

package org.example.repository;

import org.example.model.CurrencyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyBalanceRepository extends JpaRepository<CurrencyBalance, Integer> {

    Optional<CurrencyBalance> findBySymbol(String symbol);
}

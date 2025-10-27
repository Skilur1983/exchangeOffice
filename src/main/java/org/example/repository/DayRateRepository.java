package org.example.repository;

import org.example.model.Currency;
import org.example.model.DayRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayRateRepository extends JpaRepository<DayRate, Integer> {

    Optional<DayRate> findByBaseCurrencyAndQuoteCurrencyAndRateDate(
            Currency baseCurrency,
            Currency quoteCurrency,
            LocalDate date);
    List<DayRate> findAllByBaseCurrencyAndQuoteCurrencyAndRateDateBetween(
            Currency baseCurrency,
            Currency quoteCurrency,
            LocalDate startDate,
            LocalDate endDate
    );
    boolean existsByBaseCurrencyAndQuoteCurrencyAndRateDate(
            Currency baseCurrency,
            Currency quoteCurrency,
            LocalDate rateDate
    );
}

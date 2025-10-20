package org.example.service;

import org.example.model.Currency;
import org.example.model.DealStatus;
import org.example.model.dto.deal.DealReadDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DealAnalyticsService {

    BigDecimal getTotalVolumeByUser(Integer userId);
    BigDecimal getTotalVolumeByStatus(DealStatus status);
    BigDecimal getTotalVolumeByDateRange(LocalDate start, LocalDate end);

    Map<Currency, BigDecimal> getVolumesByCurrency(Integer userId);
    List<DealReadDto> getRecentDeals(Integer userId, int limit);
}

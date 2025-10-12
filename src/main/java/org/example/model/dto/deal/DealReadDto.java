package org.example.model.dto.deal;

import lombok.*;
import org.example.model.DealType;
import org.example.model.dto.currencybalance.CurrencyBalanceReadDto;
import org.example.model.dto.dayrate.DayRateReadDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealReadDto {

    private Integer id;
    private CurrencyBalanceReadDto sellerCurrencyBalance;
    private CurrencyBalanceReadDto buyerCurrencyBalance;
    private DayRateReadDto dayRate;
    private BigDecimal baseAmount;
    private DealType dealType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

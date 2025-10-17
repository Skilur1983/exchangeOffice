package org.example.model.dto.deal;

import lombok.*;
import org.example.model.Currency;
import org.example.model.DealStatus;
import org.example.model.DealType;
import org.example.model.dto.dayrate.DayRateReadDto;
import org.example.model.dto.user.UserReadDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealReadDto {

    private Integer id;
    private UserReadDto seller;
    private Currency sellerCurrency;
    private UserReadDto buyer;
    private Currency buyerCurrency;
    private DayRateReadDto dayRate;
    private BigDecimal soldAmount;
    private BigDecimal purchasedAmount;
    private BigDecimal exchangeRateUsed;
    private DealType dealType;
    private DealStatus status;
    private String statusReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    public String getCurrencyPair() {
        return sellerCurrency + "/" + buyerCurrency;
    }
}

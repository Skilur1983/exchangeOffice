package org.example.model.dto.currencybalance;

import lombok.*;
import org.example.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyBalanceReadDto {

    private Integer id;
    private Currency currency;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer userId;
    private String username;
}

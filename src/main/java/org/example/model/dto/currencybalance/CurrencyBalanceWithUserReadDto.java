package org.example.model.dto.currencybalance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Currency;
import org.example.model.dto.user.UserReadDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyBalanceWithUserReadDto {

    private Integer id;
    private Currency currency;
    private BigDecimal amount;
    private UserReadDto userReadDto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package org.example.model.dto.currencybalance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Currency;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyBalanceUpdateDto {

    @NotNull(message = "Currency is required")
    private Currency currency;

    @DecimalMin(value = "0.0", inclusive = false, message = "Buy rate must be greater than 0")
    private BigDecimal amount;

    @Positive(message = "User ID must be positive")
    private Integer userId;
}

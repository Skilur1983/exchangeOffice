package org.example.model.dto.currencybalance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
public class CurrencyBalanceCreateDto {

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0",  inclusive = false, message = "Amount must be 0 or greater")
    @Digits(integer = 15, fraction = 4, message = "Amount format is invalid")
    private BigDecimal amount;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Integer userId;
}

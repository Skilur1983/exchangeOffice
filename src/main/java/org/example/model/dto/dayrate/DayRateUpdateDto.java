package org.example.model.dto.dayrate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayRateUpdateDto {

    @NotNull(message = "Buy rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Buy rate must be greater than 0")
    @Digits(integer = 13, fraction = 6, message = "Buy rate format is invalid")
    private BigDecimal buyRate; // Rate at which bank BUYS base currency (lower)

    @NotNull(message = "Sell rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sell rate must be greater than 0")
    @Digits(integer = 13, fraction = 6, message = "Sell rate format is invalid")
    private BigDecimal sellRate; // Rate at which bank SELLS base currency (higher)

    @AssertTrue(message = "Buy rate must be less than or equal to sell rate")
    public boolean isRateValid() {
        if (buyRate == null || sellRate == null) return true;
        return buyRate.compareTo(sellRate) <= 0;
    }
}

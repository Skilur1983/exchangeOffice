package org.example.model.dto.deal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.DealType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealCreateDto {

    @NotNull(message = "Seller's currency balance ID is required")
    @Positive(message = "Seller's currency balance ID must be positive")
    private Integer sellerCurrencyBalanceId;

    @NotNull(message = "Buyer's currency balance ID is required")
    @Positive(message = "Buyer's currency balance ID must be positive")
    private Integer buyerCurrencyBalanceId;

    @NotNull(message = "Day rate ID is required")
    @Positive(message = "Day rate ID must be positive")
    private Integer dayRateId;

    @NotNull(message = "Base amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Base amount format is invalid")
    private BigDecimal baseAmount;

    @NotNull(message = "Deal type is required")
    private DealType dealType;

    @AssertTrue(message = "Seller and buyer currency balances must be different")
    public boolean isDifferentBalances() {
        if (sellerCurrencyBalanceId == null || buyerCurrencyBalanceId == null) return true;
        return !sellerCurrencyBalanceId.equals(buyerCurrencyBalanceId);
    }
}

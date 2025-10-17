package org.example.model.dto.deal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Currency;
import org.example.model.DealType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealCreateDto {

    @NotNull(message = "Seller's ID is required")
    @Positive(message = "Seller's ID must be positive")
    private Integer sellerId;

    @NotNull(message = "Seller's currency is required")
    private Currency sellerCurrency;

    @NotNull(message = "Buyer's ID is required")
    @Positive(message = "Buyer's ID must be positive")
    private Integer buyerId;

    @NotNull(message = "Buyer's currency is required")
    private Currency buyerCurrency;

    @NotNull(message = "Sold amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Sold amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Sold amount format is invalid")
    private BigDecimal soldAmount;

    @NotNull(message = "Deal type is required")
    private DealType dealType;

    @AssertTrue(message = "Seller and buyer must be different")
    public boolean isDifferentUsers() {
        if (sellerId == null || buyerId == null) return true;
        return !sellerId.equals(buyerId);
    }

    @AssertTrue(message = "Seller and buyer currencies must be different")
    public boolean isDifferentCurrencies() {
        if (sellerCurrency == null || buyerCurrency == null) return true;
        return !sellerCurrency.equals(buyerCurrency);
    }
}

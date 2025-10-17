package org.example.model.dto.dayrate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayRateReadDto {

    private Integer id;
    private Currency baseCurrency;
    private Currency quoteCurrency;
    private LocalDate rateDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal buyRate; // Rate at which bank BUYS base currency (lower)
    private BigDecimal sellRate; // Rate at which bank SELLS base currency (higher)

    public String getCurrencyPair() {
        if (baseCurrency == null || quoteCurrency == null) {
            return "N/A";
        }
        return baseCurrency + "/" + quoteCurrency;
    }
}

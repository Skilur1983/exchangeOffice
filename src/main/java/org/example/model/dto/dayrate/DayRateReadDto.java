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
    private BigDecimal buyRate;
    private BigDecimal sellRate;

    public String getCurrencyPair() {
        return baseCurrency + "/" + quoteCurrency;
    }
}

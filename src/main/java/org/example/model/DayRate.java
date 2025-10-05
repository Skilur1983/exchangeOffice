package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "day_rates",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"base_currency", "quote_currency", "created"}
        )
)
public class DayRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 20)
    private Currency baseCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_currency", nullable = false, length = 20)
    private Currency quoteCurrency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "buy_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal buyRate;

    @Column(name = "sell_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal sellRate;
}

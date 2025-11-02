package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "day_rates",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"base_currency", "quote_currency", "rate_date"}
        ),
        indexes = {
                @Index(name = "idx_rate_date", columnList = "rate_date")
        }
)
public class DayRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "quote_currency", nullable = false, length = 20)
    private Currency quoteCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_currency", nullable = false, length = 20)
    private Currency baseCurrency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(name = "buy_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal buyRate; // Rate at which bank BUYS quote currency (lower)

    @Column(name = "sell_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal sellRate; // Rate at which bank SELLS quote currency (higher)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

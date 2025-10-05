package org.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "deals")
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_currency_balance_id", nullable = false)
    @ToString.Exclude
    private CurrencyBalance sellerCurrencyBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_currency_balance_id", nullable = false)
    @ToString.Exclude
    private CurrencyBalance buyerCurrencyBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_rate_id", nullable = false)
    @ToString.Exclude
    private DayRate dayRate;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type", nullable = false, length = 10)
    private DealType dealType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

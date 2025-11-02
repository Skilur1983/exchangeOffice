package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "deals",
        indexes = {
                @Index(name = "idx_deal_created_at", columnList = "created_at"),
                @Index(name = "idx_deal_seller_id", columnList = "seller_user_id"),
                @Index(name = "idx_deal_buyer_id", columnList = "buyer_user_id"),
                @Index(name = "idx_deal_type_created", columnList = "deal_type, created_at"),
                @Index(name = "idx_deal_status_created", columnList = "status, created_at")
        })
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_user_id", nullable = false)
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_currency", nullable = false)
    private Currency sellerCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id", nullable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(name = "buyer_currency", nullable = false)
    private Currency buyerCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_rate_id", nullable = false)
    @ToString.Exclude
    private DayRate dayRate;

    @Column(name = "exchange_rate_used", precision = 19, scale = 6)
    private BigDecimal exchangeRateUsed;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "sold_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal soldAmount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "purchased_amount", precision = 19, scale = 4)
    private BigDecimal purchasedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type", nullable = false, length = 10)
    private DealType dealType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DealStatus status;

    @Column(name = "status_reason", length = 200)
    private String statusReason;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = DealStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

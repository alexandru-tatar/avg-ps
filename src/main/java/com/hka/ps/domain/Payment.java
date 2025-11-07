package com.hka.ps.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_order", columnNames = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false, unique = true)
  private String orderId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(length = 3, nullable = false)
  private String currency;

  @Column(nullable = false)
  private String method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Column(name = "idempotency_key", unique = true)
  private String idempotencyKey;
}

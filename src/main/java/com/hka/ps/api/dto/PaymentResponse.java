package com.hka.ps.api.dto;

import com.hka.ps.domain.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class PaymentResponse {
  String orderId;
  BigDecimal amount;
  String currency;
  String method;
  PaymentStatus status;
  Instant createdAt;
  Instant updatedAt;
}

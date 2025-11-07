package com.hka.ps.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {
  private String orderId;
  private BigDecimal amount;
  private String reason;
}
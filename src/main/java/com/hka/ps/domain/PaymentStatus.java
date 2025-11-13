package com.hka.ps.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statuswerte, die vom Payment Service geführt werden")
public enum PaymentStatus {
  AUTHORIZED,
  CAPTURED,
  REFUNDED,
  DECLINED
}

package com.hka.ps.api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CaptureRequest", description = "Payload zum Verbuchen (Capture) einer autorisierten Zahlung")
public class CaptureRequest {
  @Schema(description = "Order-ID der bereits autorisierten Zahlung", example = "ORD-20241006-143211-AB12CD34", requiredMode = Schema.RequiredMode.REQUIRED)
  private String orderId;
  @Schema(description = "Zu capturnder Betrag; muss der Autorisierung entsprechen", example = "149.99", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal amount;

  public CaptureRequest() {
  }

  public CaptureRequest(String orderId, BigDecimal amount) {
    this.orderId = orderId;
    this.amount = amount;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }
}

package com.hka.ps.api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RefundRequest", description = "Payload zum Anstoßen einer Rückerstattung")
public class RefundRequest {
  @Schema(description = "Order-ID der Zahlung", example = "ORD-20241006-143211-AB12CD34", requiredMode = Schema.RequiredMode.REQUIRED)
  private String orderId;
  @Schema(description = "Zu erstattender Betrag", example = "49.99", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal amount;
  @Schema(description = "Freitextbegründung für die Rückerstattung", example = "Customer cancelled order")
  private String reason;

  public RefundRequest() {
  }

  public RefundRequest(String orderId, BigDecimal amount, String reason) {
    this.orderId = orderId;
    this.amount = amount;
    this.reason = reason;
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

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}

package com.hka.ps.api.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthorizeRequest", description = "Payload zum Autorisieren eines Zahlungsvorgangs")
public class AuthorizeRequest {
  @Schema(description = "Order-ID, die autorisiert werden soll", example = "ORD-20241006-143211-AB12CD34", requiredMode = Schema.RequiredMode.REQUIRED)
  private String orderId;
  @Schema(description = "Betrag der Autorisierung", example = "149.99", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal amount;
  @Schema(description = "ISO-4217 Währungscode", example = "EUR", requiredMode = Schema.RequiredMode.REQUIRED)
  private String currency;
  @Schema(description = "Zahlmethode, z. B. CARD, PAYPAL", example = "CARD", requiredMode = Schema.RequiredMode.REQUIRED)
  private String method;

  public AuthorizeRequest() {
  }

  public AuthorizeRequest(String orderId, BigDecimal amount, String currency, String method) {
    this.orderId = orderId;
    this.amount = amount;
    this.currency = currency;
    this.method = method;
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

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }
}

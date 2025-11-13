package com.hka.ps.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.hka.ps.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PaymentResponse", description = "Antwortobjekt für Zahlungsoperationen")
public final class PaymentResponse {
  @Schema(description = "Verknüpfte Order-ID", example = "ORD-20241006-143211-AB12CD34")
  private final String orderId;
  @Schema(description = "Verarbeiteter Betrag", example = "149.99")
  private final BigDecimal amount;
  @Schema(description = "Währung", example = "EUR")
  private final String currency;
  @Schema(description = "Bezahlmethode", example = "CARD")
  private final String method;
  @Schema(description = "Aktueller Zahlungsstatus", implementation = PaymentStatus.class)
  private final PaymentStatus status;
  @Schema(description = "Zeitpunkt der Erstellung", example = "2024-10-06T14:32:11.451Z")
  private final Instant createdAt;
  @Schema(description = "Zeitpunkt der letzten Änderung", example = "2024-10-06T14:45:02.133Z")
  private final Instant updatedAt;

  private PaymentResponse(Builder builder) {
    this.orderId = builder.orderId;
    this.amount = builder.amount;
    this.currency = builder.currency;
    this.method = builder.method;
    this.status = builder.status;
    this.createdAt = builder.createdAt;
    this.updatedAt = builder.updatedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getOrderId() {
    return orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getMethod() {
    return method;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public static final class Builder {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String method;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Builder() {
    }

    public Builder orderId(String orderId) {
      this.orderId = orderId;
      return this;
    }

    public Builder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public Builder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    public Builder status(PaymentStatus status) {
      this.status = status;
      return this;
    }

    public Builder createdAt(Instant createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder updatedAt(Instant updatedAt) {
      this.updatedAt = updatedAt;
      return this;
    }

    public PaymentResponse build() {
      return new PaymentResponse(this);
    }
  }
}

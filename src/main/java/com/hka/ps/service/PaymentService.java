package com.hka.ps.service;

import com.hka.ps.api.dto.AuthorizeRequest;
import com.hka.ps.api.dto.CaptureRequest;
import com.hka.ps.api.dto.RefundRequest;
import com.hka.ps.domain.Payment;
import com.hka.ps.domain.PaymentStatus;
import com.hka.ps.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

  private final PaymentRepository repository;

  @Transactional
  public Payment authorize(AuthorizeRequest request, String idempotencyKey) {
    log.info("Authorize request orderId={} amount={} currency={} idempotencyKey={}",
        request.getOrderId(), request.getAmount(), request.getCurrency(), idempotencyKey);
    validateAuthorize(request);

    Payment existing = findIdempotent(idempotencyKey, request.getOrderId());
    if (existing != null) {
      log.info("Idempotent authorize hit for order {}", existing.getOrderId());
      return existing;
    }

    Payment payment = repository.findByOrderId(request.getOrderId()).orElseGet(Payment::new);
    if (payment.getId() != null) {
      log.info("Existing payment reused for order {}", payment.getOrderId());
      return payment;
    }

    payment.setOrderId(request.getOrderId());
    payment.setAmount(request.getAmount());
    payment.setCurrency(request.getCurrency());
    payment.setMethod(request.getMethod());
    payment.setIdempotencyKey(idempotencyKey);
    payment.setCreatedAt(Instant.now());
    payment.setUpdatedAt(payment.getCreatedAt());

    PaymentStatus status = evaluateAuthorization(request.getAmount());
    payment.setStatus(status);

    repository.save(payment);
    log.info("Authorization {} for order {}", status, payment.getOrderId());
    return payment;
  }

  @Transactional
  public Payment capture(CaptureRequest request) {
    log.info("Capture request orderId={} amount={}", request.getOrderId(), request.getAmount());
    Payment payment = repository.findByOrderId(request.getOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId=" + request.getOrderId()));

    validateAmount(request.getAmount());
    ensureAmountMatches(payment.getAmount(), request.getAmount());

    if (payment.getStatus() == PaymentStatus.CAPTURED) {
      log.info("Capture idempotent for order {}", payment.getOrderId());
      return payment;
    }
    if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
      throw new IllegalStateException("Payment not in AUTHORIZED state");
    }

    payment.setStatus(PaymentStatus.CAPTURED);
    payment.setUpdatedAt(Instant.now());
    log.info("Payment captured for order {}", payment.getOrderId());
    return payment;
  }

  @Transactional
  public Payment refund(RefundRequest request) {
    log.info("Refund request orderId={} amount={} reason={}", request.getOrderId(), request.getAmount(), request.getReason());
    Payment payment = repository.findByOrderId(request.getOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId=" + request.getOrderId()));

    validateAmount(request.getAmount());

    if (payment.getStatus() == PaymentStatus.REFUNDED) {
      log.info("Refund idempotent for order {}", payment.getOrderId());
      return payment;
    }
    if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.AUTHORIZED) {
      throw new IllegalStateException("Payment cannot be refunded from state " + payment.getStatus());
    }

    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setUpdatedAt(Instant.now());
    log.info("Payment refunded for order {}", payment.getOrderId());
    return payment;
  }

  private Payment findIdempotent(String key, String orderId) {
    if (key != null && !key.isBlank()) {
      return repository.findByIdempotencyKey(key).orElse(null);
    }
    if (orderId != null) {
      return repository.findByOrderId(orderId).orElse(null);
    }
    return null;
  }

  private PaymentStatus evaluateAuthorization(BigDecimal amount) {
    return amount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(BigDecimal.valueOf(2000)) <= 0
        ? PaymentStatus.AUTHORIZED
        : PaymentStatus.DECLINED;
  }

  private void validateAuthorize(AuthorizeRequest request) {
    Objects.requireNonNull(request.getOrderId(), "orderId required");
    validateAmount(request.getAmount());
    Objects.requireNonNull(request.getCurrency(), "currency required");
    if (request.getCurrency().length() != 3) {
      throw new IllegalArgumentException("currency must be ISO 4217 code");
    }
    Objects.requireNonNull(request.getMethod(), "method required");
  }

  private void validateAmount(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("amount must be > 0");
    }
  }

  private void ensureAmountMatches(BigDecimal existing, BigDecimal incoming) {
    if (existing.compareTo(incoming) != 0) {
      throw new IllegalStateException("amount mismatch");
    }
  }
}

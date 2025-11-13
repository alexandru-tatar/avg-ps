package com.hka.ps.service;

import com.hka.ps.api.dto.AuthorizeRequest;
import com.hka.ps.api.dto.CaptureRequest;
import com.hka.ps.api.dto.RefundRequest;
import com.hka.ps.domain.Payment;
import com.hka.ps.domain.PaymentStatus;
import com.hka.ps.publisher.PsPublisher;
import com.hka.ps.repo.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Service
public class PaymentService {
  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
  private final PsPublisher publisher;
  private final PaymentRepository repository;

  public PaymentService(PsPublisher publisher, PaymentRepository repository) {
    this.publisher = publisher;
    this.repository = repository;
  }
  
  @Transactional
  public Payment authorize(AuthorizeRequest request, String idempotencyKey) {
    String log = "Authorize request orderId= " + request.getOrderId() + " amount= " + request.getAmount() + " currency= " +  request.getCurrency() + " idempotencyKey= " + idempotencyKey;
    logger.info(log);
    publishLog(log);
    validateAuthorize(request);

    Payment existing = findIdempotent(idempotencyKey, request.getOrderId());
    if (existing != null) {
      String log2 = "Idempotent authorize hit for order " + existing.getOrderId();
      logger.info(log2);
      publishLog(log2);
      return existing;
    }

    Payment payment = repository.findByOrderId(request.getOrderId()).orElseGet(Payment::new);
    if (payment.getId() != null) {
      String log3 = "Existing payment reused for order " + payment.getOrderId();
      logger.info(log3);
      publishLog(log3);
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
    logger.info("Authorization {} for order {}", status, payment.getOrderId());
    return payment;
  }

  @Transactional
  public Payment capture(CaptureRequest request) {
    String log4 = "Capture request orderId= " + request.getOrderId() + " amount= " + request.getAmount();
    logger.info(log4);
    publishLog(log4);
    Payment payment = repository.findByOrderId(request.getOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId=" + request.getOrderId()));

    validateAmount(request.getAmount());
    ensureAmountMatches(payment.getAmount(), request.getAmount());

    if (payment.getStatus() == PaymentStatus.CAPTURED) {
      String log5 = "Capture idempotent for order " + payment.getOrderId();
      logger.info(log5);
      publishLog(log5);
      return payment;
    }
    if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
      throw new IllegalStateException("Payment not in AUTHORIZED state");
    }

    payment.setStatus(PaymentStatus.CAPTURED);
    payment.setUpdatedAt(Instant.now());
    String log6 = "Payment captured for order " + payment.getOrderId();
    logger.info(log6);
    publishLog(log6);
    return payment;
  }

  @Transactional
  public Payment refund(RefundRequest request) {
    String log7 = "Refund request orderId= " + request.getOrderId() + " amount= " + request.getAmount() + " reason= " + request.getReason();
    logger.info(log7);
    publishLog(log7);
    Payment payment = repository.findByOrderId(request.getOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Payment not found for orderId=" + request.getOrderId()));

    validateAmount(request.getAmount());

    if (payment.getStatus() == PaymentStatus.REFUNDED) {
      logger.info("Refund idempotent for order {}", payment.getOrderId());
      return payment;
    }
    if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.AUTHORIZED) {
      throw new IllegalStateException("Payment cannot be refunded from state " + payment.getStatus());
    }

    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setUpdatedAt(Instant.now());
    logger.info("Payment refunded for order {}", payment.getOrderId());
    return payment;
  }

  public void publishLog(String message) {
    publisher.publish(message);
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

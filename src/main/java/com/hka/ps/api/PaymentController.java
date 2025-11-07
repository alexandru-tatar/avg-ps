package com.hka.ps.api;

import com.hka.ps.api.dto.*;
import com.hka.ps.domain.Payment;
import com.hka.ps.domain.PaymentStatus;
import com.hka.ps.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

  private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
  private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

  private final PaymentService service;

  @PostMapping("/authorize")
  public ResponseEntity<PaymentResponse> authorize(
      @RequestBody AuthorizeRequest request,
      @RequestHeader(name = IDEMPOTENCY_HEADER, required = false) String idempotencyKey) {

    log.info("POST /payments/authorize orderId={} idempotencyKey={}", request.getOrderId(), idempotencyKey);
    Payment payment = service.authorize(request, idempotencyKey);
    PaymentResponse response = toResponse(payment);
    HttpStatus status = payment.getStatus() == PaymentStatus.DECLINED ? HttpStatus.PAYMENT_REQUIRED : HttpStatus.OK;
    log.info("Authorize result orderId={} status={}", payment.getOrderId(), status);
    return ResponseEntity.status(status).body(response);
  }

  @PostMapping("/capture")
  public ResponseEntity<PaymentResponse> capture(@RequestBody CaptureRequest request) {
    log.info("POST /payments/capture orderId={}", request.getOrderId());
    Payment payment = service.capture(request);
    log.info("Capture result orderId={} status={}", payment.getOrderId(), payment.getStatus());
    return ResponseEntity.ok(toResponse(payment));
  }

  @PostMapping("/refund")
  public ResponseEntity<PaymentResponse> refund(@RequestBody RefundRequest request) {
    log.info("POST /payments/refund orderId={} reason={}", request.getOrderId(), request.getReason());
    Payment payment = service.refund(request);
    log.info("Refund result orderId={} status={}", payment.getOrderId(), payment.getStatus());
    return ResponseEntity.ok(toResponse(payment));
  }

  private PaymentResponse toResponse(Payment payment) {
    return PaymentResponse.builder()
        .orderId(payment.getOrderId())
        .amount(payment.getAmount())
        .currency(payment.getCurrency())
        .method(payment.getMethod())
        .status(payment.getStatus())
        .createdAt(payment.getCreatedAt())
        .updatedAt(payment.getUpdatedAt())
        .build();
  }
}

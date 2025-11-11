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

    String msgIn = "POST /payments/authorize orderId= " + request.getOrderId()
        + " idempotencyKey= " + String.valueOf(idempotencyKey);
    log.info(msgIn);
    service.publishLog(msgIn);

    Payment payment = service.authorize(request, idempotencyKey);

    HttpStatus status = (payment.getStatus() == PaymentStatus.DECLINED)
        ? HttpStatus.PAYMENT_REQUIRED : HttpStatus.OK;

    String msgOut = "Authorize result orderId= " + payment.getOrderId()
        + " status= " + status;
    log.info(msgOut);
    service.publishLog(msgOut);

    return ResponseEntity.status(status).body(toResponse(payment));
  }

  @PostMapping("/capture")
  public ResponseEntity<PaymentResponse> capture(@RequestBody CaptureRequest request) {
    String msgIn = "POST /payments/capture orderId= " + request.getOrderId();
    log.info(msgIn);
    service.publishLog(msgIn);

    Payment payment = service.capture(request);

    String msgOut = "Capture result orderId= " + payment.getOrderId()
        + " status= " + payment.getStatus();
    log.info(msgOut);
    service.publishLog(msgOut);

    return ResponseEntity.ok(toResponse(payment));
  }

  @PostMapping("/refund")
  public ResponseEntity<PaymentResponse> refund(@RequestBody RefundRequest request) {
    String msgIn = "POST /payments/refund orderId= " + request.getOrderId()
        + " reason= " + request.getReason();
    log.info(msgIn);
    service.publishLog(msgIn);

    Payment payment = service.refund(request);

    String msgOut = "Refund result orderId= " + payment.getOrderId()
        + " status= " + payment.getStatus();
    log.info(msgOut);
    service.publishLog(msgOut);

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
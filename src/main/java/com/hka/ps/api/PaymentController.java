package com.hka.ps.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hka.ps.api.dto.AuthorizeRequest;
import com.hka.ps.api.dto.CaptureRequest;
import com.hka.ps.api.dto.PaymentResponse;
import com.hka.ps.api.dto.RefundRequest;
import com.hka.ps.domain.Payment;
import com.hka.ps.domain.PaymentStatus;
import com.hka.ps.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpunkte für Autorisierung, Capture und Refund von Zahlungen")
public class PaymentController {

  private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
  private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

  private final PaymentService service;

  public PaymentController(PaymentService service) {
    this.service = service;
  }

  @PostMapping("/authorize")
  @Operation(
      summary = "Autorisiert eine Zahlung",
      description = "Autorisiert einen Auftrag. Optional kann ein Idempotency-Key zur Wiederverwendbarkeit übergeben werden.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AuthorizeRequest.class))))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Zahlung autorisiert", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
      @ApiResponse(responseCode = "402", description = "Zahlung abgelehnt", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Ungültige Eingabe", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Konflikt (z. B. falscher Betrag)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  public ResponseEntity<PaymentResponse> authorize(
      @RequestBody AuthorizeRequest request,
      @Parameter(description = "Optionaler Schlüssel zur Idempotenz", example = "a86f9253-5bd5-4fa8-9c97-71b89d1cd876")
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
  @Operation(
      summary = "Capturt eine autorisierte Zahlung",
      description = "Verändert den Status einer Zahlung von AUTHORIZED auf CAPTURED, sofern der Betrag übereinstimmt.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(schema = @Schema(implementation = CaptureRequest.class))))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Capture erfolgreich", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Ungültiger Betrag oder Zahlung nicht gefunden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Capture-Konflikt", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  })
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
  @Operation(
      summary = "Erstattet eine Zahlung zurück",
      description = "Setzt den Zahlungsstatus auf REFUNDED, sofern der Auftrag autorisiert bzw. verbucht ist.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          content = @Content(schema = @Schema(implementation = RefundRequest.class))))
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Refund erfolgreich", content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
      @ApiResponse(responseCode = "400", description = "Ungültige Eingabe oder Zahlung nicht gefunden", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
      @ApiResponse(responseCode = "409", description = "Refund-Konflikt", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  })
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

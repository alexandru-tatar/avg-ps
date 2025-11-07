package com.hka.ps.repo;

import com.hka.ps.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByOrderId(String orderId);
  Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
package com.desco.payment.service.impl;

import com.desco.payment.dto.request.PaymentRequest;
import com.desco.payment.dto.response.PaymentResponse;
import com.desco.payment.dto.response.ReceiptResponse;
import com.desco.payment.entity.Payment;
import com.desco.payment.enums.PaymentStatus;
import com.desco.payment.exception.DuplicatePaymentException;
import com.desco.payment.exception.ResourceNotFoundException;
import com.desco.payment.repository.PaymentRepository;
import com.desco.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String DEFAULT_METHOD = "DUMMY_GATEWAY";
    private static final String CURRENCY = "BDT";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponse pay(PaymentRequest request) {
        if (paymentRepository.existsByUserAndBillMonthAndStatus(
                request.getUserId(), request.getBillMonth(), PaymentStatus.SUCCESS.name())) {
            throw new DuplicatePaymentException(
                    "Bill for " + request.getBillMonth() + " has already been paid by this user");
        }

        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setBillMonth(request.getBillMonth());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(resolveMethod(request.getPaymentMethod()));
        payment.setTransactionId(generateTransactionId());
        payment.setStatus(PaymentStatus.PENDING);

        // --- Simulated gateway call ---------------------------------------
        // A real integration would call bKash/Nagad here. The simulation settles
        // immediately; any gateway error leaves the row in FAILED so it stays auditable.
        try {
            settleWithGateway(payment);
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
        } catch (RuntimeException ex) {
            log.warn("Simulated gateway rejected transaction {}: {}",
                    payment.getTransactionId(), ex.getMessage());
            payment.setStatus(PaymentStatus.FAILED);
        }
        // ------------------------------------------------------------------

        // saveAndFlush (not save) so @CreationTimestamp is generated before we map the
        // response — a plain save() defers the INSERT to commit and leaves createdAt null.
        Payment saved = paymentRepository.saveAndFlush(payment);
        log.info("Payment {} recorded for user {} ({} {}) -> {}",
                saved.getTransactionId(), saved.getUserId(), CURRENCY, saved.getAmount(), saved.getStatus());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID paymentId) {
        return toResponse(findPayment(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getHistory(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceipt(UUID paymentId) {
        Payment payment = findPayment(paymentId);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException(
                    "Receipt is only available for successful payments; this payment is "
                            + payment.getStatus());
        }

        return ReceiptResponse.builder()
                .transactionId(payment.getTransactionId())
                .paymentId(payment.getId())
                .userId(payment.getUserId())
                .billMonth(payment.getBillMonth())
                .amount(payment.getAmount())
                .currency(CURRENCY)
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .issuedBy("DESCO Simulation")
                .note("This is a simulated receipt generated for academic purposes.")
                .build();
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }

    private String resolveMethod(String method) {
        return (method == null || method.isBlank()) ? DEFAULT_METHOD : method.trim().toUpperCase();
    }

    /**
     * Transaction IDs must be unique (DB constraint) and readable on a receipt.
     * Format: TXN-{epochMillis}-{6 random digits}
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /**
     * Stand-in for a real payment gateway. Always settles in the simulation.
     */
    private void settleWithGateway(Payment payment) {
        log.debug("Contacting simulated gateway for transaction {}", payment.getTransactionId());
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .billMonth(payment.getBillMonth())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

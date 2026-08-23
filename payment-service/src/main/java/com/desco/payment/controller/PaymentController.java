package com.desco.payment.controller;

import com.desco.payment.dto.request.PaymentRequest;
import com.desco.payment.dto.response.PaymentResponse;
import com.desco.payment.dto.response.ReceiptResponse;
import com.desco.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Electricity bill payment simulation")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Pay an electricity bill (simulated)")
    public ResponseEntity<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(paymentService.pay(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch a single payment by id")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "List a user's payments, newest first")
    public ResponseEntity<List<PaymentResponse>> getHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(paymentService.getHistory(userId));
    }

    @GetMapping("/{id}/receipt")
    @Operation(summary = "Generate a receipt for a successful payment")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getReceipt(id));
    }
}

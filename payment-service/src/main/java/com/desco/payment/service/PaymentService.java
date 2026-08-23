package com.desco.payment.service;

import com.desco.payment.dto.request.PaymentRequest;
import com.desco.payment.dto.response.PaymentResponse;
import com.desco.payment.dto.response.ReceiptResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    /** Simulates paying a bill and persists the resulting transaction. */
    PaymentResponse pay(PaymentRequest request);

    PaymentResponse getById(UUID paymentId);

    List<PaymentResponse> getHistory(UUID userId);

    ReceiptResponse getReceipt(UUID paymentId);
}

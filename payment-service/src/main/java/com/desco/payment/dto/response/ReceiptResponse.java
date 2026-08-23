package com.desco.payment.dto.response;

import com.desco.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Printable receipt for a completed payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private String transactionId;
    private UUID paymentId;
    private UUID userId;
    private String billMonth;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private String issuedBy;
    private String note;
}

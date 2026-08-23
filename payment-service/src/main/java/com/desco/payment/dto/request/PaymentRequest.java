package com.desco.payment.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotNull(message = "billMonth is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "billMonth must be in YYYY-MM format")
    private String billMonth;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "1.00", message = "amount must be at least 1.00")
    @DecimalMax(value = "99999999.99", message = "amount exceeds the maximum allowed value")
    private BigDecimal amount;

    /** Optional; defaults to DUMMY_GATEWAY when omitted. */
    private String paymentMethod;
}

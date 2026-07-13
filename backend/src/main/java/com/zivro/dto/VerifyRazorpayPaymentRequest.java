package com.zivro.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRazorpayPaymentRequest {

    @NotBlank private String orderId;

    @NotBlank private String paymentId;

    @NotBlank private String signature;
}

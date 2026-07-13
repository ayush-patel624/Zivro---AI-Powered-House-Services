package com.zivro.controller;

import com.zivro.config.RazorpayProperties;
import com.zivro.dto.PaymentPublicConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentConfigController {

    private final RazorpayProperties razorpayProperties;

    @GetMapping("/public-config")
    public PaymentPublicConfigResponse publicConfig() {
        boolean on = razorpayProperties.enabled() && razorpayProperties.isConfigured();
        return PaymentPublicConfigResponse.builder()
                .razorpayEnabled(on)
                .razorpayKeyId(on ? razorpayProperties.keyId() : null)
                .build();
    }
}

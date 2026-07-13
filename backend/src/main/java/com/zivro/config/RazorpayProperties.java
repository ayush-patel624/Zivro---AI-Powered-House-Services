package com.zivro.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zivro.razorpay")
public record RazorpayProperties(
        boolean enabled,
        String keyId,
        String keySecret,
        String webhookSecret,
        BigDecimal depositFraction,
        boolean requirePaidBeforeAccept) {

    public RazorpayProperties {
        if (depositFraction == null || depositFraction.compareTo(BigDecimal.ZERO) <= 0) {
            depositFraction = new BigDecimal("0.25");
        }
    }

    public boolean isConfigured() {
        return keyId != null
                && !keyId.isBlank()
                && keySecret != null
                && !keySecret.isBlank();
    }
}

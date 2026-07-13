package com.zivro.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zivro.service.BookingPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final BookingPaymentService bookingPaymentService;
    private final ObjectMapper objectMapper;

    @PostMapping("/razorpay")
    public ResponseEntity<Void> razorpay(HttpServletRequest request) throws Exception {
        String payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        String signature = request.getHeader("X-Razorpay-Signature");
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!bookingPaymentService.isWebhookVerificationConfigured()) {
            log.warn("Razorpay webhook received but zivro.razorpay.webhook-secret is not set; ignoring event.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!bookingPaymentService.verifyWebhookSignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JsonNode root = objectMapper.readTree(payload);
        String event = root.path("event").asText("");
        if (!"payment.captured".equals(event)) {
            return ResponseEntity.ok().build();
        }
        JsonNode entity = root.path("payload").path("payment").path("entity");
        if (entity.isMissingNode() || entity.isNull()) {
            return ResponseEntity.ok().build();
        }
        String paymentId = entity.path("id").asText(null);
        String orderId = entity.path("order_id").asText(null);
        long amountPaise = entity.path("amount").asLong(0);
        if (paymentId == null || orderId == null) {
            log.warn("Razorpay webhook missing payment id or order id");
            return ResponseEntity.ok().build();
        }
        bookingPaymentService.handleWebhookPaymentCaptured(orderId, paymentId, amountPaise);
        return ResponseEntity.ok().build();
    }
}

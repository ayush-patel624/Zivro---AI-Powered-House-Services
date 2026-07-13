package com.zivro.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.zivro.config.RazorpayProperties;
import com.zivro.domain.Booking;
import com.zivro.domain.PaymentStatus;
import com.zivro.domain.User;
import com.zivro.dto.BookingResponse;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ForbiddenException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.BookingRepository;
import com.zivro.util.BookingMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingPaymentService {

    private final RazorpayProperties razorpayProperties;
    private final BookingRepository bookingRepository;

    @Transactional
    public void initializeDepositForBooking(Long bookingId) {
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (!razorpayProperties.enabled() || !razorpayProperties.isConfigured()) {
            b.setPaymentStatus(PaymentStatus.NOT_CONFIGURED);
            b.setDepositAmount(null);
            b.setAmountPaid(BigDecimal.ZERO);
            b.setRazorpayOrderId(null);
            b.setRazorpayBalanceOrderId(null);
            bookingRepository.save(b);
            return;
        }
        BigDecimal deposit = computeDeposit(b.getPrice());
        b.setDepositAmount(deposit);
        b.setAmountPaid(BigDecimal.ZERO);
        String receipt = receiptFor("z", bookingId);
        try {
            RazorpayClient client = client();
            Order order = createOrder(client, deposit, receipt);
            b.setRazorpayOrderId(order.get("id"));
            b.setPaymentStatus(PaymentStatus.PENDING);
        } catch (RazorpayException e) {
            log.error("Razorpay order create failed", e);
            b.setPaymentStatus(PaymentStatus.FAILED);
            throw new BadRequestException("Could not create payment order. Check Razorpay credentials.");
        }
        bookingRepository.save(b);
    }

    @Transactional
    public void applySatisfactionPricing(Long bookingId, int satisfactionStars) {
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        BigDecimal mult = satisfactionMultiplier(satisfactionStars);
        BigDecimal finalPrice = b.getPrice().multiply(mult).setScale(2, RoundingMode.HALF_UP);
        b.setFinalPriceAfterSatisfaction(finalPrice);
        BigDecimal balance =
                finalPrice.subtract(b.getAmountPaid()).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        if (balance.compareTo(new BigDecimal("0.01")) <= 0) {
            b.setPaymentStatus(PaymentStatus.PAID);
            b.setRazorpayBalanceOrderId(null);
            bookingRepository.save(b);
            return;
        }
        if (razorpayProperties.enabled() && razorpayProperties.isConfigured()) {
            String receipt = receiptFor("zb", bookingId);
            try {
                RazorpayClient client = client();
                Order order = createOrder(client, balance, receipt);
                b.setRazorpayBalanceOrderId(order.get("id"));
                b.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
            } catch (RazorpayException e) {
                log.error("Razorpay balance order failed", e);
                throw new BadRequestException("Could not create balance payment order.");
            }
        } else {
            b.setPaymentStatus(PaymentStatus.PAID);
        }
        bookingRepository.save(b);
    }

    @Transactional
    public BookingResponse verifyAndRecordPayment(
            Long bookingId, User user, String orderId, String paymentId, String signature) {
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (!b.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the booking owner can confirm payment.");
        }
        if (!StringUtils.hasText(orderId)
                || !StringUtils.hasText(paymentId)
                || !StringUtils.hasText(signature)) {
            throw new BadRequestException("orderId, paymentId and signature are required.");
        }
        boolean depositOrder = orderId.equals(b.getRazorpayOrderId());
        boolean balanceOrder =
                StringUtils.hasText(b.getRazorpayBalanceOrderId()) && orderId.equals(b.getRazorpayBalanceOrderId());
        if (!depositOrder && !balanceOrder) {
            throw new BadRequestException("Order id does not match this booking.");
        }
        if (depositOrder && paymentId.equals(b.getRazorpayDepositPaymentId())) {
            return bookingRepository.findDetailById(bookingId).map(BookingMapper::toResponse).orElseThrow();
        }
        if (balanceOrder && paymentId.equals(b.getRazorpayBalancePaymentId())) {
            return bookingRepository.findDetailById(bookingId).map(BookingMapper::toResponse).orElseThrow();
        }
        verifySignature(orderId, paymentId, signature);
        BigDecimal paid = fetchPaymentAmountInRupees(paymentId);
        applyCapturedPayment(b, orderId, paymentId, paid);
        bookingRepository.save(b);
        return bookingRepository.findDetailById(bookingId).map(BookingMapper::toResponse).orElseThrow();
    }

    @Transactional
    public void handleWebhookPaymentCaptured(String orderId, String paymentId, long amountPaise) {
        if (!StringUtils.hasText(orderId) || !StringUtils.hasText(paymentId)) {
            return;
        }
        Booking b =
                bookingRepository
                        .findByAnyRazorpayOrderId(orderId)
                        .flatMap(booking -> bookingRepository.findByIdForUpdate(booking.getId()))
                        .orElse(null);
        if (b == null) {
            log.warn("Webhook: no booking for order {}", orderId);
            return;
        }
        boolean depositOrder = orderId.equals(b.getRazorpayOrderId());
        boolean balanceOrder =
                StringUtils.hasText(b.getRazorpayBalanceOrderId()) && orderId.equals(b.getRazorpayBalanceOrderId());
        if (!depositOrder && !balanceOrder) {
            return;
        }
        if (depositOrder && paymentId.equals(b.getRazorpayDepositPaymentId())) {
            return;
        }
        if (balanceOrder && paymentId.equals(b.getRazorpayBalancePaymentId())) {
            return;
        }
        BigDecimal paid =
                BigDecimal.valueOf(amountPaise).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        applyCapturedPayment(b, orderId, paymentId, paid);
        bookingRepository.save(b);
    }

    private void applyCapturedPayment(Booking b, String orderId, String paymentId, BigDecimal paid) {
        b.setRazorpayPaymentId(paymentId);
        b.setAmountPaid(b.getAmountPaid().add(paid).setScale(2, RoundingMode.HALF_UP));
        boolean balanceOrder =
                StringUtils.hasText(b.getRazorpayBalanceOrderId()) && orderId.equals(b.getRazorpayBalanceOrderId());
        if (balanceOrder) {
            b.setRazorpayBalancePaymentId(paymentId);
            b.setPaymentStatus(PaymentStatus.PAID);
            b.setRazorpayBalanceOrderId(null);
        } else {
            b.setRazorpayDepositPaymentId(paymentId);
            b.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        }
    }

    public boolean isDepositSatisfied(Booking b) {
        if (!razorpayProperties.requirePaidBeforeAccept()) {
            return true;
        }
        if (b.getPaymentStatus() == PaymentStatus.NOT_CONFIGURED) {
            return true;
        }
        if (b.getPaymentStatus() == PaymentStatus.PAID) {
            return true;
        }
        if (b.getDepositAmount() == null) {
            return b.getPaymentStatus() == PaymentStatus.PAID;
        }
        return b.getAmountPaid().compareTo(b.getDepositAmount()) >= 0;
    }

    public boolean isVisibleInOpenPool(Booking b) {
        return isDepositSatisfied(b);
    }

    private BigDecimal computeDeposit(BigDecimal price) {
        BigDecimal raw =
                price.multiply(razorpayProperties.depositFraction()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal min = new BigDecimal("1.00");
        return raw.max(min).min(price);
    }

    /** Maps satisfaction 1–5 to a multiplier applied to the quoted price (partial / adjustment). */
    public static BigDecimal satisfactionMultiplier(int satisfactionStars) {
        int s = Math.min(5, Math.max(1, satisfactionStars));
        BigDecimal stars = BigDecimal.valueOf(s);
        return new BigDecimal("0.78")
                .add(stars.multiply(new BigDecimal("0.044")))
                .min(BigDecimal.ONE)
                .max(new BigDecimal("0.78"));
    }

    private void verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);
            Utils.verifyPaymentSignature(attributes, razorpayProperties.keySecret());
        } catch (RazorpayException e) {
            throw new BadRequestException("Invalid Razorpay signature.");
        }
    }

    private BigDecimal fetchPaymentAmountInRupees(String paymentId) {
        try {
            RazorpayClient client = client();
            Payment payment = client.payments.fetch(paymentId);
            long paise = ((Number) payment.get("amount")).longValue();
            return BigDecimal.valueOf(paise).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } catch (RazorpayException e) {
            throw new BadRequestException("Could not fetch payment from Razorpay.");
        }
    }

    private Order createOrder(RazorpayClient client, BigDecimal amountInr, String receipt) throws RazorpayException {
        long paise = rupeesToPaise(amountInr);
        if (paise < 100) {
            paise = 100;
        }
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", paise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);
        return client.orders.create(orderRequest);
    }

    private long rupeesToPaise(BigDecimal inr) {
        return inr.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    private RazorpayClient client() throws RazorpayException {
        return new RazorpayClient(razorpayProperties.keyId(), razorpayProperties.keySecret());
    }

    private static String receiptFor(String prefix, long bookingId) {
        String r = prefix + bookingId;
        return r.length() > 40 ? r.substring(0, 40) : r;
    }

    public boolean isWebhookVerificationConfigured() {
        return StringUtils.hasText(razorpayProperties.webhookSecret());
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        if (!StringUtils.hasText(razorpayProperties.webhookSecret())) {
            return false;
        }
        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayProperties.webhookSecret());
            return true;
        } catch (RazorpayException e) {
            log.warn("Invalid Razorpay webhook signature");
            return false;
        }
    }
}

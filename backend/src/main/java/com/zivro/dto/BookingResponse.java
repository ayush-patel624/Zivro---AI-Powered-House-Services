package com.zivro.dto;

import com.zivro.domain.BookingStatus;
import com.zivro.domain.PaymentStatus;
import com.zivro.domain.UrgencyLevel;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingResponse {
    Long id;
    Long userId;
    Long workerId;
    String workerEmployeeId;
    ServiceResponse service;
    BookingStatus status;
    BigDecimal price;
    Instant bookingTime;
    UrgencyLevel urgencyLevel;
    BookingImageResponse images;
    RatingResponse rating;
    PaymentStatus paymentStatus;
    BigDecimal depositAmount;
    BigDecimal amountPaid;
    BigDecimal finalPriceAfterSatisfaction;
    /** Razorpay order id for the active checkout step (deposit or balance), if any. */
    String activeRazorpayOrderId;
    /** Amount in INR the customer should pay for {@link #activeRazorpayOrderId}, when present. */
    BigDecimal amountDueNext;
    String razorpayDepositOrderId;
    String razorpayBalanceOrderId;
    String serviceAddress;
    String locationLabel;
    Double latitude;
    Double longitude;
    String mapsUrl;
}

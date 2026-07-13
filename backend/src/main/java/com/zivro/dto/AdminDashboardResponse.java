package com.zivro.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminDashboardResponse {

    List<BookingStatusCount> bookingsByStatus;
    List<PaymentStatusCount> paymentsByStatus;
    BigDecimal totalAmountPaid;
    long totalUsers;
    long usersByRoleUser;
    long usersByRoleWorker;
    long usersByRoleAdmin;
    long workersVerified;
    long workersUnverified;
    long openDisputes;

    @Value
    @Builder
    public static class BookingStatusCount {
        String status;
        long count;
    }

    @Value
    @Builder
    public static class PaymentStatusCount {
        String status;
        long count;
    }
}

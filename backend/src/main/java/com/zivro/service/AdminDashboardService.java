package com.zivro.service;

import com.zivro.domain.BookingStatus;
import com.zivro.domain.DisputeStatus;
import com.zivro.domain.PaymentStatus;
import com.zivro.domain.Role;
import com.zivro.dto.AdminDashboardResponse;
import com.zivro.repository.BookingRepository;
import com.zivro.repository.DisputeRepository;
import com.zivro.repository.UserRepository;
import com.zivro.repository.WorkerRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final DisputeRepository disputeRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse snapshot() {
        List<AdminDashboardResponse.BookingStatusCount> byBooking = new ArrayList<>();
        for (Object[] row : bookingRepository.countBookingsGroupedByStatus()) {
            BookingStatus st = (BookingStatus) row[0];
            long c = (Long) row[1];
            byBooking.add(
                    AdminDashboardResponse.BookingStatusCount.builder()
                            .status(st.name())
                            .count(c)
                            .build());
        }
        List<AdminDashboardResponse.PaymentStatusCount> byPay = new ArrayList<>();
        for (PaymentStatus ps : PaymentStatus.values()) {
            long c = bookingRepository.countByPaymentStatus(ps);
            byPay.add(
                    AdminDashboardResponse.PaymentStatusCount.builder()
                            .status(ps.name())
                            .count(c)
                            .build());
        }
        BigDecimal totalPaid = bookingRepository.sumAmountPaidAll();
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;
        }
        long openDisputes = disputeRepository.countByStatus(DisputeStatus.OPEN);
        long verified = workerRepository.countByVerified(true);
        long unverified = workerRepository.countByVerified(false);
        return AdminDashboardResponse.builder()
                .bookingsByStatus(byBooking)
                .paymentsByStatus(byPay)
                .totalAmountPaid(totalPaid)
                .totalUsers(userRepository.count())
                .usersByRoleUser(userRepository.countByRole(Role.USER))
                .usersByRoleWorker(userRepository.countByRole(Role.WORKER))
                .usersByRoleAdmin(userRepository.countByRole(Role.ADMIN))
                .workersVerified(verified)
                .workersUnverified(unverified)
                .openDisputes(openDisputes)
                .build();
    }
}

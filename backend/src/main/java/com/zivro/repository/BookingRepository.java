package com.zivro.repository;

import com.zivro.domain.Booking;
import com.zivro.domain.BookingStatus;
import com.zivro.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"service", "user", "worker", "bookingImage", "rating"})
    List<Booking> findByUser_IdOrderByBookingTimeDesc(Long userId);

    @EntityGraph(attributePaths = {"service", "user", "worker", "bookingImage", "rating"})
    List<Booking> findByWorker_IdOrderByBookingTimeDesc(Long workerId);

    @EntityGraph(attributePaths = {"service", "user", "worker", "bookingImage", "rating"})
    List<Booking> findByStatusAndWorkerIsNullOrderByBookingTimeAsc(BookingStatus status);

    @EntityGraph(attributePaths = {"service", "user", "worker", "bookingImage", "rating"})
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findDetailById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "SELECT b FROM Booking b JOIN FETCH b.service JOIN FETCH b.user LEFT JOIN FETCH b.worker "
                    + "LEFT JOIN FETCH b.bookingImage LEFT JOIN FETCH b.rating WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    long countByService_Id(Long serviceId);

    @Query("SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status")
    List<Object[]> countBookingsGroupedByStatus();

    @Query("SELECT COALESCE(SUM(b.amountPaid), 0) FROM Booking b")
    BigDecimal sumAmountPaidAll();

    @Query(
            "SELECT b FROM Booking b WHERE b.razorpayOrderId = :orderId OR b.razorpayBalanceOrderId = :orderId")
    Optional<Booking> findByAnyRazorpayOrderId(@Param("orderId") String orderId);

    long countByPaymentStatus(PaymentStatus status);
}

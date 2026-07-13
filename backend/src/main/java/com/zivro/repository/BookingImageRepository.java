package com.zivro.repository;

import com.zivro.domain.BookingImage;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingImageRepository extends JpaRepository<BookingImage, Long> {

    Optional<BookingImage> findByBooking_Id(Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "SELECT i FROM BookingImage i JOIN FETCH i.booking b JOIN FETCH b.user LEFT JOIN FETCH b.worker WHERE b.id = :bookingId")
    Optional<BookingImage> lockByBookingId(@Param("bookingId") Long bookingId);
}

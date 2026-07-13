package com.zivro.repository;

import com.zivro.domain.BookingStatus;
import com.zivro.domain.Rating;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByBooking_Id(Long bookingId);

    Optional<Rating> findByBooking_Id(Long bookingId);

    @Query(
            "SELECT AVG(r.stars) FROM Rating r WHERE r.booking.worker.id = :workerId AND r.booking.status = :status")
    Double averageWorkerStars(
            @Param("workerId") Long workerId, @Param("status") BookingStatus status);
}

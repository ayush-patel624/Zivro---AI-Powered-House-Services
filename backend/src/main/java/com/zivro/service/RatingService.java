package com.zivro.service;

import com.zivro.domain.Booking;
import com.zivro.domain.BookingStatus;
import com.zivro.domain.Rating;
import com.zivro.domain.User;
import com.zivro.domain.Worker;
import com.zivro.dto.BookingResponse;
import com.zivro.dto.SubmitRatingRequest;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ConflictException;
import com.zivro.exception.ForbiddenException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.BookingRepository;
import com.zivro.repository.RatingRepository;
import com.zivro.repository.WorkerRepository;
import com.zivro.util.BookingMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final BookingRepository bookingRepository;
    private final RatingRepository ratingRepository;
    private final WorkerRepository workerRepository;
    private final BookingPaymentService bookingPaymentService;

    @Transactional
    public BookingResponse submitRating(Long bookingId, User customer, SubmitRatingRequest request) {
        Booking booking =
                bookingRepository
                        .findDetailById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (!booking.getUser().getId().equals(customer.getId())) {
            throw new ForbiddenException("Only the customer can rate this booking.");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("You can only rate completed bookings.");
        }
        if (ratingRepository.existsByBooking_Id(bookingId)) {
            throw new ConflictException("A rating has already been submitted for this booking.");
        }
        Rating rating =
                Rating.builder()
                        .booking(booking)
                        .stars(request.getWorkerStars())
                        .satisfactionStars(request.getSatisfactionStars())
                        .feedback(request.getFeedback())
                        .build();
        ratingRepository.save(rating);

        if (booking.getWorker() != null) {
            refreshWorkerRating(booking.getWorker().getId());
        }
        bookingPaymentService.applySatisfactionPricing(bookingId, request.getSatisfactionStars());
        return bookingRepository
                .findDetailById(bookingId)
                .map(BookingMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
    }

    private void refreshWorkerRating(Long workerId) {
        Worker worker =
                workerRepository.findById(workerId).orElseThrow(() -> new ResourceNotFoundException("Worker not found."));
        Double avg = ratingRepository.averageWorkerStars(workerId, BookingStatus.COMPLETED);
        if (avg == null || avg.isNaN()) {
            worker.setRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        } else {
            worker.setRating(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }
        workerRepository.save(worker);
    }
}

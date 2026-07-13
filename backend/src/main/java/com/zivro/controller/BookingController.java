package com.zivro.controller;

import com.zivro.domain.User;
import com.zivro.dto.BookingResponse;
import com.zivro.dto.CreateBookingRequest;
import com.zivro.dto.CreateDisputeRequest;
import com.zivro.dto.DisputeResponse;
import com.zivro.dto.NearbyWorkerResponse;
import com.zivro.dto.SubmitRatingRequest;
import com.zivro.dto.VerifyRazorpayPaymentRequest;
import com.zivro.service.BookingImageService;
import com.zivro.service.BookingPaymentService;
import com.zivro.service.BookingService;
import com.zivro.service.DisputeService;
import com.zivro.service.RatingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingImageService bookingImageService;
    private final RatingService ratingService;
    private final BookingPaymentService bookingPaymentService;
    private final DisputeService disputeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','WORKER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("booking") CreateBookingRequest request,
            @RequestPart("referenceImage") MultipartFile referenceImage) {
        return bookingService.create(user, request, referenceImage);
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal User user) {
        return bookingService.listMine(user);
    }

    @GetMapping("/worker")
    @PreAuthorize("hasRole('WORKER')")
    public List<BookingResponse> workerBookings(@AuthenticationPrincipal User user) {
        return bookingService.listForWorker(user);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('WORKER')")
    public List<BookingResponse> unassignedPool() {
        return bookingService.listOpenPool();
    }

    @GetMapping("/{id:\\d+}")
    public BookingResponse get(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.get(id, user);
    }

    @GetMapping("/{id:\\d+}/nearby-workers")
    public List<NearbyWorkerResponse> nearbyWorkers(
            @PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.nearbyWorkers(id, user);
    }

    @PostMapping("/{id:\\d+}/accept")
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse accept(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.accept(id, user);
    }

    @PostMapping("/{id:\\d+}/reject")
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse reject(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.reject(id, user);
    }

    @PostMapping("/{id:\\d+}/start")
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse start(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.start(id, user);
    }

    @PostMapping("/{id:\\d+}/complete")
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse complete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.complete(id, user);
    }

    @PostMapping("/{id:\\d+}/cancel")
    public BookingResponse cancel(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return bookingService.cancel(id, user);
    }

    @PostMapping(value = "/{id:\\d+}/images/before-work", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse uploadBeforeWork(
            @PathVariable Long id, @AuthenticationPrincipal User user, @RequestPart("file") MultipartFile file) {
        bookingImageService.uploadBeforeWork(id, user, file);
        return bookingService.get(id, user);
    }

    @PostMapping(value = "/{id:\\d+}/images/after-work", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('WORKER')")
    public BookingResponse uploadAfterWork(
            @PathVariable Long id, @AuthenticationPrincipal User user, @RequestPart("file") MultipartFile file) {
        bookingImageService.uploadAfterWork(id, user, file);
        return bookingService.get(id, user);
    }

    @PostMapping("/{id:\\d+}/rating")
    @PreAuthorize("hasAnyRole('USER','WORKER','ADMIN')")
    public BookingResponse submitRating(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubmitRatingRequest request) {
        return ratingService.submitRating(id, user, request);
    }

    @PostMapping("/{id:\\d+}/payments/verify")
    @PreAuthorize("hasAnyRole('USER','WORKER','ADMIN')")
    public BookingResponse verifyPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VerifyRazorpayPaymentRequest request) {
        return bookingPaymentService.verifyAndRecordPayment(
                id, user, request.getOrderId(), request.getPaymentId(), request.getSignature());
    }

    @PostMapping("/{id:\\d+}/disputes")
    @PreAuthorize("hasAnyRole('USER','WORKER','ADMIN')")
    public DisputeResponse createDispute(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateDisputeRequest request) {
        return disputeService.createForBooking(id, user, request);
    }
}

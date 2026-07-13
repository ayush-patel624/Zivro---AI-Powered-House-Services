package com.zivro.service;

import com.zivro.domain.Booking;
import com.zivro.domain.BookingImage;
import com.zivro.domain.BookingStatus;
import com.zivro.domain.Role;
import com.zivro.domain.User;
import com.zivro.domain.Worker;
import com.zivro.dto.BookingResponse;
import com.zivro.dto.CreateBookingRequest;
import com.zivro.dto.ImageAnalysisResponse;
import com.zivro.dto.LocationInput;
import com.zivro.dto.NearbyWorkerResponse;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ForbiddenException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.BookingImageRepository;
import com.zivro.repository.BookingRepository;
import com.zivro.repository.ServiceCatalogRepository;
import com.zivro.repository.WorkerRepository;
import com.zivro.util.BookingMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingImageRepository bookingImageRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final WorkerRepository workerRepository;
    private final PricingService pricingService;
    private final BookingImageService bookingImageService;
    private final BookingPaymentService bookingPaymentService;
    private final ImageAnalysisService imageAnalysisService;
    private final NearbyWorkerService nearbyWorkerService;

    @Transactional
    public BookingResponse create(User customer, CreateBookingRequest request, MultipartFile referenceImage) {
        if (referenceImage == null || referenceImage.isEmpty()) {
            throw new BadRequestException("A reference room/area image is required when creating a booking.");
        }
        LocationInput location = request.getLocation();
        if (location == null
                || location.getLatitude() == null
                || location.getLongitude() == null
                || location.getAddress() == null
                || location.getAddress().isBlank()) {
            throw new BadRequestException("Service location (address and coordinates) is required.");
        }
        var service =
                serviceCatalogRepository
                        .findById(request.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found."));
        Worker assignedWorker = null;
        if (request.getWorkerId() != null) {
            assignedWorker =
                    workerRepository
                            .findById(request.getWorkerId())
                            .orElseThrow(() -> new ResourceNotFoundException("Worker not found."));
            if (!assignedWorker.isVerified()) {
                throw new BadRequestException("Selected worker is not verified yet.");
            }
        }
        Instant when = request.getScheduledAt() != null ? request.getScheduledAt() : Instant.now();
        ImageAnalysisResponse analysis =
                imageAnalysisService.analyze(referenceImage, service.getIconKey());
        var price = pricingService.quote(service, request.getUrgencyLevel(), when);
        Booking booking =
                Booking.builder()
                        .user(customer)
                        .worker(assignedWorker)
                        .service(service)
                        .status(BookingStatus.PENDING)
                        .price(price)
                        .bookingTime(when)
                        .urgencyLevel(request.getUrgencyLevel())
                        .serviceAddress(location.getAddress().trim())
                        .locationLabel(
                                location.getLabel() != null && !location.getLabel().isBlank()
                                        ? location.getLabel().trim()
                                        : "Service location")
                        .latitude(BigDecimal.valueOf(location.getLatitude()))
                        .longitude(BigDecimal.valueOf(location.getLongitude()))
                        .build();
        booking = bookingRepository.save(booking);

        BookingImage imageRow = BookingImage.builder().booking(booking).build();
        imageAnalysisService.applyToBookingImage(imageRow, analysis);
        bookingImageRepository.save(imageRow);

        bookingImageService.uploadReference(booking.getId(), customer, referenceImage);
        bookingPaymentService.initializeDepositForBooking(booking.getId());
        return loadResponse(booking.getId());
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id, User principal) {
        Booking b =
                bookingRepository
                        .findDetailById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        assertViewAllowed(b, principal);
        return BookingMapper.toResponse(b);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMine(User customer) {
        return bookingRepository.findByUser_IdOrderByBookingTimeDesc(customer.getId()).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listForWorker(User user) {
        Worker w = requireWorkerProfile(user);
        return bookingRepository.findByWorker_IdOrderByBookingTimeDesc(w.getId()).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listOpenPool() {
        return bookingRepository.findByStatusAndWorkerIsNullOrderByBookingTimeAsc(BookingStatus.PENDING).stream()
                .filter(bookingPaymentService::isVisibleInOpenPool)
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NearbyWorkerResponse> nearbyWorkers(Long bookingId, User principal) {
        Booking b =
                bookingRepository
                        .findDetailById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        assertViewAllowed(b, principal);
        if (b.getStatus() != BookingStatus.PENDING || b.getWorker() != null) {
            return List.of();
        }
        return nearbyWorkerService.nearbyForBooking(bookingId);
    }

    @Transactional
    public BookingResponse accept(Long bookingId, User user) {
        Worker worker = requireWorkerProfile(user);
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (b.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be accepted.");
        }
        if (b.getWorker() == null) {
            b.setWorker(worker);
            b.setStatus(BookingStatus.ACCEPTED);
        } else if (b.getWorker().getId().equals(worker.getId())) {
            b.setStatus(BookingStatus.ACCEPTED);
        } else {
            throw new ForbiddenException("Another worker is assigned to this booking.");
        }
        if (!bookingPaymentService.isDepositSatisfied(b)) {
            throw new BadRequestException(
                    "Customer deposit has not been received yet. The booking cannot be accepted.");
        }
        return BookingMapper.toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingResponse reject(Long bookingId, User user) {
        Worker worker = requireWorkerProfile(user);
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (b.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only PENDING bookings can be rejected.");
        }
        if (b.getWorker() == null || !b.getWorker().getId().equals(worker.getId())) {
            throw new ForbiddenException("You are not the assigned worker for this booking.");
        }
        b.setWorker(null);
        return BookingMapper.toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingResponse start(Long bookingId, User user) {
        Worker worker = requireWorkerProfile(user);
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (b.getStatus() != BookingStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED bookings can be started.");
        }
        if (b.getWorker() == null || !b.getWorker().getId().equals(worker.getId())) {
            throw new ForbiddenException("You are not the assigned worker.");
        }
        assertBeforeWorkPresent(b);
        b.setStatus(BookingStatus.IN_PROGRESS);
        return BookingMapper.toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingResponse complete(Long bookingId, User user) {
        Worker worker = requireWorkerProfile(user);
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (b.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new BadRequestException("Only IN_PROGRESS bookings can be completed.");
        }
        if (b.getWorker() == null || !b.getWorker().getId().equals(worker.getId())) {
            throw new ForbiddenException("You are not the assigned worker.");
        }
        assertAfterWorkPresent(b);
        b.setStatus(BookingStatus.COMPLETED);
        return BookingMapper.toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, User user) {
        Booking b =
                bookingRepository
                        .findByIdForUpdate(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.ACCEPTED) {
            throw new BadRequestException("This booking can no longer be cancelled.");
        }
        if (user.getRole() == Role.ADMIN) {
            b.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(b);
            bookingImageService.cleanupOnCancel(bookingId);
            return loadResponse(bookingId);
        }
        if (b.getUser().getId().equals(user.getId())) {
            b.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(b);
            bookingImageService.cleanupOnCancel(bookingId);
            return loadResponse(bookingId);
        }
        Worker w = workerRepository.findByUser(user).orElse(null);
        if (w != null
                && b.getWorker() != null
                && b.getWorker().getId().equals(w.getId())
                && b.getStatus() == BookingStatus.ACCEPTED) {
            b.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(b);
            bookingImageService.cleanupOnCancel(bookingId);
            return loadResponse(bookingId);
        }
        throw new ForbiddenException("Not allowed to cancel this booking.");
    }

    private void assertBeforeWorkPresent(Booking b) {
        BookingImage img = b.getBookingImage();
        if (img == null || img.getBeforeImage() == null || img.getBeforeImage().isBlank()) {
            throw new BadRequestException("Upload the BEFORE_WORK image before starting the job.");
        }
    }

    private void assertAfterWorkPresent(Booking b) {
        BookingImage img = b.getBookingImage();
        if (img == null || img.getAfterImage() == null || img.getAfterImage().isBlank()) {
            throw new BadRequestException("Upload the AFTER_WORK image before completing the job.");
        }
    }

    private BookingResponse loadResponse(Long id) {
        return bookingRepository
                .findDetailById(id)
                .map(BookingMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
    }

    private void assertViewAllowed(Booking b, User principal) {
        if (principal.getRole() == Role.ADMIN) {
            return;
        }
        if (b.getUser().getId().equals(principal.getId())) {
            return;
        }
        Worker w = workerRepository.findByUser(principal).orElse(null);
        if (w != null && b.getWorker() != null && b.getWorker().getId().equals(w.getId())) {
            return;
        }
        throw new ForbiddenException("You cannot view this booking.");
    }

    private Worker requireWorkerProfile(User user) {
        return workerRepository
                .findByUser(user)
                .orElseThrow(() -> new ForbiddenException("Worker profile required."));
    }
}

package com.zivro.service;

import com.zivro.domain.Booking;
import com.zivro.domain.BookingImage;
import com.zivro.domain.BookingStatus;
import com.zivro.domain.User;
import com.zivro.domain.Worker;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ConflictException;
import com.zivro.exception.ForbiddenException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.media.CloudinaryService;
import com.zivro.media.CloudinaryUploadResult;
import com.zivro.repository.BookingImageRepository;
import com.zivro.repository.WorkerRepository;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BookingImageService {

    private static final long MAX_BYTES = 8 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final BookingImageRepository bookingImageRepository;
    private final WorkerRepository workerRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public void uploadReference(Long bookingId, User customer, MultipartFile file) {
        validateFile(file);
        BookingImage img = loadLocked(bookingId);
        Booking b = img.getBooking();
        assertCustomer(b, customer);
        if (b.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Reference image can only be attached while booking is PENDING.");
        }
        if (img.getReferenceImageUrl() != null && !img.getReferenceImageUrl().isBlank()) {
            throw new ConflictException("Reference image is already set for this booking.");
        }
        String path = "bookings/" + bookingId + "/reference";
        CloudinaryUploadResult up = upload(file, path);
        img.setReferenceImageUrl(up.secureUrl());
        img.setReferencePublicId(up.publicId());
        bookingImageRepository.save(img);
    }

    @Transactional
    public void uploadBeforeWork(Long bookingId, User user, MultipartFile file) {
        validateFile(file);
        Worker worker = requireWorker(user);
        BookingImage img = loadLocked(bookingId);
        Booking b = img.getBooking();
        assertAssignedWorker(b, worker);
        if (b.getStatus() != BookingStatus.ACCEPTED) {
            throw new BadRequestException("BEFORE_WORK image can only be uploaded when booking is ACCEPTED.");
        }
        if (img.getBeforePublicId() != null && !img.getBeforePublicId().isBlank()) {
            cloudinaryService.deletePublicId(img.getBeforePublicId());
        }
        String path = "bookings/" + bookingId + "/before_work";
        CloudinaryUploadResult up = upload(file, path);
        img.setBeforeImage(up.secureUrl());
        img.setBeforePublicId(up.publicId());
        bookingImageRepository.save(img);
    }

    @Transactional
    public void uploadAfterWork(Long bookingId, User user, MultipartFile file) {
        validateFile(file);
        Worker worker = requireWorker(user);
        BookingImage img = loadLocked(bookingId);
        Booking b = img.getBooking();
        assertAssignedWorker(b, worker);
        if (b.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new BadRequestException("AFTER_WORK image can only be uploaded when booking is IN_PROGRESS.");
        }
        if (img.getAfterPublicId() != null && !img.getAfterPublicId().isBlank()) {
            cloudinaryService.deletePublicId(img.getAfterPublicId());
        }
        String path = "bookings/" + bookingId + "/after_work";
        CloudinaryUploadResult up = upload(file, path);
        img.setAfterImage(up.secureUrl());
        img.setAfterPublicId(up.publicId());
        bookingImageRepository.save(img);
    }

    @Transactional
    public void cleanupOnCancel(Long bookingId) {
        bookingImageRepository
                .findByBooking_Id(bookingId)
                .ifPresent(
                        img -> {
                            cloudinaryService.deletePublicId(img.getReferencePublicId());
                            cloudinaryService.deletePublicId(img.getBeforePublicId());
                            cloudinaryService.deletePublicId(img.getAfterPublicId());
                            img.setReferenceImageUrl(null);
                            img.setReferencePublicId(null);
                            img.setBeforeImage(null);
                            img.setBeforePublicId(null);
                            img.setAfterImage(null);
                            img.setAfterPublicId(null);
                            bookingImageRepository.save(img);
                        });
    }

    private CloudinaryUploadResult upload(MultipartFile file, String publicIdPath) {
        try {
            String ct = normalizeContentType(file.getContentType());
            byte[] data = file.getBytes();
            return cloudinaryService.uploadImage(data, ct, publicIdPath);
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file.");
        }
    }

    private BookingImage loadLocked(Long bookingId) {
        return bookingImageRepository
                .lockByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking image record not found."));
    }

    private void assertCustomer(Booking b, User customer) {
        if (!b.getUser().getId().equals(customer.getId())) {
            throw new ForbiddenException("Only the booking owner can upload the reference image.");
        }
    }

    private void assertAssignedWorker(Booking b, Worker worker) {
        if (b.getWorker() == null || !b.getWorker().getId().equals(worker.getId())) {
            throw new ForbiddenException("Only the assigned worker can upload this image.");
        }
    }

    private Worker requireWorker(User user) {
        return workerRepository
                .findByUser(user)
                .orElseThrow(() -> new ForbiddenException("Worker profile required."));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Image must be at most 8 MB.");
        }
        String ct = normalizeContentType(file.getContentType());
        if (ct != null && ALLOWED_TYPES.contains(ct)) {
            return;
        }
        String name = file.getOriginalFilename();
        if (name != null && name.matches("(?i).+\\.(jpe?g|png|webp|gif)$")) {
            return;
        }
        if (ct == null || ct.startsWith("image/") || "application/octet-stream".equals(ct)) {
            return;
        }
        throw new BadRequestException("Only JPEG, PNG, WebP, or GIF images are allowed.");
    }

    private static String normalizeContentType(String raw) {
        if (raw == null) {
            return null;
        }
        String lower = raw.toLowerCase(Locale.ROOT).trim();
        int semi = lower.indexOf(';');
        return semi > 0 ? lower.substring(0, semi).trim() : lower;
    }
}

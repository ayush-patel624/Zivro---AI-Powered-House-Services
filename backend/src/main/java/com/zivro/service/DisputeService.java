package com.zivro.service;

import com.zivro.domain.Booking;
import com.zivro.domain.Dispute;
import com.zivro.domain.DisputeStatus;
import com.zivro.domain.User;
import com.zivro.dto.AdminDisputeUpdateRequest;
import com.zivro.dto.CreateDisputeRequest;
import com.zivro.dto.DisputeResponse;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ConflictException;
import com.zivro.exception.ForbiddenException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.BookingRepository;
import com.zivro.repository.DisputeRepository;
import com.zivro.util.DisputeMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public DisputeResponse createForBooking(Long bookingId, User user, CreateDisputeRequest request) {
        Booking booking =
                bookingRepository
                        .findDetailById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the customer can open a dispute for this booking.");
        }
        if (disputeRepository.existsByBooking_Id(bookingId)) {
            throw new ConflictException("A dispute already exists for this booking.");
        }
        Instant now = Instant.now();
        Dispute d =
                Dispute.builder()
                        .booking(booking)
                        .status(DisputeStatus.OPEN)
                        .reason(request.getReason().trim())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
        disputeRepository.save(d);
        return DisputeMapper.toResponse(d);
    }

    @Transactional(readOnly = true)
    public List<DisputeResponse> listAll() {
        return disputeRepository.findAllByOrderByCreatedAtDesc().stream().map(DisputeMapper::toResponse).toList();
    }

    @Transactional
    public DisputeResponse adminUpdate(Long disputeId, AdminDisputeUpdateRequest request) {
        Dispute d =
                disputeRepository
                        .findById(disputeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Dispute not found."));
        if (request.getStatus() == DisputeStatus.OPEN && d.getStatus() != DisputeStatus.OPEN) {
            throw new BadRequestException("Cannot set status back to OPEN.");
        }
        d.setStatus(request.getStatus());
        d.setResolutionNotes(request.getResolutionNotes());
        d.setUpdatedAt(Instant.now());
        disputeRepository.save(d);
        return DisputeMapper.toResponse(d);
    }
}

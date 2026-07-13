package com.zivro.dto;

import com.zivro.domain.DisputeStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DisputeResponse {
    Long id;
    Long bookingId;
    DisputeStatus status;
    String reason;
    String resolutionNotes;
    Instant createdAt;
    Instant updatedAt;
}

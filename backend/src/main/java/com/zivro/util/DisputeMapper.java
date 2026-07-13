package com.zivro.util;

import com.zivro.domain.Dispute;
import com.zivro.dto.DisputeResponse;

public final class DisputeMapper {

    private DisputeMapper() {}

    public static DisputeResponse toResponse(Dispute d) {
        return DisputeResponse.builder()
                .id(d.getId())
                .bookingId(d.getBooking().getId())
                .status(d.getStatus())
                .reason(d.getReason())
                .resolutionNotes(d.getResolutionNotes())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}

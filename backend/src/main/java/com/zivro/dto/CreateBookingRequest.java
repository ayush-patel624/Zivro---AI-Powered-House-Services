package com.zivro.dto;

import com.zivro.domain.UrgencyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Data;

@Data
public class CreateBookingRequest {

    @NotNull
    private Long serviceId;

    @NotNull
    private UrgencyLevel urgencyLevel;

    /** When set, this worker must accept before the job is confirmed. */
    private Long workerId;

    /** Defaults to now if omitted. */
    private Instant scheduledAt;

    @Valid
    @NotNull
    private LocationInput location;
}

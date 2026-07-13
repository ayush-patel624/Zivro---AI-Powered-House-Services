package com.zivro.dto;

import com.zivro.domain.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminDisputeUpdateRequest {

    @NotNull private DisputeStatus status;

    @Size(max = 4000)
    private String resolutionNotes;
}

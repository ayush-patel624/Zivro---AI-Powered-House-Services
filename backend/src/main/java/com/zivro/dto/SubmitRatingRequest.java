package com.zivro.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitRatingRequest {

    /** Rating for the worker (updates worker aggregate). */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer workerStars;

    /** Overall satisfaction with the completed job. */
    @NotNull
    @Min(1)
    @Max(5)
    private Integer satisfactionStars;

    @Size(max = 2000)
    private String feedback;
}

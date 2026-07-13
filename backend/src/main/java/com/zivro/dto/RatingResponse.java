package com.zivro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RatingResponse {
    int workerStars;
    int satisfactionStars;
    String feedback;
}

package com.zivro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImageAnalysisResponse {
    String detectedType;
    String label;
    Integer quantity;
    String quantityUnit;
    Integer estimatedMinutes;
    String stainLevel;
    Double confidence;
    String summary;
}

package com.zivro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingImageResponse {
    String referenceImageUrl;
    String beforeWorkImageUrl;
    String afterWorkImageUrl;
    ImageAnalysisResponse aiAnalysis;
}

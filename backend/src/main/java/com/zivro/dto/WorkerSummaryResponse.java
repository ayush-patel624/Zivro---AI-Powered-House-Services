package com.zivro.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WorkerSummaryResponse {
    Long id;
    String employeeId;
    String category;
    BigDecimal rating;
    boolean verified;
    boolean depositPaid;
}

package com.zivro.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ServiceUpdateRequest {

    @Size(max = 160)
    private String name;

    @Size(max = 2000)
    private String description;

    @Positive
    private BigDecimal basePrice;
}

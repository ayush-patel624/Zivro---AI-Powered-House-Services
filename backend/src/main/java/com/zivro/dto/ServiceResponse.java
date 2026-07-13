package com.zivro.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServiceResponse {
    Long id;
    String name;
    String description;
    BigDecimal basePrice;
    String category;
    String iconKey;
    Integer sortOrder;
}

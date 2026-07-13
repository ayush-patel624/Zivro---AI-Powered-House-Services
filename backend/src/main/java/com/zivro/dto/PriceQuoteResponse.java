package com.zivro.dto;

import com.zivro.domain.UrgencyLevel;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PriceQuoteResponse {
    Long serviceId;
    UrgencyLevel urgencyLevel;
    BigDecimal quotedPrice;
    String currency;
}

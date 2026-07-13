package com.zivro.service;

import com.zivro.domain.ServiceCatalog;
import com.zivro.domain.UrgencyLevel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /**
     * Quoted price: base × urgency × peak-hour factor × simple demand stub.
     */
    public BigDecimal quote(ServiceCatalog service, UrgencyLevel urgency, Instant when) {
        BigDecimal base = service.getBasePrice();
        BigDecimal urgencyMult =
                switch (urgency) {
                    case NORMAL -> BigDecimal.ONE;
                    case URGENT -> new BigDecimal("1.22");
                    case SAME_DAY -> new BigDecimal("1.45");
                };
        BigDecimal peak = peakMultiplier(when);
        BigDecimal demand = new BigDecimal("1.04");
        return base.multiply(urgencyMult)
                .multiply(peak)
                .multiply(demand)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal peakMultiplier(Instant when) {
        ZonedDateTime z = when.atZone(IST);
        int hour = z.getHour();
        boolean morningPeak = hour >= 8 && hour < 11;
        boolean eveningPeak = hour >= 17 && hour < 21;
        if (morningPeak || eveningPeak) {
            return new BigDecimal("1.12");
        }
        return BigDecimal.ONE;
    }
}

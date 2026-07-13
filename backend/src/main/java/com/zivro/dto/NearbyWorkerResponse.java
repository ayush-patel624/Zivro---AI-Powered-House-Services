package com.zivro.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NearbyWorkerResponse {
    Long workerId;
    String employeeId;
    String name;
    String category;
    BigDecimal rating;
    boolean verified;
    double distanceKm;
    int etaMinutes;
    double latitude;
    double longitude;
}

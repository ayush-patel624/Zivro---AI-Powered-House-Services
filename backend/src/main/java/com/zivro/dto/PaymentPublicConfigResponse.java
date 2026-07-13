package com.zivro.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentPublicConfigResponse {
    boolean razorpayEnabled;
    String razorpayKeyId;
}

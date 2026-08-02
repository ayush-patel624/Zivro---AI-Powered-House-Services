package com.zivro.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;

@Value
@Builder
public class ApiError {
    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
    Map<String, String> fieldErrors;
}

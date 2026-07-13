package com.zivro.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zivro.cors")
public record CorsProperties(String allowedOrigins) {

    private static final List<String> LOCAL_DEFAULTS =
            List.of(
                    "http://localhost:5173",
                    "http://127.0.0.1:5173",
                    "http://localhost:4173",
                    "http://127.0.0.1:4173");

    public List<String> resolvedOrigins() {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return LOCAL_DEFAULTS;
        }
        List<String> configured =
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
        return configured.isEmpty() ? LOCAL_DEFAULTS : configured;
    }
}

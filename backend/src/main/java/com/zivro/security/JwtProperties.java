package com.zivro.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zivro.jwt")
public record JwtProperties(String secret, long expirationMs) {

    public JwtProperties {
        if (expirationMs <= 0) {
            expirationMs = 86_400_000L;
        }
    }
}

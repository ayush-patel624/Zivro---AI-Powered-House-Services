package com.zivro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zivro.cloudinary")
public record CloudinaryProperties(
        boolean enabled,
        String cloudName,
        String apiKey,
        String apiSecret,
        String uploadFolder) {

    public CloudinaryProperties {
        if (uploadFolder == null || uploadFolder.isBlank()) {
            uploadFolder = "zivro";
        }
    }

    public boolean isConfigured() {
        return cloudName != null
                && !cloudName.isBlank()
                && apiKey != null
                && !apiKey.isBlank()
                && apiSecret != null
                && !apiSecret.isBlank();
    }
}

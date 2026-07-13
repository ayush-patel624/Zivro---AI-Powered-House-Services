package com.zivro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zivro.bootstrap-admin")
public record BootstrapAdminProperties(String email, String password, String name) {

    public boolean shouldRun() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }
}

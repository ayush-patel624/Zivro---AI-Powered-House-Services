package com.zivro.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RazorpayProperties.class, BootstrapAdminProperties.class})
public class RazorpayConfiguration {}

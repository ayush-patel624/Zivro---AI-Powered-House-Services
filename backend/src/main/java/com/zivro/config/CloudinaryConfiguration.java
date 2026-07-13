package com.zivro.config;

import com.zivro.media.CloudinaryDisabledService;
import com.zivro.media.CloudinaryService;
import com.zivro.media.CloudinaryServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(CloudinaryService.class)
    CloudinaryService cloudinaryService(CloudinaryProperties properties) {
        if (properties.enabled() && properties.isConfigured()) {
            return new CloudinaryServiceImpl(properties);
        }
        return new CloudinaryDisabledService();
    }
}

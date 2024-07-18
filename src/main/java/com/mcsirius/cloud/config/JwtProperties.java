package com.mcsirius.cloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private Resource location;
    private String alias;
    private String password;
    private Duration tokenTTL = Duration.ofMinutes(10);
}

package com.mcsirius.cloud.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public KeyPair keyPair(JwtProperties jwtProperties) {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(jwtProperties.getLocation(), jwtProperties.getPassword().toCharArray());
        return keyStoreKeyFactory.getKeyPair(jwtProperties.getAlias(), jwtProperties.getPassword().toCharArray());
    }
}

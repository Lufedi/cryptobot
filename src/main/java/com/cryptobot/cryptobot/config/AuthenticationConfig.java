package com.cryptobot.cryptobot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Represents the Authentication configuration
 * @author Lufedi
 */
@Configuration
@PropertySource("classpath:exchange.properties")
public class AuthenticationConfig {

    @Value("${exchange.config.api.key}")
    private  String key;

    @Value("${exchange.config.api.secret}")
    private  String secret;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}

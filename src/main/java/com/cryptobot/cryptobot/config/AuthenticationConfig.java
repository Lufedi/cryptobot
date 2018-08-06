package com.cryptobot.cryptobot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Represents the Authentication configuration
 * Look for api and secret key in exchange.properties file
 *
 * CAUTION SHOWING THE DATA IN THIS CLASS
 * DO NOT UPLOAD YOUR PROPERTIES FILE TO PUBLIC REPOSITORIES
 *
 * @author Lufedi
 */
@Configuration
@PropertySource("classpath:exchange.properties")
public class AuthenticationConfig {

    /**
     * API Key used in the exchange
     */
    @Value("${exchange.config.api.key}")
    private  String key;
    /**
     * Secret key used in the exchange
     */
    @Value("${exchange.config.api.secret}")
    private  String secret;
    /**
     * Exchange name
     */
    @Value("${exchange.config.name}")
    private String exchangeName;

    public String getExchangeName() { return exchangeName; }

    public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }

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

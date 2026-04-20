package com.dsanext.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe binding for dsanext.jwt.* properties.
 */
@ConfigurationProperties(prefix = "dsanext.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        long refreshExpirationMs
) {}

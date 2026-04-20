package com.dsanext.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for DSANext API.
 * Allowed origins are driven by the dsanext.cors.allowed-origins property.
 */
@Configuration
public class CorsConfig {

    @Value("${dsanext.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${dsanext.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${dsanext.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origins
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins.stream().map(String::trim).toList());

        // Methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        config.setAllowedMethods(methods.stream().map(String::trim).toList());

        // Headers — allow all request headers
        config.setAllowedHeaders(List.of("*"));

        // Expose Authorization header so frontend can read JWT
        config.setExposedHeaders(List.of("Authorization", "X-Total-Count", "X-Total-Pages"));

        config.setAllowCredentials(true);
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

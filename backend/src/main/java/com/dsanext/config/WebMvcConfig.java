package com.dsanext.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures Spring MVC to serve uploaded profile images
 * from the filesystem as static resources under /uploads/**.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${dsanext.upload.profile-image-dir:./uploads/profiles}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve profile images from disk
        registry
            .addResourceHandler("/uploads/profiles/**")
            .addResourceLocations("file:" + uploadDir + "/")
            .setCachePeriod(3600); // 1 hour cache
    }
}

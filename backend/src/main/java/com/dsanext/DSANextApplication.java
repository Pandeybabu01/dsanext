//package com.dsanext;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.scheduling.annotation.EnableAsync;
//
///**
// * DSANext — Main Application Entry Point
// * <p>
// * A production-grade SaaS DSA Practice Platform.
// * </p>
// */
//@SpringBootApplication
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
//@ConfigurationPropertiesScan
//@EnableAsync
//public class DSANextApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(DSANextApplication.class, args);
//    }
//}

package com.dsanext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.dsanext")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@ConfigurationPropertiesScan
@EnableAsync
public class DSANextApplication {

    public static void main(String[] args) {
        SpringApplication.run(DSANextApplication.class, args);
    }
}
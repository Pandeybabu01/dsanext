package com.dsanext.controller;

import com.dsanext.dto.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root API endpoint — returns basic API info.
 * Prevents the 500 error when hitting /api/ directly.
 */
@RestController
@RequestMapping("/")
public class RootController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> root() {
        return ResponseEntity.ok(ApiResponse.success("DSANext API is running", Map.of(
                "name",    "DSANext API",
                "version", "1.0.0",
                "status",  "UP",
                "docs",    "/api/actuator/health"
        )));
    }
}
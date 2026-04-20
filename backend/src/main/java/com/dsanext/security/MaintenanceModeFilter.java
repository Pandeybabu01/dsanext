package com.dsanext.security;

import com.dsanext.service.SettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Maintenance mode filter.
 * When app.maintenance_mode = true:
 *   - ADMIN users: full access allowed
 *   - All other users: 503 JSON response with maintenance message
 *   - /auth/** and /settings/public: always allowed (for frontend bootstrap)
 *
 * Runs after JWT filter so authentication state is available.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SettingService settingService;
    private final ObjectMapper   objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Always pass through auth and public settings endpoints
        if (path.startsWith("/auth/") || path.equals("/settings/public")
                || path.startsWith("/actuator/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check maintenance mode setting
        boolean maintenanceMode = settingService.isMaintenanceMode();

        if (maintenanceMode) {
            // Allow admins through
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.isAuthenticated()
                    && auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            if (!isAdmin) {
                String maintenanceMessage = "DSANext is currently undergoing maintenance. Please check back shortly.";

                try {
                    maintenanceMessage = settingService
                            .getAppSetting("app.maintenance_message").getSettingValue();
                } catch (Exception ignored) { }

                log.info("Maintenance mode: blocked request to [{}]", path);

                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                Map<String, Object> body = Map.of(
                        "success",     false,
                        "message",     maintenanceMessage,
                        "error",       "Service Unavailable",
                        "maintenance", true,
                        "timestamp",   Instant.now().toString()
                );
                objectMapper.writeValue(response.getOutputStream(), body);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}

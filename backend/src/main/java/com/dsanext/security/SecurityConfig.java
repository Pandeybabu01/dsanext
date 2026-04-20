//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfigurationSource;
//
///**
// * DSANext Spring Security Configuration.
// *
// * Strategy:
// *  - Stateless JWT (no sessions, no cookies)
// *  - BCrypt password encoding (cost factor 12)
// *  - Method-level security enabled (@PreAuthorize)
// *  - Custom 401/403 JSON handlers (no HTML redirects)
// *  - CORS configured from CorsConfig bean
// *
// * Route access rules (least-privilege, explicit allow-list):
// *  PUBLIC      — auth endpoints, public problem listing, platform listing, public settings
// *  JWT         — all user-specific endpoints (progress, notes, bookmarks, notifications)
// *  ADMIN       — /admin/**, /analytics/admin, and admin sub-paths on platform/settings
// */
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final AuthService             authService;
//    private final JwtAuthFilter           jwtAuthFilter;
//    private final JwtAuthEntryPoint       jwtAuthEntryPoint;
//    private final JwtAccessDeniedHandler  jwtAccessDeniedHandler;
//    private final CorsConfigurationSource corsConfigurationSource;
//
//    // ── Public endpoints (no JWT required) ──────────────────
//
//    private static final String[] PUBLIC_GET_ENDPOINTS = {
//            "/auth/me",              // Returns null user gracefully
//            "/problems",             // Public problem listing
//            "/problems/**",          // Problem detail
//            "/platforms",            // Platform listing for filter dropdowns
//            "/platforms/**",         // Individual platform detail
//            "/settings/public",      // Public app config for frontend bootstrap
//            "/uploads/**",           // Served profile images (static files)
//            "/actuator/health",      // Health check
//            "/actuator/info",        // App info
//    };
//
//    private static final String[] PUBLIC_POST_ENDPOINTS = {
//            "/auth/register",
//            "/auth/login",
//    };
//
//    // ── Security filter chain ────────────────────────────────
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            // ── CSRF — disabled (stateless JWT API) ──────────
//            .csrf(AbstractHttpConfigurer::disable)
//
//            // ── CORS — use our CorsConfig bean ───────────────
//            .cors(cors -> cors.configurationSource(corsConfigurationSource))
//
//            // ── Session — stateless, never create HTTP sessions
//            .sessionManagement(session ->
//                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//            // ── Exception handling ────────────────────────────
//            .exceptionHandling(exceptions -> exceptions
//                    .authenticationEntryPoint(jwtAuthEntryPoint)
//                    .accessDeniedHandler(jwtAccessDeniedHandler))
//
//            // ── Authorization rules ───────────────────────────
//            .authorizeHttpRequests(auth -> auth
//
//                // Public GET endpoints
//                .requestMatchers(HttpMethod.GET,  PUBLIC_GET_ENDPOINTS).permitAll()
//
//                // Public POST endpoints (auth)
//                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
//
//                // Admin-only paths — enforced at URL level AND @PreAuthorize at method level
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//                .requestMatchers("/analytics/admin").hasRole("ADMIN")
//                .requestMatchers(HttpMethod.POST,   "/platforms/**").hasRole("ADMIN")
//                .requestMatchers(HttpMethod.PUT,    "/platforms/**").hasRole("ADMIN")
//                .requestMatchers(HttpMethod.DELETE, "/platforms/**").hasRole("ADMIN")
//                .requestMatchers("/settings/admin/**").hasRole("ADMIN")
//
//                // All other requests require authentication
//                .anyRequest().authenticated()
//            )
//
//            // ── Authentication provider ───────────────────────
//            .authenticationProvider(authenticationProvider())
//
//            // ── JWT filter before username/password filter ────
//            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // ── Beans ─────────────────────────────────────────────────
//
//    /**
//     * BCrypt with cost factor 12 — strong enough for production,
//     * benchmark ~250ms on modern hardware per hash.
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
//
//    /**
//     * DaoAuthenticationProvider — wires UserDetailsService + PasswordEncoder.
//     * Used by AuthenticationManager during login.
//     */
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(authService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    /**
//     * AuthenticationManager — exposed as a bean so AuthController can inject it.
//     * Spring Boot 3 requires explicit extraction from AuthenticationConfiguration.
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}






//package com.dsanext.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfigurationSource;
//
///**
// * DSANext Spring Security Configuration.
// *
// * Strategy:
// *  - Stateless JWT (no sessions, no cookies)
// *  - BCrypt password encoding (cost factor 12)
// *  - Method-level security enabled (@PreAuthorize)
// *  - Custom 401/403 JSON handlers (no HTML redirects)
// *  - CORS configured from CorsConfig bean
// *
// * Route access rules (least-privilege, explicit allow-list):
// *  PUBLIC      — auth endpoints, public problem listing, platform listing, public settings
// *  JWT         — all user-specific endpoints (progress, notes, bookmarks, notifications)
// *  ADMIN       — /admin/**, /analytics/admin, and admin sub-paths on platform/settings
// */
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//public class SecurityConfig {
//
//    private final UserDetailsService      userDetailsService;
//    private final JwtAuthFilter           jwtAuthFilter;
//    private final JwtAuthEntryPoint       jwtAuthEntryPoint;
//    private final JwtAccessDeniedHandler  jwtAccessDeniedHandler;
//    private final CorsConfigurationSource corsConfigurationSource;
//
//    public SecurityConfig(
//            @Lazy UserDetailsService userDetailsService,
//            JwtAuthFilter jwtAuthFilter,
//            JwtAuthEntryPoint jwtAuthEntryPoint,
//            JwtAccessDeniedHandler jwtAccessDeniedHandler,
//            CorsConfigurationSource corsConfigurationSource) {
//        this.userDetailsService     = userDetailsService;
//        this.jwtAuthFilter          = jwtAuthFilter;
//        this.jwtAuthEntryPoint      = jwtAuthEntryPoint;
//        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
//        this.corsConfigurationSource= corsConfigurationSource;
//    }
//
//    // ── Public endpoints (no JWT required) ──────────────────
//
//    private static final String[] PUBLIC_GET_ENDPOINTS = {
//            "/auth/me",              // Returns null user gracefully
//            "/problems",             // Public problem listing
//            "/problems/**",          // Problem detail
//            "/platforms",            // Platform listing for filter dropdowns
//            "/platforms/**",         // Individual platform detail
//            "/settings/public",      // Public app config for frontend bootstrap
//            "/uploads/**",           // Served profile images (static files)
//            "/actuator/health",      // Health check
//            "/actuator/info",        // App info
//    };
//
//    private static final String[] PUBLIC_POST_ENDPOINTS = {
//            "/auth/register",
//            "/auth/login",
//    };
//
//    // ── Security filter chain ────────────────────────────────
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // ── CSRF — disabled (stateless JWT API) ──────────
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // ── CORS — use our CorsConfig bean ───────────────
//                .cors(cors -> cors.configurationSource(corsConfigurationSource))
//
//                // ── Session — stateless, never create HTTP sessions
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // ── Exception handling ────────────────────────────
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint(jwtAuthEntryPoint)
//                        .accessDeniedHandler(jwtAccessDeniedHandler))
//
//                // ── Authorization rules ───────────────────────────
//                .authorizeHttpRequests(auth -> auth
//
//                        // Public GET endpoints
//                        .requestMatchers(HttpMethod.GET,  PUBLIC_GET_ENDPOINTS).permitAll()
//
//                        // Public POST endpoints (auth)
//                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
//
//                        // Admin-only paths — enforced at URL level AND @PreAuthorize at method level
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/analytics/admin").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.POST,   "/platforms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT,    "/platforms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/platforms/**").hasRole("ADMIN")
//                        .requestMatchers("/settings/admin/**").hasRole("ADMIN")
//
//                        // All other requests require authentication
//                        .anyRequest().authenticated()
//                )
//
//                // ── Authentication provider ───────────────────────
//                .authenticationProvider(authenticationProvider())
//
//                // ── JWT filter before username/password filter ────
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // ── Beans ─────────────────────────────────────────────────
//
//    /**
//     * BCrypt with cost factor 12 — strong enough for production,
//     * benchmark ~250ms on modern hardware per hash.
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
//
//    /**
//     * DaoAuthenticationProvider — wires UserDetailsService + PasswordEncoder.
//     * Used by AuthenticationManager during login.
//     */
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    /**
//     * AuthenticationManager — exposed as a bean so AuthController can inject it.
//     * Spring Boot 3 requires explicit extraction from AuthenticationConfiguration.
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}


//
//package com.dsanext.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfigurationSource;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(prePostEnabled = true)
//public class SecurityConfig {
//
//    private final UserDetailsService userDetailsService;
//    private final JwtAuthFilter jwtAuthFilter;
//    private final JwtAuthEntryPoint jwtAuthEntryPoint;
//    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
//    private final CorsConfigurationSource corsConfigurationSource;
//
//    public SecurityConfig(
//            @Lazy UserDetailsService userDetailsService,
//            JwtAuthFilter jwtAuthFilter,
//            JwtAuthEntryPoint jwtAuthEntryPoint,
//            JwtAccessDeniedHandler jwtAccessDeniedHandler,
//            CorsConfigurationSource corsConfigurationSource) {
//        this.userDetailsService = userDetailsService;
//        this.jwtAuthFilter = jwtAuthFilter;
//        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
//        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
//        this.corsConfigurationSource = corsConfigurationSource;
//    }
//
//    private static final String[] PUBLIC_GET_ENDPOINTS = {
//            "/auth/me",
//            "/problems",
//            "/problems/**",
//            "/platforms",
//            "/platforms/**",
//            "/settings/public",
//            "/uploads/**",
//            "/actuator/health",
//            "/actuator/info",
//    };
//
//    private static final String[] PUBLIC_POST_ENDPOINTS = {
//            "/auth/register",
//            "/auth/login",
//    };
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource))
//                .sessionManagement(session ->
//                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint(jwtAuthEntryPoint)
//                        .accessDeniedHandler(jwtAccessDeniedHandler))
//                .authorizeHttpRequests(auth -> auth
//
//                        // PUBLIC
//                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
//                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
//
//                        // ✅ FIXED ADMIN PATH (IMPORTANT CHANGE)
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/analytics/admin/**").hasRole("ADMIN")
//
//                        // PLATFORM ADMIN
//                        .requestMatchers(HttpMethod.POST, "/api/platforms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.PUT, "/api/platforms/**").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/platforms/**").hasRole("ADMIN")
//
//                        // SETTINGS ADMIN
//                        .requestMatchers("/api/settings/admin/**").hasRole("ADMIN")
//
//                        // DEFAULT
//                        .anyRequest().authenticated()
//                )
//                .authenticationProvider(authenticationProvider())
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}

package com.dsanext.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * DSANext Spring Security Configuration.
 *
 * Strategy:
 *  - Stateless JWT (no sessions, no cookies)
 *  - BCrypt password encoding (cost factor 12)
 *  - Method-level security enabled (@PreAuthorize)
 *  - Custom 401/403 JSON handlers (no HTML redirects)
 *  - CORS configured from CorsConfig bean
 *
 * Route access rules (least-privilege, explicit allow-list):
 *  PUBLIC      — auth endpoints, public problem listing, platform listing, public settings
 *  JWT         — all user-specific endpoints (progress, notes, bookmarks, notifications)
 *  ADMIN       — /admin/**, /analytics/admin, and admin sub-paths on platform/settings
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService      userDetailsService;
    private final JwtAuthFilter           jwtAuthFilter;
    private final JwtAuthEntryPoint       jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler  jwtAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            @Lazy UserDetailsService userDetailsService,
            JwtAuthFilter jwtAuthFilter,
            JwtAuthEntryPoint jwtAuthEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            CorsConfigurationSource corsConfigurationSource) {
        this.userDetailsService     = userDetailsService;
        this.jwtAuthFilter          = jwtAuthFilter;
        this.jwtAuthEntryPoint      = jwtAuthEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.corsConfigurationSource= corsConfigurationSource;
    }

    // ── Public endpoints (no JWT required) ──────────────────

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/auth/me",              // Returns null user gracefully
            "/problems",             // Public problem listing
            "/problems/**",          // Problem detail
            "/platforms",            // Platform listing for filter dropdowns
            "/platforms/**",         // Individual platform detail
            "/settings/public",      // Public app config for frontend bootstrap
            "/uploads/**",           // Served profile images (static files)
            "/actuator/health",      // Health check
            "/actuator/info",        // App info
    };

    private static final String[] PUBLIC_POST_ENDPOINTS = {
            "/auth/register",
            "/auth/login",
    };

    // ── Security filter chain ────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── CSRF — disabled (stateless JWT API) ──────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── CORS — use our CorsConfig bean ───────────────
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // ── Session — stateless, never create HTTP sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Exception handling ────────────────────────────
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // ── Authorization rules ───────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET,  PUBLIC_GET_ENDPOINTS).permitAll()

                        // Public POST endpoints (auth)
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()

                        // Admin-only paths — enforced at URL level AND @PreAuthorize at method level
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/analytics/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/platforms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/platforms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/platforms/**").hasRole("ADMIN")
                        .requestMatchers("/settings/admin/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // ── Authentication provider ───────────────────────
                .authenticationProvider(authenticationProvider())

                // ── JWT filter before username/password filter ────
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Beans ─────────────────────────────────────────────────

    /**
     * BCrypt with cost factor 12 — strong enough for production,
     * benchmark ~250ms on modern hardware per hash.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider — wires UserDetailsService + PasswordEncoder.
     * Used by AuthenticationManager during login.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — exposed as a bean so AuthController can inject it.
     * Spring Boot 3 requires explicit extraction from AuthenticationConfiguration.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Prevents Spring Boot from auto-registering JwtAuthFilter as a servlet filter
     * in addition to the SecurityFilterChain registration — which would cause it
     * to run twice per request (double DB lookup, double JWT validation).
     */
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthFilter>
    jwtFilterRegistration(JwtAuthFilter filter) {
        var reg = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        reg.setEnabled(false);  // Managed by Spring Security only
        return reg;
    }
}
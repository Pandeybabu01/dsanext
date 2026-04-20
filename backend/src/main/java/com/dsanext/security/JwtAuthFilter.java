//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///**
// * JWT authentication filter — runs once per request.
// *
// * Flow:
// *  1. Extract Bearer token from Authorization header
// *  2. Validate token signature and expiry via AuthService
// *  3. Load UserDetails from DB
// *  4. Set authentication in SecurityContext
// *
// * If any step fails, the filter simply clears context and continues —
// * the downstream security chain handles 401/403 for protected endpoints.
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final AuthService authService;
//
//    private static final String AUTHORIZATION_HEADER = "Authorization";
//    private static final String BEARER_PREFIX        = "Bearer ";
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest  request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain         filterChain) throws ServletException, IOException {
//
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (StringUtils.hasText(token) && authService.validateToken(token)) {
//                String email = authService.extractEmail(token);
//
//                // Only load user if not already authenticated in this request
//                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                    UserDetails userDetails = authService.loadUserByUsername(email);
//
//                    if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
//                        UsernamePasswordAuthenticationToken authentication =
//                                new UsernamePasswordAuthenticationToken(
//                                        userDetails,
//                                        null,
//                                        userDetails.getAuthorities()
//                                );
//
//                        authentication.setDetails(
//                                new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authentication);
//                        log.debug("Authenticated user '{}' for request [{}]",
//                                email, request.getRequestURI());
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            // Never let filter exceptions bubble up — log and continue
//            log.error("JWT filter error for request [{}]: {}",
//                    request.getRequestURI(), ex.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    /**
//     * Extracts the JWT token from the Authorization header.
//     * Returns null if the header is missing or not a Bearer token.
//     */
//    private String extractTokenFromRequest(HttpServletRequest request) {
//        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
//            return authHeader.substring(BEARER_PREFIX.length());
//        }
//        return null;
//    }
//
//    /**
//     * Skip JWT processing for the authentication endpoints themselves.
//     */
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
//    }
//}



//
//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///**
// * JWT authentication filter — runs once per request.
// *
// * Flow:
// *  1. Extract Bearer token from Authorization header
// *  2. Validate token signature and expiry via AuthService
// *  3. Load UserDetails from DB
// *  4. Set authentication in SecurityContext
// *
// * If any step fails, the filter simply clears context and continues —
// * the downstream security chain handles 401/403 for protected endpoints.
// */
//@Slf4j
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private AuthService authService;
//
//    @Autowired
//    public void setAuthService(@Lazy AuthService authService) {
//        this.authService = authService;
//    }
//
//    private static final String AUTHORIZATION_HEADER = "Authorization";
//    private static final String BEARER_PREFIX        = "Bearer ";
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest  request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain         filterChain) throws ServletException, IOException {
//
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (StringUtils.hasText(token) && authService.validateToken(token)) {
//                String email = authService.extractEmail(token);
//
//                // Only load user if not already authenticated in this request
//                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                    UserDetails userDetails = authService.loadUserByUsername(email);
//
//                    if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
//                        UsernamePasswordAuthenticationToken authentication =
//                                new UsernamePasswordAuthenticationToken(
//                                        userDetails,
//                                        null,
//                                        userDetails.getAuthorities()
//                                );
//
//                        authentication.setDetails(
//                                new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authentication);
//                        log.debug("Authenticated user '{}' for request [{}]",
//                                email, request.getRequestURI());
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            // Never let filter exceptions bubble up — log and continue
//            log.error("JWT filter error for request [{}]: {}",
//                    request.getRequestURI(), ex.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    /**
//     * Extracts the JWT token from the Authorization header.
//     * Returns null if the header is missing or not a Bearer token.
//     */
//    private String extractTokenFromRequest(HttpServletRequest request) {
//        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
//            return authHeader.substring(BEARER_PREFIX.length());
//        }
//        return null;
//    }
//
//    /**
//     * Skip JWT processing for the authentication endpoints themselves.
//     */
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
//    }
//}


//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///**
// * JWT authentication filter — runs once per request.
// *
// * Flow:
// *  1. Extract Bearer token from Authorization header
// *  2. Validate token signature and expiry via AuthService
// *  3. Load UserDetails from DB
// *  4. Set authentication in SecurityContext
// *
// * If any step fails, the filter simply clears context and continues —
// * the downstream security chain handles 401/403 for protected endpoints.
// */
//@Slf4j
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private AuthService authService;
//
//    @Autowired
//    public void setAuthService(@Lazy AuthService authService) {
//        this.authService = authService;
//    }
//
//    private static final String AUTHORIZATION_HEADER = "Authorization";
//    private static final String BEARER_PREFIX        = "Bearer ";
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest  request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain         filterChain) throws ServletException, IOException {
//
//        try {
//            String token = extractTokenFromRequest(request);
//
//            if (StringUtils.hasText(token) && authService.validateToken(token)) {
//                String email = authService.extractEmail(token);
//
//                // Only load user if not already authenticated in this request
//                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                    UserDetails userDetails = authService.loadUserByUsername(email);
//
//                    if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
//                        UsernamePasswordAuthenticationToken authentication =
//                                new UsernamePasswordAuthenticationToken(
//                                        userDetails,
//                                        null,
//                                        userDetails.getAuthorities()
//                                );
//
//                        authentication.setDetails(
//                                new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authentication);
//                        log.debug("Authenticated user '{}' for request [{}]",
//                                email, request.getRequestURI());
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            // Never let filter exceptions bubble up — log and continue
//            log.error("JWT filter error for request [{}]: {}",
//                    request.getRequestURI(), ex.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    /**
//     * Extracts the JWT token from the Authorization header.
//     * Returns null if the header is missing or not a Bearer token.
//     */
//    private String extractTokenFromRequest(HttpServletRequest request) {
//        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
//            return authHeader.substring(BEARER_PREFIX.length());
//        }
//        return null;
//    }
//
//    /**
//     * Skip JWT processing for the authentication endpoints themselves.
//     */
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
//    }
//}


//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private AuthService authService;
//
//    @Autowired
//    public void setAuthService(@Lazy AuthService authService) {
//        this.authService = authService;
//    }
//
//    private static final String AUTH_HEADER = "Authorization";
//    private static final String BEARER = "Bearer ";
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        try {
//            String header = request.getHeader(AUTH_HEADER);
//
//            log.debug("Auth Header: {}", header);
//
//            String token = extractToken(header);
//
//            if (token != null && authService.validateToken(token)) {
//
//                String email = authService.extractEmail(token);
//                log.debug("Extracted email from JWT: {}", email);
//
//                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                    UserDetails userDetails = authService.loadUserByUsername(email);
//
//                    UsernamePasswordAuthenticationToken auth =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetails,
//                                    null,
//                                    userDetails.getAuthorities()
//                            );
//
//                    auth.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request)
//                    );
//
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//
//                    log.debug("User authenticated: {}", email);
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("JWT Filter error: {}", ex.getMessage(), ex);
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(String header) {
//        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
//            return header.substring(7);
//        }
//        return null;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//
//        // FIXED: proper API base path
//        return path.startsWith("/api/auth/login")
//                || path.startsWith("/api/auth/register");
//    }
//}

//
//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.lang.NonNull;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private AuthService authService;
//
//    @Autowired
//    public void setAuthService(@Lazy AuthService authService) {
//        this.authService = authService;
//    }
//
//    private static final String AUTH_HEADER = "Authorization";
//    private static final String BEARER = "Bearer ";
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        String path = request.getRequestURI();
//
//        // ✅ FIX 1: ACTUATOR BYPASS (CRITICAL)
//        if (path.startsWith("/api/actuator")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            String header = request.getHeader(AUTH_HEADER);
//
//            log.debug("Auth Header: {}", header);
//
//            String token = extractToken(header);
//
//            if (token != null && authService.validateToken(token)) {
//
//                String email = authService.extractEmail(token);
//                log.debug("Extracted email from JWT: {}", email);
//
//                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                    UserDetails userDetails = authService.loadUserByUsername(email);
//
//                    UsernamePasswordAuthenticationToken auth =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetails,
//                                    null,
//                                    userDetails.getAuthorities()
//                            );
//
//                    auth.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request)
//                    );
//
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//
//                    log.debug("User authenticated: {}", email);
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("JWT Filter error: {}", ex.getMessage(), ex);
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(String header) {
//        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
//            return header.substring(7);
//        }
//        return null;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//
//        // ✅ FIX 2: ACTUATOR EXCLUDED HERE TOO (SAFE)
//        return path.startsWith("/api/auth/login")
//                || path.startsWith("/api/auth/register")
//                || path.startsWith("/api/actuator");
//    }
//}

//package com.dsanext.security;
//
//import com.dsanext.service.AuthService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private AuthService authService;
//
//    @Autowired
//    public void setAuthService(@Lazy AuthService authService) {
//        this.authService = authService;
//    }
//
//    private static final String AUTH_HEADER = "Authorization";
//    private static final String BEARER = "Bearer ";
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        String path = request.getServletPath();
//
//        // ✅ ONLY SYSTEM LEVEL BYPASS (NOT BUSINESS LOGIC)
//        if (path.startsWith("/api/auth")
//                || path.startsWith("/api/actuator")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            String header = request.getHeader(AUTH_HEADER);
//            String token = extractToken(header);
//
//            if (token != null && authService.validateToken(token)) {
//
//                String email = authService.extractEmail(token);
//
//                if (email != null &&
//                        SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                    UserDetails userDetails = authService.loadUserByUsername(email);
//
//                    UsernamePasswordAuthenticationToken auth =
//                            new UsernamePasswordAuthenticationToken(
//                                    userDetails,
//                                    null,
//                                    userDetails.getAuthorities()
//                            );
//
//                    auth.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request)
//                    );
//
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//                }
//            }
//
//        } catch (Exception ex) {
//            log.error("JWT error: {}", ex.getMessage(), ex);
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(String header) {
//        if (StringUtils.hasText(header) && header.startsWith(BEARER)) {
//            return header.substring(7);
//        }
//        return null;
//    }
//}

package com.dsanext.security;

import com.dsanext.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter — runs once per request.
 *
 * Flow:
 *  1. Extract Bearer token from Authorization header
 *  2. Validate token signature and expiry via AuthService
 *  3. Load UserDetails from DB
 *  4. Set authentication in SecurityContext
 *
 * If any step fails, the filter simply clears context and continues —
 * the downstream security chain handles 401/403 for protected endpoints.
 */
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private AuthService authService;

    @Autowired
    public void setAuthService(@Lazy AuthService authService) {
        this.authService = authService;
    }

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && authService.validateToken(token)) {
                String email = authService.extractEmail(token);

                // Only load user if not already authenticated in this request
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load the actual User entity so @AuthenticationPrincipal resolves
                    // to com.dsanext.domain.entity.User in controllers
                    com.dsanext.domain.entity.User user = authService.loadUser(email);

                    if (user != null && user.isActive()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,           // principal = our User entity
                                        null,
                                        user.getAuthorities()
                                );

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("✅ Authenticated user: {} | Roles: {}",
                                email, user.getAuthorities());
                    }
                }
            }
        } catch (Exception ex) {
            // Never let filter exceptions bubble up — log and continue
            log.error("JWT filter error for request [{}]: {}",
                    request.getRequestURI(), ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * Returns null if the header is missing or not a Bearer token.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Skip JWT processing for the authentication endpoints themselves.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
    }
}
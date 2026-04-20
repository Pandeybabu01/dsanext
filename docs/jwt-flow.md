# DSANext — JWT Authentication Flow

## Token Structure

DSANext uses HMAC-SHA256 signed JWTs. The payload contains:

```json
{
  "sub":      "john@example.com",
  "userId":   "550e8400-e29b-41d4-a716-446655440000",
  "role":     "USER",
  "username": "johndoe",
  "iat":      1705312200,
  "exp":      1705398600
}
```

**Default expiry:** 24 hours (86,400,000 ms) — configurable via `security.jwt_expiry_minutes` app setting.

---

## Complete Authentication Flow

### Step 1: Register or Login

```
POST /api/auth/register
POST /api/auth/login

Request:
{
  "email":    "john@example.com",
  "password": "Secure@123"
}

Response:
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwidXNlcklkIjoiNTUwZTg0MDAiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTcwNTMxMjIwMCwiZXhwIjoxNzA1Mzk4NjAwfQ.signature",
    "tokenType":   "Bearer",
    "expiresIn":   86400000,
    "user":        { "id": "...", "email": "...", "role": "USER" }
  }
}
```

**Frontend action:** Store `accessToken` in `localStorage` under key `dsanext_token`. Store `user` under `dsanext_user`.

---

### Step 2: Authenticated Requests

```
GET /api/progress
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Server flow:
1. JwtAuthFilter.doFilterInternal()
2. Extract "Bearer " prefix → raw token
3. authService.validateToken(token)
   → Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)
   → Check signature ✓, check exp ✓
4. authService.extractEmail(token) → "john@example.com"
5. authService.loadUserByUsername("john@example.com")
   → userRepository.findByEmail("john@example.com")
6. new UsernamePasswordAuthenticationToken(userDetails, null, authorities)
7. SecurityContextHolder.getContext().setAuthentication(auth)
8. Controller: @AuthenticationPrincipal User user → user entity injected
```

---

### Step 3: Token Expiry

When the token expires, the server returns:

```
HTTP 401
{
  "success": false,
  "message": "Authentication required. Please login to access this resource.",
  "error":   "Unauthorized"
}
```

**Frontend action:** Axios interceptor catches 401 → clears `localStorage` → redirects to `/login`.

```javascript
// src/lib/axios.js
api.interceptors.response.use(null, (error) => {
  if (error.response?.status === 401) {
    localStorage.removeItem('dsanext_token')
    localStorage.removeItem('dsanext_user')
    window.location.href = '/login'
  }
})
```

---

## Role-Based Access Control

Spring Security maps `Role.ADMIN` → `ROLE_ADMIN` authority (Spring adds the `ROLE_` prefix).

### URL-level enforcement (SecurityConfig)

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
.requestMatchers("/analytics/admin").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/platforms/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

### Method-level enforcement (@PreAuthorize)

```java
@GetMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<AnalyticsResponse>> getAdminAnalytics() { ... }
```

Both layers enforce the same rule — defense in depth.

---

## Security Configuration Reference

| Setting | Value | Notes |
|---------|-------|-------|
| Algorithm | HMAC-SHA256 (HS256) | Symmetric — secret must stay server-side |
| Key size | 256+ bit | Enforced by JJWT at startup |
| BCrypt cost | 12 | ~250ms per hash — prevents brute force |
| Session policy | `STATELESS` | No server-side sessions, no cookies |
| CSRF | Disabled | Not needed for stateless JWT APIs |
| CORS | Configurable | Via `dsanext.cors.allowed-origins` |

---

## Environment Variable: JWT_SECRET

In production, set `JWT_SECRET` to a cryptographically random 256-bit (32+ byte) string:

```bash
# Generate secure secret
openssl rand -base64 32

# Set in production environment
export JWT_SECRET="your-generated-secret-here"
```

The default dev secret is intentionally weak — it must be changed before production deployment.

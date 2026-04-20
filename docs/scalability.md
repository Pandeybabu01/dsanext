# DSANext — Scalability & Performance Guide

## Current Architecture Capacity

| Metric | Single Instance | Notes |
|--------|----------------|-------|
| Concurrent users | ~500 | With 30 HikariCP connections |
| Requests/second | ~200–400 | Spring Boot on 4-core server |
| Database connections | 30 (prod) | HikariCP max-pool-size |
| JWT validation | ~1ms | In-memory, no DB lookup |
| BCrypt hashing | ~250ms | Cost factor 12, login only |
| Profile image size | 5MB max | Stored on local filesystem |
| Log retention | 90 days | Configurable via app settings |

---

## Phase 1 — Immediate Optimizations (No Infrastructure Change)

### 1.1 Database Query Optimization

All high-traffic queries are already indexed. Additional tuning:

```sql
-- Composite index for progress dashboard query
CREATE INDEX CONCURRENTLY idx_progress_user_status
    ON progress(user_id, status)
    INCLUDE (problem_id, solved_at, updated_at);

-- Partial index for active problems only (most common query)
CREATE INDEX CONCURRENTLY idx_problems_active_difficulty
    ON problems(difficulty, topic)
    WHERE is_active = true;

-- Partial index for unread notifications (hot path)
CREATE INDEX CONCURRENTLY idx_notifications_unread
    ON notifications(user_id, created_at DESC)
    WHERE is_read = false;

-- Covering index for bookmark existence check (very frequent)
CREATE INDEX CONCURRENTLY idx_bookmarks_user_problem_covering
    ON bookmarks(user_id, problem_id);

-- Analyze after index creation
ANALYZE problems, progress, notifications, bookmarks;
```

### 1.2 N+1 Query Prevention

All list endpoints already use `JOIN FETCH` in JPQL. Verify with query logging:

```yaml
# application-dev.yml — enable for profiling
spring.jpa.properties.hibernate:
  generate_statistics: true
  format_sql: true
logging.level.org.hibernate.stat: DEBUG
```

Run the app, execute a list request, check logs for statement count. Any endpoint showing N+1 (one query per row) needs a fetch join.

### 1.3 Connection Pool Tuning

For 4-vCPU production server:

```yaml
# application-prod.yml
spring.datasource.hikari:
  maximum-pool-size:    25     # (num_cores * 2) + effective_spindle_count
  minimum-idle:         5
  idle-timeout:         600000  # 10 min
  connection-timeout:   3000    # 3s — fail fast
  max-lifetime:        1800000  # 30 min
  leak-detection-threshold: 60000
  pool-name: DSANextProd
  # Health check query
  connection-test-query: SELECT 1
```

### 1.4 JVM Tuning

```bash
# /etc/systemd/system/dsanext-backend.service ExecStart flags
java \
  -jar dsanext-backend-1.0.0.jar \
  -XX:+UseG1GC \
  -XX:G1HeapRegionSize=16m \
  -XX:MaxGCPauseMillis=200 \
  -XX:+ParallelRefProcEnabled \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/dsanext/logs/heapdump.hprof \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod
```

### 1.5 Spring Boot Response Compression

Already configured in `application.yml`. Verify Nginx also gzips:

```nginx
gzip on;
gzip_min_length  1000;
gzip_comp_level  6;
gzip_vary        on;
gzip_types
  application/json
  application/javascript
  text/css
  text/plain
  image/svg+xml;
```

---

## Phase 2 — Caching Layer (Redis)

When single-instance response times exceed 100ms for frequent read endpoints, add Redis.

### 2.1 Install Redis

```bash
sudo apt install -y redis-server
sudo systemctl enable redis-server
sudo systemctl start redis-server

# Secure Redis
sudo nano /etc/redis/redis.conf
# Set: bind 127.0.0.1
# Set: requirepass your-redis-password
# Set: maxmemory 512mb
# Set: maxmemory-policy allkeys-lru
```

### 2.2 Add Spring Cache to Backend

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

```yaml
# application-prod.yml
spring:
  data:
    redis:
      host:     localhost
      port:     6379
      password: your-redis-password
      timeout:  2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle:   4
          min-idle:   1

  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutes default TTL
      cache-null-values: false
```

```java
// Add to DSANextApplication.java
@EnableCaching

// CacheConfig.java — cache configuration bean
@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheManagerCustomizer() {
        return builder -> builder
            // Public settings: rarely change, 1 hour TTL
            .withCacheConfiguration("publicSettings",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(1)))
            // Platform list: changes infrequently, 30 min TTL
            .withCacheConfiguration("platforms",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30)))
            // Problem topics: changes on new problems, 15 min TTL
            .withCacheConfiguration("problemTopics",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(15)))
            // User analytics: compute-heavy, 5 min TTL
            .withCacheConfiguration("userAnalytics",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5)));
    }
}
```

### 2.3 Annotate Service Methods

```java
// SettingService.java
@Cacheable(value = "publicSettings", key = "'all'")
public Map<String, String> getPublicSettingsMap() { ... }

@CacheEvict(value = "publicSettings", allEntries = true)
public AppSettingResponse updateAppSetting(String key, ...) { ... }

// PlatformService.java
@Cacheable(value = "platforms", key = "'active'")
public List<PlatformResponse> getAllActivePlatforms() { ... }

@CacheEvict(value = "platforms", allEntries = true)
public PlatformResponse createPlatform(...) { ... }

// ProblemService.java
@Cacheable(value = "problemTopics", key = "'topics'")
public List<String> getDistinctTopics() { ... }

@CacheEvict(value = "problemTopics", allEntries = true)
public ProblemResponse createProblem(...) { ... }

// AnalyticsService.java
@Cacheable(value = "userAnalytics", key = "#userId")
public AnalyticsResponse getUserAnalytics(UUID userId) { ... }

@CacheEvict(value = "userAnalytics", key = "#userId")
public void evictUserAnalytics(UUID userId) { ... }
```

### 2.4 Cache Invalidation Strategy

| Cache | TTL | Invalidated By |
|-------|-----|----------------|
| `publicSettings` | 1 hour | Any `updateAppSetting()` call |
| `platforms` | 30 min | Platform create/update/delete |
| `problemTopics` | 15 min | Problem create/update/delete |
| `userAnalytics` | 5 min | Progress update, note save, bookmark toggle |

---

## Phase 3 — Horizontal Scaling

### 3.1 Load Balancer Setup (Nginx)

JWT is stateless — no session affinity needed. Any backend instance can handle any request.

```nginx
# /etc/nginx/sites-available/dsanext
upstream dsanext_backend {
    least_conn;                          # Route to instance with fewest connections
    server 127.0.0.1:8080 weight=1;     # Instance 1
    server 127.0.0.1:8081 weight=1;     # Instance 2
    server 127.0.0.1:8082 weight=1;     # Instance 3

    keepalive 32;                        # Keep connections warm
}

server {
    location /api/ {
        proxy_pass http://dsanext_backend/api/;
        proxy_http_version 1.1;
        proxy_set_header Connection "";   # Enable keepalive
    }
}
```

### 3.2 Multiple Backend Instances

```bash
# Run three instances on different ports
java -jar dsanext-backend.jar --server.port=8080 &
java -jar dsanext-backend.jar --server.port=8081 &
java -jar dsanext-backend.jar --server.port=8082 &
```

Since JWT validation is in-memory (same secret on all instances), no shared session store is needed.

### 3.3 File Upload — Shared Storage

Local filesystem profile images break with multiple instances (each instance has its own disk). Migrate to object storage:

```java
// StorageService.java — S3-compatible interface
public interface StorageService {
    String upload(MultipartFile file, String key) throws IOException;
    void delete(String key) throws IOException;
    String getPublicUrl(String key);
}

// S3StorageService.java — AWS S3 / DigitalOcean Spaces / MinIO
@Service
@ConditionalOnProperty(name = "dsanext.storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Client s3Client;

    @Value("${dsanext.storage.bucket}")
    private String bucket;

    @Value("${dsanext.storage.base-url}")
    private String baseUrl;

    @Override
    public String upload(MultipartFile file, String key) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket).key(key)
                .contentType(file.getContentType())
                .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return getPublicUrl(key);
    }

    @Override
    public String getPublicUrl(String key) {
        return baseUrl + "/" + key;
    }
}
```

Add to `application-prod.yml`:
```yaml
dsanext.storage:
  type:     s3
  bucket:   dsanext-uploads
  base-url: https://dsanext-uploads.nyc3.digitaloceanspaces.com
  region:   nyc3

cloud.aws.credentials:
  access-key: ${AWS_ACCESS_KEY}
  secret-key:  ${AWS_SECRET_KEY}
```

### 3.4 Async Queue (When Volume Requires)

Replace `@Async` notification sends with a proper queue:

```
Current: Service → @Async → Thread Pool → DB insert (notifications)

Future:  Service → RabbitMQ → Consumer Worker → DB insert + Email send
```

```java
// NotificationProducer.java
@Service
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(NotificationEvent event) {
        rabbitTemplate.convertAndSend("dsanext.notifications", event);
    }
}

// NotificationConsumer.java
@Component
public class NotificationConsumer {

    @RabbitListener(queues = "dsanext.notifications")
    public void handleNotification(NotificationEvent event) {
        // Save to DB
        // Send email if enabled
        // Push to WebSocket if connected
    }
}
```

---

## Phase 4 — Database Scaling

### 4.1 Read Replicas

Route read-heavy queries to PostgreSQL read replicas using Spring's `AbstractRoutingDataSource`:

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("primaryDS") DataSource primary,
            @Qualifier("replicaDS") DataSource replica) {

        ReplicationRoutingDataSource routing = new ReplicationRoutingDataSource();
        routing.setPrimaryDataSource(primary);
        routing.addReplicaDataSource(replica);
        return routing;
    }
}

// Routes @Transactional(readOnly=true) to replica automatically
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                ? "replica" : "primary";
    }
}
```

All read-only service methods already annotated with `@Transactional(readOnly=true)` — they will automatically route to the replica.

### 4.2 Connection Pooling with PgBouncer

At very high concurrency, PostgreSQL's max_connections becomes a bottleneck. PgBouncer pools connections efficiently:

```bash
sudo apt install -y pgbouncer

# /etc/pgbouncer/pgbouncer.ini
[databases]
dsanext_prod_db = host=127.0.0.1 port=5432 dbname=dsanext_prod_db

[pgbouncer]
listen_port     = 6432
listen_addr     = 127.0.0.1
auth_type       = md5
pool_mode       = transaction    # Best for Spring Boot
max_client_conn = 1000
default_pool_size = 25
```

```yaml
# application-prod.yml — point to PgBouncer
spring.datasource.url: jdbc:postgresql://localhost:6432/dsanext_prod_db
```

### 4.3 Full-Text Search with PostgreSQL

Before adding Elasticsearch, try PostgreSQL's built-in full-text search:

```sql
-- Add tsvector column for problem full-text search
ALTER TABLE problems
    ADD COLUMN search_vector tsvector
        GENERATED ALWAYS AS (
            to_tsvector('english',
                coalesce(title, '') || ' ' ||
                coalesce(topic, '') || ' ' ||
                coalesce(description, ''))
        ) STORED;

CREATE INDEX idx_problems_search_vector
    ON problems USING GIN(search_vector);
```

```java
// ProblemRepository.java — native query using tsquery
@Query(value = """
    SELECT * FROM problems
    WHERE search_vector @@ plainto_tsquery('english', :query)
      AND is_active = true
    ORDER BY ts_rank(search_vector, plainto_tsquery('english', :query)) DESC
    LIMIT :limit
    """, nativeQuery = true)
List<Problem> fullTextSearch(@Param("query") String query, @Param("limit") int limit);
```

---

## Monitoring & Observability

### Actuator Endpoints

Already configured. Add Prometheus metrics for time-series monitoring:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
# application-prod.yml
management.endpoints.web.exposure.include: health,info,metrics,prometheus
management.metrics.tags.application: dsanext
```

```bash
# Scrape metrics
curl http://localhost:8080/api/actuator/prometheus
```

### Prometheus + Grafana Setup

```bash
# Install Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.51.0/prometheus-2.51.0.linux-amd64.tar.gz
tar xvf prometheus-*.tar.gz

# prometheus.yml
scrape_configs:
  - job_name: 'dsanext'
    metrics_path: '/api/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    scrape_interval: 15s

# Install Grafana
sudo apt-get install -y adduser libfontconfig1 musl
wget https://dl.grafana.com/oss/release/grafana_10.3.3_amd64.deb
sudo dpkg -i grafana_10.3.3_amd64.deb
sudo systemctl enable grafana-server
sudo systemctl start grafana-server
# Access: http://server-ip:3000 (admin/admin)
```

### Key Metrics to Monitor

| Metric | Alert Threshold | Dashboard Panel |
|--------|----------------|-----------------|
| `http_server_requests_seconds_max` | > 2s | Request latency P99 |
| `hikaricp_connections_active` | > 25 (of 30) | DB connection pressure |
| `jvm_memory_used_bytes` | > 85% heap | Memory utilization |
| `jvm_gc_pause_seconds_sum` | > 500ms/min | GC pressure |
| `process_cpu_usage` | > 80% | CPU saturation |
| `dsanext_users_registered_total` | — | Growth tracking |
| `dsanext_problems_solved_total` | — | Engagement tracking |

### Custom Business Metrics

```java
// MetricsService.java
@Component
public class MetricsService {

    private final Counter registrations;
    private final Counter problemsSolved;
    private final Timer   loginTimer;

    public MetricsService(MeterRegistry registry) {
        registrations  = registry.counter("dsanext.users.registered");
        problemsSolved = registry.counter("dsanext.problems.solved");
        loginTimer     = registry.timer("dsanext.auth.login.duration");
    }

    public void recordRegistration()   { registrations.increment(); }
    public void recordProblemSolved()  { problemsSolved.increment(); }
    public <T> T timeLogin(Supplier<T> fn) {
        return loginTimer.record(fn);
    }
}
```

### Structured Logging

Add JSON logging for log aggregation tools (Loki, Datadog, CloudWatch):

```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- logback-spring.xml (prod profile) -->
<springProfile name="prod">
  <appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>/opt/dsanext/logs/application.json</file>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <customFields>{"app":"dsanext","env":"prod"}</customFields>
    </encoder>
  </appender>
</springProfile>
```

---

## Security Hardening for Production

### Rate Limiting

Add API rate limiting using Bucket4j:

```xml
<dependency>
    <groupId>com.giffing.bucket4j.spring.boot.starter</groupId>
    <artifactId>bucket4j-spring-boot-starter</artifactId>
    <version>0.10.1</version>
</dependency>
```

```yaml
bucket4j:
  enabled: true
  filters:
    - cache-name: rate-limit-cache
      url: /auth/login
      rate-limits:
        - bandwidths:
            - capacity: 10
              time: 1
              unit: minutes
              refill-speed: interval
```

### HTTP Security Headers

Already configured in Nginx. Add to Spring Boot as well:

```java
// SecurityConfig.java — add to filterChain
.headers(headers -> headers
    .frameOptions(frame -> frame.sameOrigin())
    .contentTypeOptions(Customizer.withDefaults())
    .httpStrictTransportSecurity(hsts -> hsts
        .maxAgeInSeconds(31536000)
        .includeSubDomains(true))
)
```

### Secrets Management

Never store secrets in code or config files. Use environment variables or a secrets manager:

```bash
# For small deployments — environment variables in systemd
# /etc/systemd/system/dsanext-backend.service
[Service]
EnvironmentFile=/opt/dsanext/backend/.env  # chmod 600

# For larger deployments — HashiCorp Vault or AWS Secrets Manager
vault kv put secret/dsanext \
  db_password="..." \
  jwt_secret="..."
```

---

## Frontend Performance

### Vite Build Optimization

Already configured with manual chunks. Additional optimizations:

```javascript
// vite.config.js additions
build: {
  rollupOptions: {
    output: {
      manualChunks: {
        vendor:   ['react', 'react-dom', 'react-router-dom'],
        redux:    ['@reduxjs/toolkit', 'react-redux'],
        charts:   ['recharts'],
        clsx:     ['clsx'],
      },
    },
  },
  minify:    'terser',
  terserOptions: { compress: { drop_console: true } },
  reportCompressedSize: true,
},
```

### Nginx Static Asset Caching

Already configured in Nginx with `expires 1y` for hashed assets. Verify with:

```bash
curl -I https://dsanext.com/assets/index-DiwrgTda.js | grep -i cache
# cache-control: public, max-age=31536000, immutable
```

### Lazy Loading

Already implemented with `React.lazy()` and `<Suspense>` in `AppRouter.jsx`. Every page is code-split — users only download what they navigate to.

### Image Optimization

```bash
# Compress uploaded profile images before storage
# Add to FileUploadController.java using TwelveMonkeys or Thumbnailator
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.20</version>
</dependency>
```

```java
// Resize and compress before saving
Thumbnails.of(file.getInputStream())
    .size(256, 256)
    .outputFormat("jpg")
    .outputQuality(0.85)
    .toOutputStream(outputStream);
```

---

## Future Improvements Roadmap

### Short-Term (1–3 months)

| Feature | Description | Effort |
|---------|-------------|--------|
| Email verification | Verify email on registration via token link | Medium |
| OAuth2 login | Google / GitHub social login | Medium |
| WebSocket notifications | Real-time bell count without polling | Medium |
| Problem tags | Multiple tags per problem (e.g., "classic", "interview", "contest") | Low |
| Code snippet in notes | Syntax-highlighted code blocks in note editor | Low |
| Keyboard shortcuts | `j/k` to navigate problems, `b` to bookmark | Low |
| Problem difficulty filter on dashboard | Filter recently solved by difficulty | Low |

### Medium-Term (3–6 months)

| Feature | Description | Effort |
|---------|-------------|--------|
| Leaderboard | Weekly/monthly ranking by problems solved | Medium |
| Study plans | Curated 30/60/90-day DSA programs | High |
| Contest mode | Timed problem sets with scoring | High |
| Problem import | Bulk import from CSV or LeetCode API | Medium |
| Discussion threads | Per-problem comments and hints | High |
| Streak tracking | Daily solving streaks with calendar heatmap | Medium |
| Mobile app | React Native app reusing API | High |

### Long-Term (6–12 months)

| Feature | Description | Effort |
|---------|-------------|--------|
| AI hints | GPT-powered hints for stuck users | High |
| Code execution | Run and test code in-browser (via Judge0) | Very High |
| Company tag filtering | Filter by FAANG / startup / etc. | Medium |
| Interview simulator | Timed mock interview sessions | Very High |
| Team workspaces | Groups for bootcamps / teams | High |
| Multi-language UI | i18n for Hindi, Spanish, Mandarin | Medium |
| Analytics export | Export progress as PDF or CSV | Low |

### Infrastructure Roadmap

```
Month 1-2:   Single VPS — current architecture
Month 3-4:   Add Redis cache for public endpoints
Month 5-6:   Add read replica for analytics queries
Month 7-9:   Add second backend instance + Nginx load balancer
Month 10-12: Migrate file uploads to S3
Year 2:      Kubernetes deployment, RabbitMQ for async, Elasticsearch for search
```

---

## Capacity Planning

| Users (MAU) | Architecture | Monthly Cost (est.) |
|-------------|-------------|---------------------|
| < 1,000 | Single VPS (4 vCPU / 4GB) | $24–40 |
| 1,000–10,000 | VPS + Redis | $50–80 |
| 10,000–50,000 | 2 app servers + PostgreSQL read replica + Redis | $150–250 |
| 50,000–200,000 | Auto-scaling cluster + RDS + ElastiCache + S3 | $400–800 |
| 200,000+ | Kubernetes + managed services + CDN | $1,500+ |

Pricing based on DigitalOcean / Hetzner. AWS/GCP will be 2–3× higher.

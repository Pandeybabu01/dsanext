# DSANext — Production Deployment Guide

This guide covers deployment on a **Linux VPS** (Ubuntu 22.04) without Docker.
Recommended: 2 vCPU, 4GB RAM, 40GB SSD — sufficient for up to 10,000 active users.

---

## Server Preparation

### Update and install dependencies

```bash
sudo apt update && sudo apt upgrade -y

# Java 21
sudo apt install -y wget apt-transport-https
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | \
  sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo $VERSION_CODENAME) main" | \
  sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-21-jdk

# Maven
sudo apt install -y maven

# Node.js 20 LTS
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# PostgreSQL 15
sudo apt install -y postgresql-15 postgresql-client-15

# Nginx (reverse proxy)
sudo apt install -y nginx

# Certbot (SSL)
sudo apt install -y certbot python3-certbot-nginx

# Useful tools
sudo apt install -y git curl unzip htop ufw fail2ban

echo "✅ Dependencies installed"
java -version && mvn -version && node -version && psql --version
```

---

## PostgreSQL Production Setup

```bash
# Switch to postgres user
sudo -u postgres psql

# In psql shell:
CREATE USER dsanext_prod WITH PASSWORD 'CHANGE_THIS_STRONG_PASSWORD';
CREATE DATABASE dsanext_prod_db OWNER dsanext_prod;
GRANT ALL PRIVILEGES ON DATABASE dsanext_prod_db TO dsanext_prod;
\c dsanext_prod_db
GRANT ALL ON SCHEMA public TO dsanext_prod;
\q

# Verify connection
PGPASSWORD='CHANGE_THIS_STRONG_PASSWORD' psql \
  -h localhost -U dsanext_prod -d dsanext_prod_db -c "SELECT version();"
```

### PostgreSQL Tuning (for 4GB RAM server)

Edit `/etc/postgresql/15/main/postgresql.conf`:

```ini
# Memory
shared_buffers         = 1GB
effective_cache_size   = 3GB
work_mem               = 16MB
maintenance_work_mem   = 256MB

# Connections
max_connections        = 100

# WAL
wal_buffers            = 64MB
checkpoint_completion_target = 0.9
random_page_cost       = 1.1

# Logging
log_min_duration_statement = 1000   # Log queries over 1s
log_checkpoints            = on
```

```bash
sudo systemctl restart postgresql
```

---

## Application User

Run the app as a dedicated non-root user:

```bash
sudo useradd -r -m -d /opt/dsanext -s /bin/bash dsanext
sudo mkdir -p /opt/dsanext/{backend,frontend,uploads/profiles,logs}
sudo chown -R dsanext:dsanext /opt/dsanext
```

---

## Backend Deployment

### Clone and build

```bash
# As the dsanext user
sudo -u dsanext bash

cd /opt/dsanext
git clone https://github.com/your-org/dsanext.git source
cd source/backend

# Build production JAR (skips tests for speed — run tests in CI/CD)
mvn clean package -DskipTests -Pproduction

# Copy artifact
cp target/dsanext-backend-1.0.0.jar /opt/dsanext/backend/

exit  # back to sudo user
```

### Environment Variables

Create `/opt/dsanext/backend/.env` (readable only by dsanext user):

```bash
sudo -u dsanext bash -c 'cat > /opt/dsanext/backend/.env << EOF
SPRING_PROFILES_ACTIVE=prod
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dsanext_prod_db
DB_USERNAME=dsanext_prod
DB_PASSWORD=CHANGE_THIS_STRONG_PASSWORD
JWT_SECRET=$(openssl rand -base64 48)
JWT_EXPIRATION_MS=86400000
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://dsanext.com,https://www.dsanext.com
UPLOAD_DIR=/opt/dsanext/uploads/profiles
EOF
chmod 600 /opt/dsanext/backend/.env'
```

### Systemd Service

Create `/etc/systemd/system/dsanext-backend.service`:

```ini
[Unit]
Description=DSANext Spring Boot Backend
After=network.target postgresql.service
Requires=postgresql.service

[Service]
Type=simple
User=dsanext
Group=dsanext
WorkingDirectory=/opt/dsanext/backend
EnvironmentFile=/opt/dsanext/backend/.env

ExecStart=/usr/bin/java \
  -jar dsanext-backend-1.0.0.jar \
  --spring.profiles.active=${SPRING_PROFILES_ACTIVE} \
  -XX:+UseG1GC \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/dsanext/logs/heapdump.hprof \
  -Djava.security.egd=file:/dev/./urandom

Restart=on-failure
RestartSec=10
StandardOutput=append:/opt/dsanext/logs/backend.log
StandardError=append:/opt/dsanext/logs/backend-error.log

# Security hardening
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ReadWritePaths=/opt/dsanext/uploads /opt/dsanext/logs

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable dsanext-backend
sudo systemctl start dsanext-backend

# Check status
sudo systemctl status dsanext-backend

# Watch logs
sudo journalctl -u dsanext-backend -f
# Or: tail -f /opt/dsanext/logs/backend.log
```

---

## Frontend Deployment

### Build

```bash
sudo -u dsanext bash

cd /opt/dsanext/source/frontend

# Set production environment
cat > .env.production << EOF
VITE_API_BASE_URL=https://dsanext.com/api
VITE_APP_NAME=DSANext
VITE_APP_VERSION=1.0.0
EOF

npm install
npm run build

# Deploy built files
cp -r dist/* /opt/dsanext/frontend/

exit
```

---

## Nginx Configuration

### Create site config

```bash
sudo nano /etc/nginx/sites-available/dsanext
```

```nginx
# Redirect HTTP → HTTPS
server {
    listen 80;
    server_name dsanext.com www.dsanext.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name dsanext.com www.dsanext.com;

    # SSL — managed by Certbot
    ssl_certificate     /etc/letsencrypt/live/dsanext.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/dsanext.com/privkey.pem;
    include             /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam         /etc/letsencrypt/ssl-dhparams.pem;

    # Security headers
    add_header X-Frame-Options           "SAMEORIGIN"  always;
    add_header X-Content-Type-Options    "nosniff"     always;
    add_header X-XSS-Protection          "1; mode=block" always;
    add_header Referrer-Policy           "strict-origin-when-cross-origin" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_types text/plain text/css application/json application/javascript
               text/xml application/xml image/svg+xml;

    # ── Spring Boot API (reverse proxy) ──────────────────
    location /api/ {
        proxy_pass         http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
        proxy_read_timeout 30s;
        proxy_connect_timeout 5s;

        # File upload limit
        client_max_body_size 10m;
    }

    # ── Uploaded profile images ────────────────────────
    location /uploads/ {
        alias  /opt/dsanext/uploads/;
        expires 1h;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # ── React SPA (serve index.html for all routes) ────
    location / {
        root  /opt/dsanext/frontend;
        index index.html;

        # Cache static assets aggressively
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2|woff)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            access_log off;
        }

        # For React Router — always serve index.html
        try_files $uri $uri/ /index.html;
    }

    # Block access to sensitive files
    location ~ /\. { deny all; }
}
```

Enable and test:

```bash
sudo ln -s /etc/nginx/sites-available/dsanext /etc/nginx/sites-enabled/
sudo nginx -t   # Should print: syntax is ok
sudo systemctl reload nginx
```

---

## SSL Certificate (Let's Encrypt)

```bash
# Obtain certificate (replace with your domain)
sudo certbot --nginx -d dsanext.com -d www.dsanext.com \
  --non-interactive --agree-tos --email admin@dsanext.com

# Auto-renewal (already set up by certbot, verify it works)
sudo certbot renew --dry-run
```

---

## Firewall Configuration

```bash
# UFW setup
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow SSH (change 22 to your custom port if applicable)
sudo ufw allow 22/tcp

# Allow HTTP and HTTPS (Nginx)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Block direct access to Spring Boot (only via Nginx)
# Port 8080 is NOT opened

sudo ufw enable
sudo ufw status verbose
```

---

## Fail2Ban (Brute Force Protection)

```bash
# Create DSANext jail for API login endpoint
sudo cat > /etc/fail2ban/filter.d/dsanext-api.conf << 'EOF'
[Definition]
failregex = ^<HOST> .* "POST /api/auth/login HTTP.*" 401
ignoreregex =
EOF

sudo cat > /etc/fail2ban/jail.d/dsanext.conf << 'EOF'
[dsanext-api]
enabled  = true
port     = http,https
filter   = dsanext-api
logpath  = /var/log/nginx/access.log
maxretry = 10
findtime = 600
bantime  = 3600
EOF

sudo systemctl enable fail2ban
sudo systemctl restart fail2ban
sudo fail2ban-client status dsanext-api
```

---

## Log Rotation

Create `/etc/logrotate.d/dsanext`:

```
/opt/dsanext/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    sharedscripts
    postrotate
        systemctl kill -s USR1 dsanext-backend 2>/dev/null || true
    endscript
}
```

---

## Deployment Update Script

Save as `/opt/dsanext/scripts/deploy.sh`:

```bash
#!/bin/bash
# DSANext — Zero-Downtime Deployment Update Script
set -e

DEPLOY_DIR=/opt/dsanext
SOURCE_DIR=$DEPLOY_DIR/source
BACKEND_JAR=$DEPLOY_DIR/backend/dsanext-backend-1.0.0.jar

echo "🚀 DSANext deployment started at $(date)"

# Pull latest code
cd $SOURCE_DIR
git pull origin main
echo "✅ Code updated"

# Build backend
cd $SOURCE_DIR/backend
mvn clean package -DskipTests -q
cp target/dsanext-backend-1.0.0.jar $BACKEND_JAR
echo "✅ Backend built"

# Build frontend
cd $SOURCE_DIR/frontend
npm install --silent
npm run build
rsync -a --delete dist/ $DEPLOY_DIR/frontend/
echo "✅ Frontend built and deployed"

# Restart backend (systemd handles graceful restart)
sudo systemctl restart dsanext-backend
sleep 5

# Health check
for i in {1..10}; do
  if curl -sf http://localhost:8080/api/actuator/health | grep -q '"UP"'; then
    echo "✅ Backend healthy"
    break
  fi
  echo "   Waiting for backend... ($i/10)"
  sleep 3
done

echo "✅ Deployment complete at $(date)"
```

```bash
sudo chmod +x /opt/dsanext/scripts/deploy.sh
# To deploy: sudo -u dsanext /opt/dsanext/scripts/deploy.sh
```

---

## Monitoring Checklist

### Health Endpoints

```bash
# Backend health
curl https://dsanext.com/api/actuator/health

# Nginx status
sudo nginx -t && sudo systemctl status nginx

# PostgreSQL
sudo systemctl status postgresql
sudo -u postgres psql -c "SELECT count(*) FROM pg_stat_activity WHERE datname='dsanext_prod_db';"
```

### Log Locations

| Service | Log |
|---------|-----|
| Spring Boot | `/opt/dsanext/logs/backend.log` |
| Nginx access | `/var/log/nginx/access.log` |
| Nginx error | `/var/log/nginx/error.log` |
| PostgreSQL | `/var/log/postgresql/postgresql-15-main.log` |
| Systemd | `journalctl -u dsanext-backend` |

---

## Production Environment Variables Reference

```bash
# /opt/dsanext/backend/.env (chmod 600)

SPRING_PROFILES_ACTIVE=prod

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dsanext_prod_db
DB_USERNAME=dsanext_prod
DB_PASSWORD=<strong-random-password>

# JWT — generate with: openssl rand -base64 48
JWT_SECRET=<48-char-base64-random>
JWT_EXPIRATION_MS=86400000       # 24 hours
JWT_REFRESH_MS=2592000000        # 30 days

# Server
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=https://dsanext.com,https://www.dsanext.com

# File uploads
UPLOAD_DIR=/opt/dsanext/uploads/profiles
```

---

## Backup Strategy

### PostgreSQL Automated Backup

```bash
sudo -u dsanext bash -c 'cat > /opt/dsanext/scripts/backup.sh << '"'"'EOF'"'"'
#!/bin/bash
BACKUP_DIR=/opt/dsanext/backups/db
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

PGPASSWORD=$DB_PASSWORD pg_dump \
  -h localhost -U $DB_USERNAME $DB_NAME \
  --no-password --format=custom \
  > $BACKUP_DIR/dsanext_$TIMESTAMP.dump

# Keep last 30 backups
ls -t $BACKUP_DIR/*.dump | tail -n +31 | xargs -r rm
echo "Backup complete: dsanext_$TIMESTAMP.dump"
EOF
chmod +x /opt/dsanext/scripts/backup.sh'

# Schedule daily at 2AM
(crontab -l -u dsanext 2>/dev/null; echo "0 2 * * * /opt/dsanext/scripts/backup.sh >> /opt/dsanext/logs/backup.log 2>&1") | \
  sudo crontab -u dsanext -
```

### Restore from backup

```bash
PGPASSWORD=<password> pg_restore \
  -h localhost -U dsanext_prod -d dsanext_prod_db \
  --clean --if-exists \
  /opt/dsanext/backups/db/dsanext_20240115_020000.dump
```

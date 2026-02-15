# 🚀 Docker Setup Guide for Exchange Office

## Prerequisites
- Docker Engine 20.10+ installed
- Docker Compose 2.0+ installed
- Port 8082 available on your machine

## 📋 Quick Start Commands

### 1️⃣ First Time Setup

```bash
# Navigate to your project directory
cd /path/to/your/project

# Build and start services
docker-compose up -d

# View logs (optional)
docker-compose logs -f
```

### 2️⃣ Check Status

```bash
# Check if containers are running
docker-compose ps

# Should show:
# exchange-office-db    running
# exchange-office-api   running
```

### 3️⃣ Verify Application

```bash
# Wait about 30 seconds for app to start, then test:
curl http://localhost:8082/actuator/health

# Or open in browser:
# http://localhost:8082
```

---

## 🔄 Common Commands

### Start Services
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose stop
```

### Restart Services
```bash
docker-compose restart
```

### View Logs
```bash
# All logs
docker-compose logs -f

# Only app logs
docker-compose logs -f app

# Only database logs
docker-compose logs -f db

# Last 100 lines
docker-compose logs --tail=100
```

### Rebuild After Code Changes
```bash
# Stop, rebuild, and start
docker-compose down
docker-compose build
docker-compose up -d
```

### Complete Cleanup (⚠️ Deletes Database Data)
```bash
docker-compose down -v
```

---

## 🗄️ Database Management

### Connect to Database
```bash
# Using Docker exec
docker exec -it exchange-office-db psql -U postgres -d exchange_office

# Common psql commands once connected:
\dt office.*          # List tables in office schema
\d office.users       # Describe users table
SELECT * FROM office.users;
\q                    # Quit
```

### Run SQL Script
```bash
# Copy SQL file to container and run
docker cp script.sql exchange-office-db:/script.sql
docker exec -it exchange-office-db psql -U postgres -d exchange_office -f /script.sql
```

### Database Backup
```bash
# Create backup
docker exec exchange-office-db pg_dump -U postgres exchange_office > backup.sql

# Restore backup
docker exec -i exchange-office-db psql -U postgres -d exchange_office < backup.sql
```

---

## 🐛 Troubleshooting

### Problem: Application won't start

**Check app logs:**
```bash
docker-compose logs app
```

**Common issues:**
- Database not ready yet → Wait 10-30 seconds
- Port 8082 already in use → Check with `lsof -i :8082` (Mac/Linux) or `netstat -ano | findstr :8082` (Windows)
- Flyway migration error → Check migration files in `src/main/resources/db/migration`

### Problem: Database connection error

**Check database logs:**
```bash
docker-compose logs db
```

**Verify database is ready:**
```bash
docker exec exchange-office-db pg_isready -U postgres
# Should return: /var/run/postgresql:5432 - accepting connections
```

**Restart database:**
```bash
docker-compose restart db
```

### Problem: Port already in use

**Option 1: Change port in docker-compose.yml**
```yaml
ports:
  - "8083:8082"  # Use 8083 on host, 8082 in container
```

**Option 2: Stop conflicting service**
```bash
# Find process using port 8082
lsof -i :8082  # Mac/Linux
netstat -ano | findstr :8082  # Windows

# Kill the process (use PID from above)
kill -9 <PID>  # Mac/Linux
taskkill /PID <PID> /F  # Windows
```

### Problem: Out of memory

**Increase Docker memory:**
- Docker Desktop → Settings → Resources → Memory → Set to 4GB+

**Or adjust memory in docker-compose.yml:**
```yaml
environment:
  JAVA_TOOL_OPTIONS: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0"
```

### Problem: Flyway migration errors

**Check migration status:**
```bash
docker exec -it exchange-office-db psql -U postgres -d exchange_office -c "SELECT * FROM office.flyway_schema_history;"
```

**Reset Flyway (⚠️ Development only):**
```bash
docker exec -it exchange-office-db psql -U postgres -d exchange_office -c "DROP SCHEMA office CASCADE; CREATE SCHEMA office;"
docker-compose restart app
```

---

## 🔐 Production Deployment Checklist

Before deploying to production, update `docker-compose.yml`:

### 1. Change JWT Secret
```yaml
JWT_SECRET: your-production-256-bit-secret-key-here
```

Generate a secure key:
```bash
# Linux/Mac
openssl rand -base64 32

# Or use a password generator for 256+ bits
```

### 2. Change Database Password
```yaml
db:
  environment:
    POSTGRES_PASSWORD: your_very_secure_password
app:
  environment:
    PASSWORD: your_very_secure_password
```

### 3. Don't Expose Database Port
Remove this line from `docker-compose.yml`:
```yaml
# ports:
#   - "5432:5432"  # Comment out or remove
```

### 4. Use Environment File
Create `.env` file (never commit to git):
```env
DB_PASSWORD=your_secure_password
JWT_SECRET=your_secure_jwt_secret
```

Update `docker-compose.yml`:
```yaml
environment:
  PASSWORD: ${DB_PASSWORD}
  JWT_SECRET: ${JWT_SECRET}
```

Add to `.gitignore`:
```
.env
```

---

## 📊 Monitoring

### Check Resource Usage
```bash
docker stats
```

### View Container Details
```bash
docker inspect exchange-office-app
```

### Check Network
```bash
docker network ls
docker network inspect exchange-office-net
```

---

## 🧹 Cleanup Commands

```bash
# Remove stopped containers
docker-compose down

# Remove containers and volumes (⚠️ deletes data)
docker-compose down -v

# Remove all unused Docker resources
docker system prune -a
```

---

## 📝 Notes

- **Database Schema**: Flyway automatically creates and manages the `office` schema
- **Port**: Application runs on port 8082 (both inside container and on host)
- **Graceful Shutdown**: Uses `tini` for proper signal handling
- **Memory**: JVM automatically uses 75% of container memory
- **Health Check**: Database health check ensures app waits for DB to be ready

---

## 🆘 Still Having Issues?

1. **Check all logs**: `docker-compose logs`
2. **Verify services**: `docker-compose ps`
3. **Check connectivity**: `docker exec exchange-office-api ping db`
4. **Restart everything**: `docker-compose restart`
5. **Nuclear option**: `docker-compose down -v && docker-compose up -d`

---

## 🎯 Next Steps After Successful Startup

1. Test the API: `curl http://localhost:8082/actuator/health`
2. Check Swagger UI (if enabled): `http://localhost:8082/swagger-ui.html`
3. Login and get JWT token
4. Test endpoints with token
5. Review logs: `docker-compose logs -f app`

**Application is ready when you see:**
```
Started ExchangeOfficeApplication in X.XXX seconds
```

# GitHub Setup and CI/CD Pipeline Guide

## Prerequisites

1. **Git installed** - Download from https://git-scm.com/download/win
2. **GitHub account** - https://github.com (sign up if needed)
3. **GitHub CLI (optional but recommended)** - https://cli.github.com/

---

## 1️⃣ Initialize Git Repository & Push to GitHub

### Step 1: Initialize Local Git Repository

```bash
cd hackathon-java-assignment

# Initialize git
git init

# Configure your git identity
git config user.name "Your Name"
git config user.email "your.email@example.com"

# Stage all changes
git add .

# Create initial commit
git commit -m "Initial commit: Hackathon assignment with bug fixes and search API

Features:
- Fixed optimistic locking in WarehouseRepository
- Fixed transaction boundary violation in StoreResource
- Implemented search and filter API for warehouses
- All 28 tests passing
- Comprehensive documentation

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Step 2: Create GitHub Repository

**Option A: Using GitHub Web UI**

1. Go to https://github.com/new
2. Fill in repository name: `hackathon-java-assignment`
3. Add description: "Senior Java Backend Assignment - Transaction Management & Optimistic Locking"
4. Choose: **Public** (for sharing) or **Private** (if confidential)
5. Do NOT initialize with README, .gitignore, or license (we have these)
6. Click "Create repository"

**Option B: Using GitHub CLI**

```bash
# Install GitHub CLI from https://cli.github.com/
# Then authenticate
gh auth login

# Create repository
gh repo create hackathon-java-assignment --public --source=. --remote=origin --push
```

### Step 3: Add Remote and Push

```bash
# Add the remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/hackathon-java-assignment.git

# Rename default branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main
```

### Verify

Visit `https://github.com/YOUR_USERNAME/hackathon-java-assignment` to see your repository.

---

## 2️⃣ GitHub Actions CI/CD Pipeline

Create `.github/workflows/build-and-test.yml`:

```yaml
name: Build & Test Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_DB: quarkus_test
          POSTGRES_USER: quarkus_test
          POSTGRES_PASSWORD: quarkus_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Run Unit Tests
      run: mvn test
    
    - name: Run Integration Tests
      run: mvn test -Dtest="WarehouseTestcontainersIT,WarehouseConcurrencyIT"
      env:
        QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://localhost:5432/quarkus_test
        QUARKUS_DATASOURCE_USERNAME: quarkus_test
        QUARKUS_DATASOURCE_PASSWORD: quarkus_test
    
    - name: Upload Code Coverage
      uses: codecov/codecov-action@v3
      with:
        files: ./target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella
    
    - name: Build Native Image (if main branch)
      if: github.ref == 'refs/heads/main'
      run: mvn package -Pnative -DskipTests
```

---

## 3️⃣ Code Coverage Reporting

Create `.github/workflows/code-coverage.yml`:

```yaml
name: Code Coverage Report

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  coverage:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run Tests with Coverage
      run: mvn clean test jacoco:report
    
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        files: ./target/site/jacoco/jacoco.xml
```

---

## 4️⃣ Health Check Implementation

Add health checks to your application. Create:

`src/main/java/com/fulfilment/application/monolith/health/WarehouseHealthCheck.java`:

```java
package com.fulfilment.application.monolith.health;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@ApplicationScoped
@Health
public class WarehouseHealthCheck implements HealthCheck {

  @Inject
  private WarehouseHealthProbe probe;

  @Override
  public HealthCheckResponse call() {
    try {
      probe.checkDatabaseConnectivity();
      probe.checkRepositoryAccess();
      
      return HealthCheckResponse.up("Warehouse Service")
          .withData("database", "connected")
          .withData("repository", "accessible")
          .build();
    } catch (Exception e) {
      return HealthCheckResponse.down("Warehouse Service")
          .withData("error", e.getMessage())
          .build();
    }
  }
}
```

`src/main/java/com/fulfilment/application/monolith/health/WarehouseHealthProbe.java`:

```java
package com.fulfilment.application.monolith.health;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class WarehouseHealthProbe {

  @Inject
  private EntityManager entityManager;

  @Inject
  private WarehouseRepository warehouseRepository;

  public void checkDatabaseConnectivity() {
    // Simple query to check DB connectivity
    entityManager.createQuery("SELECT 1").getSingleResult();
  }

  public void checkRepositoryAccess() {
    // Check if repository can be accessed
    warehouseRepository.getAll();
  }
}
```

`src/main/java/com/fulfilment/application/monolith/health/ReadinessProbe.java`:

```java
package com.fulfilment.application.monolith.health;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@ApplicationScoped
@Readiness
public class ReadinessProbe implements HealthCheck {

  @Inject
  private WarehouseHealthProbe probe;

  @Override
  public HealthCheckResponse call() {
    try {
      probe.checkDatabaseConnectivity();
      return HealthCheckResponse.up("Application Ready").build();
    } catch (Exception e) {
      return HealthCheckResponse.down("Application Not Ready")
          .withData("reason", "Database not accessible: " + e.getMessage())
          .build();
    }
  }
}
```

---

## 5️⃣ Configure Health Checks in application.properties

Add to `src/main/resources/application.properties`:

```properties
# Health Check Configuration
quarkus.health.enabled=true
quarkus.management.enabled=true
quarkus.management.port=9000

# Expose health endpoints
quarkus.smallrye-health.root-path=/health
quarkus.smallrye-health.liveness.root-path=/health/live
quarkus.smallrye-health.readiness.root-path=/health/ready

# Enable detailed health info
quarkus.smallrye-health.detailed-enabled=true
```

---

## 6️⃣ Health Check Endpoints

Once running, health checks are available at:

```bash
# Overall health
curl http://localhost:9000/health

# Liveness probe (is app running?)
curl http://localhost:9000/health/live

# Readiness probe (is app ready for requests?)
curl http://localhost:9000/health/ready

# Detailed health (JSON format)
curl http://localhost:9000/health | jq
```

---

## 7️⃣ GitHub Actions for Health Checks

Create `.github/workflows/health-check.yml`:

```yaml
name: Health Check After Deploy

on:
  workflow_run:
    workflows: ["Build & Test Pipeline"]
    types: [completed]
    branches: [main]

jobs:
  health-check:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Start Application
      run: |
        mvn quarkus:dev &
        sleep 30
      
    - name: Check Liveness Probe
      run: |
        curl -f http://localhost:8080/health/live || exit 1
    
    - name: Check Readiness Probe
      run: |
        curl -f http://localhost:8080/health/ready || exit 1
    
    - name: Check Overall Health
      run: |
        curl -f http://localhost:8080/health || exit 1
    
    - name: Verify Health Status
      run: |
        curl http://localhost:8080/health | grep -q "UP" || exit 1
```

---

## 8️⃣ Step-by-Step Git Push Guide

### Complete Commands:

```bash
# 1. Navigate to project
cd path/to/hackathon-java-assignment

# 2. Initialize git (if not already done)
git init

# 3. Configure git
git config user.name "Your Name"
git config user.email "your.email@example.com"

# 4. Add all files
git add .

# 5. Create commit
git commit -m "Initial commit: Hackathon assignment - bug fixes and search API"

# 6. Add GitHub remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/hackathon-java-assignment.git

# 7. Rename branch to main
git branch -M main

# 8. Push to GitHub
git push -u origin main

# 9. Verify
git remote -v
```

---

## 9️⃣ GitHub Repository Setup Checklist

### README & Documentation
- [ ] Push START_HERE.md to repository
- [ ] Push INDEX.md for navigation
- [ ] Push QUESTIONS.md with answers
- [ ] Update README.md with build/health check info

### CI/CD Workflows
- [ ] Create `.github/workflows/build-and-test.yml`
- [ ] Create `.github/workflows/code-coverage.yml`
- [ ] Create `.github/workflows/health-check.yml`

### Health Checks
- [ ] Add WarehouseHealthCheck.java
- [ ] Add WarehouseHealthProbe.java
- [ ] Add ReadinessProbe.java
- [ ] Configure application.properties

### Additional Files
- [ ] Add `.github/PULL_REQUEST_TEMPLATE.md`
- [ ] Add `.github/ISSUE_TEMPLATE/bug_report.md`
- [ ] Create CONTRIBUTING.md

### Branch Protection
- [ ] Go to Settings → Branches
- [ ] Add rule for main branch
- [ ] Require status checks to pass
- [ ] Require code reviews
- [ ] Dismiss stale PR approvals

---

## 🔟 Pull Request Template

Create `.github/pull_request_template.md`:

```markdown
## Description
Please include a summary of the changes and related context.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Related Issues
Fixes #(issue number)

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review
- [ ] I have commented complex logic
- [ ] Tests pass locally
- [ ] No new warnings generated

## Screenshots (if applicable)
```

---

## Deployment Options

### Option 1: Docker Deployment

Create `Dockerfile`:

```dockerfile
FROM quarkus/ubi-quarkus-native-image:latest AS builder

WORKDIR /workspace
COPY . .
RUN mvn package -Pnative -DskipTests

FROM quarkus/ubi-quarkus-native-runtime:latest
WORKDIR /work/
COPY --from=builder /workspace/target/*-runner /work/application

EXPOSE 8080
EXPOSE 9000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:9000/health/ready || exit 1

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
```

### Option 2: GitHub Pages (Documentation)

```bash
# Create docs folder
mkdir -p docs

# Copy documentation
cp *.md docs/

# Commit and push
git add docs/
git commit -m "docs: Add project documentation"
git push origin main
```

Then go to Repository Settings → Pages → Source: main/docs folder

---

## Monitoring & Alerts

### GitHub Actions Notifications

1. Go to Settings → Notifications
2. Enable email for workflow failures
3. Create custom rules for branch notifications

### Status Badges

Add to README.md:

```markdown
# Hackathon Java Assignment

![Build & Test](https://github.com/YOUR_USERNAME/hackathon-java-assignment/workflows/Build%20&%20Test%20Pipeline/badge.svg)
![Code Coverage](https://codecov.io/gh/YOUR_USERNAME/hackathon-java-assignment/branch/main/graph/badge.svg)

## Quick Links
- [Project Documentation](https://github.com/YOUR_USERNAME/hackathon-java-assignment)
- [Health Checks](#health-checks)
- [Contributing](CONTRIBUTING.md)
```

---

## Quick Reference

### Test & Build Locally
```bash
mvn clean test              # Run unit tests
mvn package                 # Build application
mvn quarkus:dev            # Start dev server
```

### Check Health
```bash
curl http://localhost:8080/health/ready
curl http://localhost:8080/health/live
```

### Push Changes
```bash
git add .
git commit -m "Your message"
git push origin main
```

---

## Troubleshooting

**Issue**: "fatal: not a git repository"
```bash
git init
```

**Issue**: "Permission denied" when pushing
```bash
# Use personal access token instead of password
# Go to GitHub → Settings → Developer settings → Personal access tokens
# Generate token with 'repo' scope
# Use: git push https://<TOKEN>@github.com/user/repo.git
```

**Issue**: "Connection refused" for health checks
```bash
# Make sure app is running
mvn quarkus:dev

# Check if port 8080 is in use
netstat -ano | findstr :8080
```

---

## Summary

1. ✅ Initialize git repository
2. ✅ Create GitHub repository
3. ✅ Push code to GitHub
4. ✅ Set up CI/CD pipelines
5. ✅ Implement health checks
6. ✅ Configure branch protection
7. ✅ Set up monitoring

Your hackathon assignment is now in production-grade state with automated testing and health monitoring!

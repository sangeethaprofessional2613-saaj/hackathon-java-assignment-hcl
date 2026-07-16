# 🚀 Quick Start: Push to GitHub with CI/CD

## Prerequisites Check

Before you start, make sure you have:
- ✅ Git installed (https://git-scm.com/download/win)
- ✅ GitHub account (https://github.com)
- ✅ GitHub repository created

---

## 📋 Step-by-Step: Git Setup & Push

### Step 1: Open Command Prompt/PowerShell

```bash
# Navigate to project directory
cd "C:\Users\L E N O V O\Downloads\hackathon-java-assignment-20260716T052815Z-1-001\hackathon-java-assignment"
```

### Step 2: Initialize Git Repository

```bash
# Initialize git
git init

# Configure git identity (one-time setup)
git config user.name "Your Full Name"
git config user.email "your.email@example.com"

# Verify configuration
git config --list
```

### Step 3: Stage All Changes

```bash
# Stage all files
git add .

# Verify staged files
git status
```

### Step 4: Create Initial Commit

```bash
# Commit with descriptive message
git commit -m "Initial commit: Hackathon assignment with bug fixes and search API

- Fixed optimistic locking in WarehouseRepository
- Fixed transaction boundary violation in StoreResource
- Implemented search and filter API for warehouses
- All 28 tests passing
- Added CI/CD pipelines
- Added health checks

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Step 5: Create GitHub Repository

**Via GitHub Web UI (Recommended):**

1. Go to https://github.com/new
2. **Repository name**: `hackathon-java-assignment`
3. **Description**: "Senior Java Backend Assignment - Transaction Management & Concurrency Control"
4. **Visibility**: Choose **Public** or **Private**
5. ⚠️ **IMPORTANT**: Do NOT check "Initialize this repository with a README"
6. Click **Create repository**

**Result**: You'll see a page with commands like:
```
git remote add origin https://github.com/YOUR_USERNAME/hackathon-java-assignment.git
```

### Step 6: Add Remote and Push

```bash
# Replace YOUR_USERNAME with your actual GitHub username
git remote add origin https://github.com/YOUR_USERNAME/hackathon-java-assignment.git

# Rename main branch
git branch -M main

# Push to GitHub
git push -u origin main

# Verify remote
git remote -v
```

### Step 7: Verify on GitHub

✅ Visit: `https://github.com/YOUR_USERNAME/hackathon-java-assignment`

You should see:
- All your files and folders
- .github/workflows with CI/CD pipelines
- Health check classes
- All documentation files

---

## 🔄 Subsequent Commits

After the initial push, use these commands for future commits:

```bash
# See what changed
git status

# Stage changes
git add .

# Commit
git commit -m "Your commit message"

# Push to GitHub
git push origin main
```

---

## ✅ CI/CD Pipeline Files Created

The following files have been created for you:

### Workflows
- ✅ `.github/workflows/build-and-test.yml` - Automated build and test pipeline

### Health Checks
- ✅ `src/main/java/.../health/WarehouseHealthCheck.java`
- ✅ `src/main/java/.../health/WarehouseHealthProbe.java`
- ✅ `src/main/java/.../health/ReadinessProbe.java`
- ✅ `src/main/java/.../health/LivenessProbe.java`

### Configuration
- ✅ `.github/pull_request_template.md` - PR template
- ✅ `.gitignore` - Git ignore rules
- ✅ `application.properties` - Updated with health check config
- ✅ `Dockerfile` - Container deployment
- ✅ `docker-compose.yml` - Local development with Docker

### Documentation
- ✅ `GITHUB_AND_CICD_SETUP.md` - This comprehensive guide

---

## 🏥 Health Check Endpoints

Once deployed, access health checks at:

```bash
# Overall health status
curl http://localhost:8080/health

# Liveness probe (is app running?)
curl http://localhost:8080/health/live

# Readiness probe (is app ready for requests?)
curl http://localhost:8080/health/ready

# Detailed JSON response
curl http://localhost:8080/health | jq
```

**Example Response**:
```json
{
  "status": "UP",
  "checks": [
    {
      "name": "Warehouse Service",
      "status": "UP",
      "data": {
        "database": "connected",
        "repository": "accessible"
      }
    },
    {
      "name": "Application Ready",
      "status": "UP"
    },
    {
      "name": "Application Live",
      "status": "UP"
    }
  ]
}
```

---

## 🐳 Docker Deployment

### Local Development

```bash
# Build Docker image
docker build -t hackathon-assignment:latest .

# Run with docker-compose
docker-compose up -d

# Check health
curl http://localhost:8080/health/ready

# View logs
docker-compose logs -f app

# Stop
docker-compose down
```

### Docker Hub Push (Optional)

```bash
# Tag image
docker tag hackathon-assignment:latest YOUR_USERNAME/hackathon-assignment:latest

# Push to Docker Hub
docker push YOUR_USERNAME/hackathon-assignment:latest
```

---

## 🔧 Troubleshooting

### Issue: "fatal: not a git repository"
```bash
git init
```

### Issue: "fatal: could not read Username for 'github.com'"
Use personal access token instead of password:
1. Go to GitHub → Settings → Developer settings → Personal access tokens
2. Generate token with 'repo' scope
3. Use: `git push https://YOUR_TOKEN@github.com/YOUR_USERNAME/repo.git`

### Issue: "Permission denied (publickey)"
Set up SSH keys:
```bash
ssh-keygen -t ed25519 -C "your.email@example.com"
# Add public key to GitHub Settings → SSH Keys
git remote set-url origin git@github.com:YOUR_USERNAME/hackathon-java-assignment.git
```

### Issue: "Connection refused" for health checks
```bash
# Start the app
mvn quarkus:dev

# In another terminal, test health
curl http://localhost:8080/health/ready
```

---

## 📊 CI/CD Pipeline Details

### What Happens on Push?

When you push code to GitHub, the `.github/workflows/build-and-test.yml` automatically:

1. ✅ Checks out your code
2. ✅ Sets up JDK 17
3. ✅ Builds Maven project
4. ✅ Runs all 28 unit tests
5. ✅ Runs integration tests
6. ✅ Uploads test results
7. ✅ Reports status (success or failure)

### View Pipeline Results

1. Go to your GitHub repository
2. Click **Actions** tab
3. See all workflow runs
4. Click on any run to see details

---

## 🎯 Next Steps

1. **Push to GitHub** using steps above
2. **Monitor CI/CD pipeline** at Actions tab
3. **Check health endpoints** after deployment
4. **Share repository link** with stakeholders
5. **Enable branch protection** (optional but recommended)

---

## 📚 Additional Resources

- [Git Documentation](https://git-scm.com/doc)
- [GitHub Actions Guide](https://docs.github.com/en/actions)
- [Quarkus Health Guide](https://quarkus.io/guides/microprofile-health)
- [Docker Documentation](https://docs.docker.com/)

---

## Summary Checklist

- [ ] Git installed and configured
- [ ] GitHub repository created
- [ ] All files staged with `git add .`
- [ ] Initial commit created
- [ ] Remote added: `git remote add origin ...`
- [ ] Code pushed: `git push -u origin main`
- [ ] Repository visible on GitHub
- [ ] CI/CD pipeline running (check Actions tab)
- [ ] Health checks configured
- [ ] Docker setup ready

**You're all set! 🎉**

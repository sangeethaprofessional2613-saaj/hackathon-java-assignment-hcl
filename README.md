# Java Hackathon Assignment - 6 Hour Edition

This is a **hackathon-style code assignment** designed to be completed in approximately **6 hours**. It covers API design, persistence, testing patterns, and transaction management in a real-world Quarkus application.

## Quick Start

```bash
# 1. Start the application in dev mode
./mvnw quarkus:dev

# 2. Access Swagger UI
open http://localhost:8080/q/swagger-ui

# 3. Run tests
./mvnw test

# 4. Generate code coverage report
./mvnw clean test jacoco:report
```

## Project Overview

This codebase follows **Hexagonal Architecture** (Ports & Adapters) with:

- Domain use cases isolated from REST and database concerns
- CDI events for post-commit integration calls
- OpenAPI-generated REST layer for the Warehouse API
- Hand-coded REST endpoints for Stores and Products
- JaCoCo code coverage reporting configured

---

## Technologies

- **Java 17+**
- **Quarkus 3.13.3**
- **PostgreSQL** (via Docker or Quarkus Dev Services)
- **H2 Database** (for testing)
- **JUnit 5** + **Testcontainers** + **Mockito**
- **OpenAPI** (code generation for Warehouse API)
- **JaCoCo** (code coverage with 80% target)

---

## Running the Code

```bash
# Compile and run tests
./mvnw clean test

# Run specific test class
./mvnw test -Dtest=ArchiveWarehouseUseCaseTest

# Generate coverage report
./mvnw jacoco:report

# Start development mode
./mvnw quarkus:dev

# Access Swagger UI
open http://localhost:8080/q/swagger-ui
```

### Run in JVM mode

First compile:

```bash
./mvnw package
```

Start a PostgreSQL instance:

```bash
docker run -it --rm=true --name quarkus_test \
  -e POSTGRES_USER=quarkus_test \
  -e POSTGRES_PASSWORD=quarkus_test \
  -e POSTGRES_DB=quarkus_test \
  -p 15432:5432 postgres:13.3
```

Then run:

```bash
java -jar ./target/quarkus-app/quarkus-run.jar
```

Navigate to <http://localhost:8080/index.html>

---

## Code Coverage

The project uses JaCoCo for code coverage reporting with a minimum target of 80% line coverage.

Current coverage can be viewed at: `target/site/jacoco/index.html`

**Good luck!** This is about demonstrating your understanding of production-grade patterns, not just writing code under pressure.

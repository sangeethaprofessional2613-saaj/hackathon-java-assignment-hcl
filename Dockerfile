FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /build
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Native image stage (optional - for faster startup)
FROM quarkus/ubi-quarkus-native-image:latest AS native-builder

WORKDIR /workspace
COPY --from=builder /build .

RUN mvn package -Pnative -DskipTests

# Runtime stage
FROM quarkus/ubi-quarkus-native-runtime:latest

WORKDIR /work/

# Copy the native executable from builder
COPY --from=native-builder /workspace/target/*-runner /work/application

EXPOSE 8080
EXPOSE 9000

# Health check - uses readiness probe
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:9000/health/ready || exit 1

# Set environment variables
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0"

# Run the application
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]

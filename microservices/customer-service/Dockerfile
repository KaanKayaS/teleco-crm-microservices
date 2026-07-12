# ============================================================
# Stage 1 — deps: Cache Maven dependencies
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS deps

WORKDIR /build

# Maven wrapper & config
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy only pom to cache dependencies
COPY pom.xml .

# Download all dependencies (cached layer — only re-runs when pom.xml changes)
RUN ./mvnw dependency:go-offline -B -q

# ============================================================
# Stage 2 — build: Compile & package the application
# ============================================================
FROM deps AS build

# Copy source code
COPY src ./src

# Build the application (skip tests — they should run in CI)
RUN ./mvnw package -DskipTests -B \
    && mv target/*.jar target/app.jar

# ============================================================
# Stage 3 — runtime: Minimal production image
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Create upload directory for file uploads
RUN mkdir -p /app/uploads/documents && \
    chown -R appuser:appgroup /app

# Copy the fat JAR from build stage
COPY --from=build --chown=appuser:appgroup /build/target/app.jar ./app.jar

# Switch to non-root user
USER appuser:appgroup

# Expose application port
EXPOSE 8081

# JVM tuning via environment variable
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

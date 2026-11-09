# Multi-stage Dockerfile for GEOCYBER INTEL
# Stage 1: Build the Maven application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build executable jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE image
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="GEOCYBER Research Team"
LABEL description="Advanced Geopolitical Cyber Threat Intelligence and Multimodal APT Attribution Platform"

WORKDIR /app

# Create non-root application user for defense-in-depth security
RUN addgroup -S geocyber && adduser -S geocyber -G geocyber

# Copy compiled jar from builder stage
COPY --from=builder /build/target/geocyber-intel-1.0.0-SNAPSHOT.jar app.jar

RUN chown -R geocyber:geocyber /app
USER geocyber

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

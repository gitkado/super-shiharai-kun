# Multi-stage Docker build for production
# Build stage - use pre-installed Gradle from base image
FROM gradle:8-jdk11 AS builder

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build using the pre-installed Gradle (not gradlew)
RUN gradle buildFatJar --no-daemon

# Runtime stage
FROM openjdk:11-jre-slim AS runtime

# Install necessary packages and create non-root user
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        netcat-traditional && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r appuser && \
    useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR file from builder stage
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Run the application with environment variable support
ENTRYPOINT ["sh", "-c", "exec java -server ${JAVA_OPTS:--XX:+UseContainerSupport} -jar app.jar"]
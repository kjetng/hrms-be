# Multi-stage build: First stage builds the app, second copies the JAR to runtime
FROM eclipse-temurin:24-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Gradle files first for better caching
COPY gradlew settings.gradle ./
COPY gradle ./gradle
COPY build.gradle ./

# Download dependencies (cached unless gradle files change)
RUN ./gradlew dependencies --no-daemon

# Copy source and build the JAR
COPY src ./src
RUN ./gradlew build --no-daemon

# Runtime stage: Use slim JRE image
FROM eclipse-temurin:24-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose Spring Boot port (default 8080)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
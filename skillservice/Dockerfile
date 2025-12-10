# ========================
# Stage 1: Build Stage
# ========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copy pom.xml first
COPY pom.xml .

# 2. Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# 3. Copy source code
COPY src ./src

# 4. Build the jar
RUN mvn clean package -DskipTests

# ========================
# Stage 2: Run Stage
# ========================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8083

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Healthcheck (optional - remove if no actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]

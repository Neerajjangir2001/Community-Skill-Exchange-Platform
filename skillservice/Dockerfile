# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# IMPORTANT: This Dockerfile must be built with the build context at the PROJECT ROOT.
# e.g., docker build -f skillservice/Dockerfile .

# 1. Build authService (Reference dependency)
COPY authService/pom.xml authService/
COPY authService/src authService/src
RUN mvn -f authService/pom.xml clean install -DskipTests

# 2. Build userprofile (Reference dependency)
COPY userprofile/pom.xml userprofile/
COPY userprofile/src userprofile/src
RUN mvn -f userprofile/pom.xml clean install -DskipTests

# 3. Build skillservice
COPY skillservice/pom.xml skillservice/
COPY skillservice/src skillservice/src
RUN mvn -f skillservice/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/skillservice/target/skillservice-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "skillservice-0.0.1-SNAPSHOT.jar"]

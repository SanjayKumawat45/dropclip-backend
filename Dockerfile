# =========================================================
# STAGE 1 — BUILD SPRING BOOT APPLICATION
# =========================================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first
# This allows Docker to cache dependencies
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests


# =========================================================
# STAGE 2 — RUN SPRING BOOT APPLICATION
# =========================================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy generated Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Render uses the PORT environment variable.
# Spring Boot will be configured to use it.
EXPOSE 10000

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
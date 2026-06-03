# ========================================
# DOCKERFILE - Backend Build & Run (single-stage)
# ========================================
# Single stage avoids pulling a second base image (flaky DNS).
# The maven image is based on eclipse-temurin-11, so it can both
# build the project and run the resulting JAR with `java -jar`.

FROM maven:3.8.6-eclipse-temurin-11

# Set working directory inside container
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src
COPY mvnw ./mvnw
COPY .mvn ./.mvn

# Build the project (skip tests for faster image builds)
RUN mvn clean package -DskipTests

# Create directories for model and images
RUN mkdir -p /app/model /app/images

# Expose port that Spring Boot runs on
EXPOSE 8080

# Run the built Spring Boot application
ENTRYPOINT ["sh", "-c", "java -jar target/siamese-0.0.1-SNAPSHOT.jar"]

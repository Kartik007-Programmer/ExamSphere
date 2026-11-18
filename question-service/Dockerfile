# Build stage
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
# Use Maven wrapper or system Maven to build
RUN apk add --no-cache maven && mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar

ENV QUESTION_SERVICE_PORT=8081
ENTRYPOINT ["java", "-jar", "app.jar"]
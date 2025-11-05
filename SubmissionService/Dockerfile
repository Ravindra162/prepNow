# Multi-stage build for SubmissionService
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/SubmissionService-0.0.1-SNAPSHOT.jar app.jar
# Create directory for local file storage
RUN mkdir -p /tmp/submissions
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]


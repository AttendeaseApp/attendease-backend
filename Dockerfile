FROM ubuntu:latest AS build
RUN apt-get update
RUN apt-get install openjdk-21-jdk -y
COPY . .
RUN ./gradlew bootjar --no-daemon

FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates
EXPOSE 8080
COPY /build/libs/attendease-backend-0.0.1.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

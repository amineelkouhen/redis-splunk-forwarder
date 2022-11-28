# Stage 1 - Build
FROM openjdk:18-ea-20-jdk-slim AS build
MAINTAINER Amine El Kouhen
RUN mkdir /forwarder
COPY . /forwarder
WORKDIR /forwarder
RUN ./gradlew build --no-daemon
ENTRYPOINT ["java","-jar","/build/libs/redis-splunk-forwarder-1.0.0.jar"]

## Stage 2 - Package
FROM --platform=linux/x86_64 openjdk:18-jdk-alpine AS runtime
COPY --from=build /forwarder/build/libs/redis-splunk-forwarder-1.0.0.jar app.jar
EXPOSE 8585
ENTRYPOINT ["java","-jar","/app.jar"]
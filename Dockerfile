FROM eclipse-temurin:21-jdk-alpine

VOLUME /tmp
RUN apk update && apk upgrade --no-cache \
    && apk add --no-cache gnupg libpng busybox bash

COPY target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
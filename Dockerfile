FROM openjdk:17.0.2-jdk-slim AS build

WORKDIR /app

COPY gradlew gradlew.bat /app/
COPY gradle/wrapper /app/gradle/wrapper
COPY build.gradle settings.gradle /app/

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY ./src/main /app/src/main

RUN ./gradlew clean build --no-daemon

FROM openjdk:17.0.2-jdk-slim

WORKDIR /app

COPY --from=build /app/build/libs/schedule-sharing-service-0.0.1-SNAPSHOT.jar /app/schedule-sharing-service.jar

EXPOSE 8080
EXPOSE 5005

ENTRYPOINT ["java"]
CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "schedule-sharing-service.jar"]
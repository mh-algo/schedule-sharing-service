FROM openjdk:17.0.2-jdk-slim

WORKDIR /app

COPY ./build/libs/schedule-sharing-service-0.0.1-SNAPSHOT.jar /app/schedule-sharing-service.jar

EXPOSE 8080
EXPOSE 5005

ENTRYPOINT ["java"]
CMD ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "schedule-sharing-service.jar"]
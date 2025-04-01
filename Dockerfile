FROM eclipse-temurin:21-jre
COPY /target/daily-reporting-service-0.0.1-SNAPSHOT.jar daily-reporting-service-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "daily-reporting-service-0.0.1-SNAPSHOT.jar"]
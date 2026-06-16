FROM phenompeople/openjdk17:latest

WORKDIR /app
COPY target/travel-ticket-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseG1GC", "-jar", "/app/app.jar"]

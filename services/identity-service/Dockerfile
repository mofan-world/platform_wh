FROM phenompeople/openjdk17:latest

WORKDIR /app
COPY target/identity-service-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseG1GC", "-jar", "/app/app.jar"]

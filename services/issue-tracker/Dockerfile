FROM phenompeople/openjdk17:latest

WORKDIR /app
RUN mkdir -p /home/appuser/uploads/attachments
COPY target/issue-tracker-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+UseG1GC", "-jar", "/app/app.jar"]

FROM phenompeople/openjdk17:latest

WORKDIR /app
COPY gateway/GatewayServer.java /app/GatewayServer.java
COPY apps/issue-tracker-web/dist /app/issue-tracker-web
COPY apps/identity-web/dist /app/identity-web

EXPOSE 80
ENTRYPOINT ["java", "/app/GatewayServer.java"]

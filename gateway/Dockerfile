FROM phenompeople/openjdk17:latest

WORKDIR /app
COPY gateway/GatewayServer.java /app/GatewayServer.java
COPY apps/issue-tracker-web/dist /app/issue-web
COPY apps/identity-web/dist /app/identity-web
COPY apps/travel-ticket-web/dist /app/travel-web

EXPOSE 80
ENTRYPOINT ["java", "/app/GatewayServer.java"]

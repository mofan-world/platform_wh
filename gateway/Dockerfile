FROM phenompeople/openjdk17:latest

WORKDIR /app
COPY gateway/GatewayServer.java /app/GatewayServer.java
COPY apps/issue-tracker-web/dist /app/issue-web
<<<<<<< HEAD
=======
COPY apps/identity-web/dist /app/identity-web
COPY apps/travel-ticket-web/dist /app/travel-web
>>>>>>> 8d4a0dd22c32f7596c9a1123e5b559292bcd79dd

EXPOSE 80
ENTRYPOINT ["java", "/app/GatewayServer.java"]

# ---------- 1 Build stage ----------
FROM maven:3.9.10-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# download dependencies first (kept in Docker layer cache)
RUN mvn -q dependency:go-offline
COPY src ./src
# produce an executable JAR
RUN mvn -q package -DskipTests

# ---------- 2 Runtime stage ----------
FROM eclipse-temurin:21-jre AS runtime
LABEL org.opencontainers.image.source="https://github.com/your-org/currency-exchange"
WORKDIR /app

# grab only the JAR we just built
COPY --from=build /app/target/*.jar app.jar

# allow overriding the port (default 8080)
ARG APP_PORT=8080
ENV SERVER_PORT=$APP_PORT

EXPOSE ${SERVER_PORT}

# tini = proper signal handling (graceful shutdown)
RUN apt-get update && apt-get install -y --no-install-recommends tini && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["/usr/bin/tini", "--"]
CMD ["java","-jar","/app/app.jar"]


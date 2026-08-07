# ── Stage 1: Build ──────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DESCO Simulation <admin@desco.com>"
LABEL service="admin-service"

RUN addgroup -S desco && adduser -S desco -G desco
USER desco

WORKDIR /app

COPY --from=builder /app/target/admin-service-*.jar app.jar

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8087/actuator/health | grep -q '"status":"UP"' || exit 1

EXPOSE 8087

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

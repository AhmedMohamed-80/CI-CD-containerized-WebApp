FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B


COPY src ./src
RUN ./mvnw package -DskipTests -B


FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app


RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser


COPY --from=builder /app/target/metrics-dashboard.jar app.jar


HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
